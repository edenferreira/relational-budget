(ns ^{:doc "CRUD for development"}
 edenferreira.rawd.api
  (:require [clojure.string :as str]
            [hiccup.page :as h.page]
            [hiccup.core :as h]
            [edenferreira.rawd :as-alias rwd]
            [edenferreira.rawd.instant :as instant]
            [edenferreira.rawd.view :as view]
            [clojure.set :as set]
            [clojure.string :as string]
            [edenferreira.clojure.set.extensions :as set.ext]
            [edenferreira.rawd.api :as rawd]))

(defn create-index [get-state definition]
  (let [adapt-params (->> definition
                          ::rwd/filters
                          (map ::rwd/adapter)
                          (apply juxt))]
    (fn index [request]
      (let [params (:params request)
            adapted (apply merge (adapt-params params))]
        (h.page/html5
         {}
         (h/html
          (view/index
           (cond-> definition
             (::as-of request)
             (assoc :as-of (::as-of request)
                    :state (get-state :as-of (::as-of request)
                                      adapted))

             (nil? (::as-of request))
             (assoc :state (get-state adapted))))))))))

(defn create-respond-index [get-state definition]
  (let [index (create-index get-state definition)]
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

(defn entities-routes [interceptors entities]
  (set
   (map :route/route
        (set.ext/extend
         (set.ext/extend
          entities
           :route/uri (comp #(str "/" % "/create") name ::rwd/entity)
           :route/handler (fn [{::rwd/keys [entity adapter handler]
                                :or {adapter identity
                                     handler identity}}]
                            (respond-create-entity entity adapter handler))
           :route/name (comp keyword
                             (partial str "creating-entity-")
                             name ::rwd/entity))
          :route/route
          (juxt :route/uri
                (constantly :post)
                (comp (partial conj interceptors) :route/handler)
                (constantly :route-name)
                :route/name)))))

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

(defn routes [interceptors get-state definition]
  (let [interceptors (conj interceptors as-of-interceptor)]
    (set/union
     #{["/" :get (conj interceptors
                       (create-respond-index get-state definition))
        :route-name :index]
       ["/index.html" :get (conj interceptors
                                 (create-respond-index get-state definition))
        :route-name :index-html]
       ["/filter/attributes" :post
        (conj interceptors
              (fn [request]
                (let [params (update-keys
                              (get request :params)
                              #(string/replace %
                                               "filter-"
                                               ""))]
                  {:status 303
                   :headers
                   {"Location"
                    (str "/?"
                         (->> params
                              (map (partial string/join "="))
                              (string/join "&")))}
                   :body {}})))
        :route-name :filter-attributes]
       ["/filter/as-of" :post
        (conj interceptors
              (fn [request]
                {:status 303
                 :headers
                 {"Location"
                  (str "/?as-of="
                       (str (get (:params request) "as-of")
                            ":00Z"))}
                 :body {}}))
        :route-name :filter-as-of]}
     (set (map :route/route (entities-routes interceptors (::rwd/entities definition)))))))

(comment
  (->> (db->table {:c #{{:a 1 :b 2}
                        {:a 2 :b 3}}})
       (h.html5/page {}))
  (do h)
  (do ppp)
  '_)
