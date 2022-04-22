(ns edenferreira.relational-budget.http-server
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.content-negotiation :as conneg]
            [io.pedestal.http.body-params :as body-params]
            [br.com.relational-budget :as-alias rebu]
            [br.com.relational-budget.budget :as-alias budget]
            [br.com.relational-budget.category :as-alias category]
            [br.com.relational-budget.account :as-alias account]
            [br.com.relational-budget.entry :as-alias entry]
            [br.com.relational-budget.assignment :as-alias assignment]
            [clojure.data.json :as json]
            [edenferreira.rawd.api :as rawd]
            [edenferreira.relational-budget.main :as main]
            [edenferreira.relational-budget.derived-relations :as derived-rels]
            [clojure.set :as set]
            [edenferreira.rawd.instant :as instant])
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
   {:name :assigment
    :attributes {:category {:type "text"}
                 :amount {:type "number"}}
    :adapter (fn [{:keys [category amount]}]
               {:id (random-uuid)
                :category category
                :amount (bigdec amount)
                :as-of (Instant/now)})
    :handler (make-handler-catch-invalid-state main/create-assignment)}
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

(defn get-state! [ & {:keys [as-of]}]
  (let [state (if (doto as-of println)
                (-> @main/db
                    (update ::rebu/entries (partial set/select #(.isBefore (::entry/when %) as-of)))
                    (update ::rebu/accounts (partial set/select #(.isBefore (::account/created-at %)
                                                                            as-of)))
                    (update ::rebu/categories
                            (partial set/select #(.isBefore (::category/created-at %)
                                                            as-of)))
                    (update ::rebu/budgets
                            (partial set/select #(.isBefore (::budget/created-at %) as-of)))
                    (update ::rebu/assignments
                            (partial set/select #(.isBefore (::assignment/created-at %) as-of))))
                @main/db)]

    (-> state
        (update ::rebu/entries (partial sort-by ::entry/when))
        (update ::rebu/accounts (partial sort-by ::account/created-at))
        (update ::rebu/categories (partial sort-by ::category/created-at))
        (update ::rebu/budgets (partial sort-by ::budget/created-at))
        (update ::rebu/assigments (partial sort-by ::assignment/created-at))
        (assoc ::rebu/accounts-with-balances
               (sort-by ::account/created-at
                        (derived-rels/accounts-with-balances
                         (::rebu/accounts state)
                         (::rebu/entries state)))
               ::rebu/categories-with-balances
               (sort-by ::category/created-at
                        (derived-rels/categories-with-balances
                         (::rebu/categories state)
                         (::rebu/assignments state)
                         (::rebu/entries state)))
               ::rebu/budgets-with-balances
               (sort-by ::budget/created-at
                        (derived-rels/budgets-with-balances
                         (::rebu/budgets state)
                         (::rebu/entries state)))
               ::rebu/accounts-balances-by-days
               (sort-by ::account/day
                        (derived-rels/accounts-balances-by-days
                         (::rebu/accounts state)
                         (::rebu/entries state)))
               ::rebu/entries-balances-by-days
               (sort-by ::entry/day
                        (derived-rels/entries-balances-by-days
                         (::rebu/entries state)))))))

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
  (get-state!
   :as-of (instant/parse "1999-01-03T02:00:00Z"))
(instant/parse "2000-01-03T02:00:00Z")
  (start-dev)
  (restart)
  (rawd/entities->forms entities)
  (route/try-routing-for routes :prefix-tree "/greet" :get))
