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
            [edenferreira.rawd :as-alias rwd]
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

;; TODO add filter relation
(def definition
  {::rwd/entities
   #{#::rwd{:entity :account
            :adapter (fn [{:keys [name type initial-balance]}]
                       {:id (random-uuid)
                        :name name
                        :type type
                        :initial-balance (or (bigdec initial-balance) 0M)
                        :as-of (Instant/now)})
            :handler (make-handler-catch-invalid-state main/create-account)}
     #::rwd{:entity :category
            :adapter (fn [{:keys [name]}]
                       {:id (random-uuid)
                        :name name
                        :as-of (Instant/now)})
            :handler (make-handler-catch-invalid-state main/create-category)}
     #::rwd{:entity :entry
            :adapter (fn
                       [{:keys [amount type other-party budget account category]}]
                       {:id (random-uuid)
                        :amount (bigdec amount)
                        :type (case
                                  type
                                "credit"
                                :br.com.relational-budget.entry/credit
                                "debit"
                                :br.com.relational-budget.entry/debit)
                        :other-party other-party
                        :as-of (Instant/now)
                        :budget budget
                        :account account
                        :category category})
            :handler (make-handler-catch-invalid-state main/add-entry)}
     #::rwd{:entity :budget
            :adapter (fn [{:keys [name]}]
                       {:id (random-uuid)
                        :name name
                        :as-of (Instant/now)})
            :handler (make-handler-catch-invalid-state main/create-budget)}
     #::rwd{:entity :assigment
            :adapter (fn [{:keys [category amount]}]
                       {:id (random-uuid)
                        :category category
                        :amount (bigdec amount)
                        :as-of (Instant/now)})
            :handler (make-handler-catch-invalid-state main/create-assignment)}}
   ::rwd/attributes
   #{#::rwd{:attribute :entry-budget
            :type "text"
            :entity :entry}
     #::rwd{:attribute :assigment-category
            :type "text"
            :entity :assigment}
     #::rwd{:attribute :entry-account
            :type "text"
            :entity :entry}
     #::rwd{:attribute :entry-amount
            :type "number"
            :entity :entry}
     #::rwd{:attribute :category-name
            :type "text"
            :entity :category}
     #::rwd{:attribute :entry-type
            :type "select"
            :entity :entry
            :select-options ["credit" "debit"]}
     #::rwd{:attribute :assigment-amount
            :type "number"
            :entity :assigment}
     #::rwd{:attribute :account-name
            :type "text"
            :entity :account}
     #::rwd{:attribute :budget-name
            :type "text"
            :entity :budget}
     #::rwd{:attribute :account-type
            :type "select"
            :entity :account
            :select-options ["checking" "savings"]}
     #::rwd{:attribute :account-initial-balance
            :type "number"
            :entity :account}
     #::rwd{:attribute :entry-other-party
            :type "text"
            :entity :entry}
     #::rwd{:attribute :entry-category
            :type "text"
            :entity :entry}}
   ::rwd/filters
   #{#::rwd{:attribute :account-name
            :adapter (fn [& {:keys [account-name]}]
                       {::account/name account-name})}}})

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

(defn get-state! [ & {:keys [as-of]
                      account-name ::account/name
                      :as i}]
  (let [state (if as-of
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
                @main/db)
        state (cond-> state
                account-name (update ::rebu/accounts
                                     (partial set/join #{{::account/name account-name}})))]
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
                definition)))

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
  (get-state! :as-of (instant/parse "2020-01-03T02:00:00Z"))

  (edenferreira.relational-budget.dev/start!)

  (do
    (require 'edev)
    (edev/e-la-vamos-nos)
    (def p (portal/open))
    (start-dev))

  (do
    (portal/clear)
    (restart))

  '_)
