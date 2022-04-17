(ns edenferreira.orcamento.http-server
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.content-negotiation :as conneg]
            [io.pedestal.http.body-params :as body-params]
            [br.com.orcamento :as-alias orc]
            [br.com.orcamento.budget :as-alias budget]
            [br.com.orcamento.category :as-alias category]
            [br.com.orcamento.account :as-alias account]
            [br.com.orcamento.entry :as-alias entry]
            [clojure.data.json :as json]
            [edenferreira.rawd.api :as rawd]
            [edenferreira.orcamento.main :as main])
  (:import [java.time Instant]))

(defn make-handler-catch-invalid-state [f]
  (fn [input]
    (try
      {:status 200
       :body (f input)}
      (catch java.lang.IllegalStateException _e
        {:status 400
         :body {:invalid-input input}}))))

(def entities
  [{:name :budget
    :attributes {:name {:type "text"}}
    :adapter (fn [{:keys [name]}]
               {:id (random-uuid)
                :name name
                :as-of (Instant/now)})
    :handler (make-handler-catch-invalid-state main/create-budget)}
   {:name :account
    :attributes {:name {:type "text"}
                 :type {:type "select"
                        :options ["checking" "savings"]}
                 :initial-balance {:type "number"}}
    :adapter (fn [{:keys [name type initial-balance]}]
               {:id (random-uuid)
                :name name
                :initial-balance (or initial-balance 0M)
                :as-of (Instant/now)})
    :handler (make-handler-catch-invalid-state main/create-account)}
   {:name :category
    :attributes {:name {:type "text"}}
    :adapter (fn [{:keys [name]}]
               {:id (random-uuid)
                :name name
                :as-of (Instant/now)})
    :handler (make-handler-catch-invalid-state main/create-category)}
   {:name :entry
    :attributes {:amount {:type "number"}
                 :type {:type "select"
                        :options ["credit" "debit"]}
                 :other-party {:type "text"}
                 :budget {:type "text"}
                 :account {:type "text"}
                 :category {:type "text"}}
    :adapter (fn [{:keys [amount type other-party budget account category]}]
               {:id (random-uuid)
                :amount (bigdec amount)
                :type (case type
                        "credit" ::entry/credit
                        "debit" ::entry/debit)
                :other-party other-party
                :as-of (Instant/now)
                :budget budget
                :account account
                :category category})
    :handler (make-handler-catch-invalid-state main/add-entry)}])

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
   (rawd/routes [(body-params/body-params) coerce-body content-neg-intc]
                entities)))

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
  (rawd/entities->forms entities)
  (route/try-routing-for routes :prefix-tree "/greet" :get))
