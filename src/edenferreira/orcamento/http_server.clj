(ns edenferreira.orcamento.http-server
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.content-negotiation :as conneg]
            [io.pedestal.http.body-params :as body-params]
            [clojure.data.json :as json]
            [hiccup.core :as h]
            [hiccup.page :as h.page]
            [hiccup.form :as h.form]
            [clojure.string :as str]
            [clojure.set :as set]))
(def entities
  [{:name :budget
    :attributes {:name {:type "text"}}}
   {:name :account
    :attributes {:name {:type "text"}}}
   {:name :category
    :attributes {:name {:type "text"}}}
   {:name :entry
    :attributes {:amount {:type "text"}
                 :type {:type "text"}
                 :other-party {:type "text"}
                 :when {:type "date"}
                 :budget {:type "text"}
                 :account {:type "text"}
                 :category {:type "text"}}}])

;; TODO placeholder
(defn create-form-input [& {:keys [id type label name placeholder]}]
  [:li
   [:label {:for id} label]
   [:input (cond-> {:type type
                    :id id
                    :name name}
             placeholder (assoc :placeholder placeholder))]])

(defn create-form [& {:keys [enctype action method items button-label]}]
  [:form {:action action
          :method method
          :enctype enctype}
   [:ul (seq items)]
   [:button button-label]])

(defn entity->form [& {:keys [name attributes]}]
  (create-form :enctype "application/x-www-form-urlencoded"
               :method "POST"
               :action (str "/" (clojure.core/name name) "/create")
               :items (map
                       (fn [[attr {:keys [type]}]]
                         (create-form-input :id (str (clojure.core/name name)
                                                     "-"
                                                     (clojure.core/name attr))
                                            :type type
                                            :label (str (clojure.core/name name)
                                                        " "
                                                        (clojure.core/name attr))
                                            :name (str (clojure.core/name name)
                                                       "-"
                                                       (clojure.core/name attr))))
                       attributes)
               :button-label (str "Create "
                                  (clojure.core/name name))))

(defn entities->forms [entities]
  [:div (map entity->form entities)])

(defn ok [body]
  {:status 200 :body body})

(defn not-found []
  {:status 404 :body "Not found\n"})

(defn index [_]
  (h.page/html5
   {:lang "en"}
   (h/html
    [:div {:class ""}
     [:h1 {:class ""} "Hello Hiccup"]
     (entities->forms entities)
     [:hr]
     [:h1 {:class "text-success"} "Hello World!"]])))

(defn respond-index [request]
  (ok (index request)))

(defn hello-world [{:keys [name]}]
  (h.page/html5
   {:lang "en"}
   (h/html [:p (str "Hello, " name)])))

(defn respond-hello [request]
  (ok (hello-world (get-in request [:query-params]))))

(defn creating [body]
  (h.page/html5
   {:lang "en"}
   (h/html [:p (map #(do [:p (str (first %) " : " (second %))]) body)])))

(defn respond-creating [request]
  (ok (creating (:params request))))

(def supported-types ["text/html" "application/edn" "application/json" "text/plain"])

(def content-neg-intc (conneg/negotiate-content supported-types))

(def coerce-body
  {:name ::coerce-body
   :leave
   (fn [context]
     (let [accepted (get-in context [:request :accept :field] "text/plain")
           response (get context :response)
           body (get response :body)
           coerced-body (case accepted
                          "text/html" body
                          "text/plain" body
                          "application/edn" (pr-str body)
                          "application/json" (json/write-str body))
           updated-response (assoc response
                                   :headers {"Content-Type" accepted}
                                   :body coerced-body)]
       (assoc context :response updated-response)))})

(defn respond-create-entity [entity]
  (fn create-entity [request]
    (ok (assoc (:params request)
               ::entity entity))))

(def routes
  (route/expand-routes
   (set/union
    #{["/index.html" :get [(body-params/body-params) coerce-body content-neg-intc respond-index] :route-name :index]
      ["/hello-world" :get [(body-params/body-params) coerce-body content-neg-intc respond-hello] :route-name :hello-world]
      ["/creating" :post [(body-params/body-params) coerce-body content-neg-intc respond-creating] :route-name :creating]}
    (set
     (map
      (fn [{:keys [name]}]
        [(str "/" (clojure.core/name name) "/create")
         :post [(body-params/body-params) coerce-body content-neg-intc (respond-create-entity name)]
         :route-name (keyword (str "creating-entity-" (clojure.core/name name)))])
      entities)))))

(defn create-server []
  (http/create-server
   {::http/routes routes
    ::http/type :jetty
    ::http/port 8890}))


(def service-map
  {::http/routes routes
   ::http/type :jetty
   ::http/port 8890})

(defn start []
  (http/start (http/create-server service-map)))
 ;; For interactive development
(defonce server (atom nil))

(defn start-dev []
  (reset! server
          (http/start (http/create-server
                       (assoc service-map
                              ::http/join? false)))))

(defn stop-dev []
  (http/stop @server))

(defn restart []
  (stop-dev)
  (start-dev))

(comment
  (start-dev)
  (restart)
  (entities->forms entities)
  (route/try-routing-for routes :prefix-tree "/greet" :get))
