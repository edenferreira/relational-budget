(ns ^{:doc "CRUD for development"}
    edenferreira.rawd.api
  (:require [clojure.string :as str]
            [hiccup.page :as h.page]
            [hiccup.core :as h]
            [edenferreira.rawd.instant :as instant]
            [edenferreira.rawd.view :as view]
            [clojure.set :as set]
            [clojure.string :as string]))

(defn create-index [get-state entities]
  (fn index [request]
    (h.page/html5
     {}
     (h/html
      (view/index
       (cond-> {:entities entities}
         (::as-of request)
         (assoc :as-of (::as-of request)
                :state (get-state :as-of (::as-of request)))

         (nil? (::as-of request))
         (assoc :state (get-state))))))))

(defn create-respond-index [get-state entities]
  (let [index (create-index get-state entities)]
    (fn respond-index [request]
      {:status 200 :body (index request)})))

(defn respond-create-entity [entity adapter handler]
  (fn create-entity [request]
    (let [adapted (adapter (update-keys
                            (:params request)
                            #(keyword (str/replace %
                                                   (str (name entity) "-")
                                                   ""))))
          handled (handler adapted)]
      (if (= 200 (:status handled))
        {:status 303
         :headers {"Location" "/"}
         :body {}}
        handled))))

(defn entities->routes [interceptors entities]
  (set
   (map
    (fn [{:keys [name adapter handler]
          :or {adapter identity
               handler identity}}]
      [(str "/" (clojure.core/name name) "/create")
       :post (conj interceptors
                   (respond-create-entity name adapter handler))
       :route-name (keyword (str "creating-entity-" (clojure.core/name name)))])
    entities)))

(def as-of-interceptor
  {:enter (fn [context]
            (let [params (get-in context [:request :params])
                  as-of (or (get params :as-of)
                            (get params "as-of"))]
              (update context
                      :request
                      merge
                      (cond-> {::now (instant/now)}
                        (and as-of
                             (not (string/ends-with? as-of "Z")))
                        (assoc ::as-of (instant/parse (str as-of ":00Z")))

                        (and as-of
                             (string/ends-with? as-of "Z"))
                        (assoc ::as-of (instant/parse as-of))))))})

(defn routes [interceptors get-state entities]
  (let [interceptors (conj interceptors as-of-interceptor)]
    (set/union
     #{["/" :get (conj interceptors (create-respond-index get-state entities)) :route-name :index]
       ["/index.html" :get (conj interceptors (create-respond-index get-state entities)) :route-name :index-html]
       ["/filter/as-of" :post (conj interceptors
                                    (fn [request]
                                      {:status 303
                                       :headers {"Location" (str "/?as-of=" (str (get (:params request) "as-of")
                                                                                 ":00Z"))}
                                       :body {}}))
        :route-name :filter-as-of]}
     (entities->routes interceptors entities))))

(comment
  (->> (db->table {:c #{{:a 1 :b 2}
                        {:a 2 :b 3}}})
       (h.html5/page {}))
  (do h)
  (do ppp)
  '_)
