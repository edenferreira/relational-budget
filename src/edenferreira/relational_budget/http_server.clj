(ns edenferreira.relational-budget.http-server
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.content-negotiation :as conneg]
            [io.pedestal.http.body-params :as body-params]
            [br.com.relational-budget :as-alias orc]
            [br.com.relational-budget.budget :as-alias budget]
            [br.com.relational-budget.category :as-alias category]
            [br.com.relational-budget.account :as-alias account]
            [br.com.relational-budget.entry :as-alias entry]
            [clojure.data.json :as json]
            [edenferreira.rawd.api :as rawd]
            [edenferreira.relational-budget.main :as main]
            [edenferreira.relational-budget.derived-relations :as derived-rels])
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
                :type type
                :initial-balance (or (bigdec initial-balance) 0M)
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
           updated-response (-> response
                                (assoc-in [:headers "Content-Type"] accepted)
                                (assoc :body coerced-body))]
       (assoc context :response updated-response)))})

(defn get-state! []
  (let [state @main/db]
    (-> state
        (update ::orc/entries (partial sort-by ::entry/when))
        (update ::orc/accounts (partial sort-by ::account/created-at))
        (update ::orc/categories (partial sort-by ::category/created-at))
        (update ::orc/budgets (partial sort-by ::budget/created-at))
        (assoc ::orc/accounts-with-balances
               (sort-by ::account/created-at
                        (derived-rels/accounts-with-balances
                         (::orc/accounts state)
                         (::orc/entries state)))
               ::orc/categories-with-balances
               (sort-by ::category/created-at
                        (derived-rels/categories-with-balances
                         (::orc/categories state)
                         (::orc/entries state)))
               ::orc/budgets-with-balances
               (sort-by ::budget/created-at
                        (derived-rels/budgets-with-balances
                         (::orc/budgets state)
                         (::orc/entries state)))
               ::orc/accounts-balances-by-days
               (sort-by ::account/day
                        (derived-rels/accounts-balances-by-days
                         (::orc/accounts state)
                         (::orc/entries state)))
               ::orc/entries-balances-by-days
               (sort-by ::entry/day
                        (derived-rels/entries-balances-by-days
                         (::orc/entries state)))))))

(def routes
  (route/expand-routes
   (rawd/routes [(body-params/body-params) coerce-body content-neg-intc]
                get-state!
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
  (get-state!)
  (start-dev)
  (restart)
  (rawd/entities->forms entities)
  (route/try-routing-for routes :prefix-tree "/greet" :get))