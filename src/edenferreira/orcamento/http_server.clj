(ns edenferreira.orcamento.http-server
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.content-negotiation :as conneg]
            [io.pedestal.http.body-params :as body-params]
            [clojure.data.json :as json]
            [hiccup.core :as h]
            [hiccup.page :as h.page]
            [hiccup.form :as h.form]
            [clojure.string :as str]))

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
     (h.form/form-to
      {:enctype "application/x-www-form-urlencoded"} [:post "/creating"]
      (h.form/label "account" "Account:")
      (h.form/text-field {:class ""
                          :id "name"
                          :placeholder "Enter a name here"}
                         "account")
      (h.form/submit-button {} "Create Account"))
     (h.form/form-to
      {:enctype "application/x-www-form-urlencoded"} [:post "/creating"]
      (h.form/label "category" "Category:")
      (h.form/text-field {:class ""
                          :id "name"
                          :placeholder "Enter a name here"}
                         "category")
      (h.form/submit-button {} "Create Category"))
     (h.form/form-to
      {:enctype "application/x-www-form-urlencoded"} [:post "/creating"]
      (h.form/label "budget" "Budget:")
      (h.form/text-field {:class ""
                          :id "name"
                          :placeholder "Enter a name here"}
                         "budget")
      (h.form/submit-button {} "Create Budget"))
     (h.form/form-to
      {:enctype "application/x-www-form-urlencoded"} [:post "/creating"]
      (h.form/label "entry" "Entry:")
      [:div (h.form/label "amount" "Amount:")
       (h.form/text-field {:class ""
                                :id "amount"
                                :placeholder "Enter a name here"}
                               "amount")]
      [:div (h.form/label "type" "Type:")
       (h.form/text-field {:class ""
                                :id "type"
                                :placeholder "Enter a name here"}
                               "type")]
      [:div (h.form/label "other-party" "Other Party:")
       (h.form/text-field {:class ""
                                :id "other-party"
                                :placeholder "Enter a name here"}
                               "other-party")]
      [:div (h.form/label "when" "When:")
       (h.form/text-field {:class ""
                                :id "when"
                                :placeholder "Enter a name here"}
                               "when")]
      [:div (h.form/label "account" "Account:")
       (h.form/text-field {:class ""
                                :id "name"
                                :placeholder "Enter a name here"}
                               "account")]
      [:div (h.form/label "category" "Category:")
       (h.form/text-field {:class ""
                                  :id "name"
                                  :placeholder "Enter a name here"}
                                 "category")]
      [:div (h.form/label "budget" "Budget:")
       (h.form/text-field {:class ""
                                :id "name"
                                :placeholder "Enter a name here"}
                               "budget")]
      (h.form/submit-button {} "Create Entry"))
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

(def routes
  (route/expand-routes
   #{["/index.html" :get [(body-params/body-params) coerce-body content-neg-intc respond-index] :route-name :index]
     ["/hello-world" :get [(body-params/body-params) coerce-body content-neg-intc respond-hello] :route-name :hello-world]
     ["/creating" :post [(body-params/body-params) coerce-body content-neg-intc respond-creating] :route-name :creating]}))

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
  (route/try-routing-for routes :prefix-tree "/greet" :get))
