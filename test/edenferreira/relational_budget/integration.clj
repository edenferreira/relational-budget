(ns edenferreira.relational-budget.integration
  (:require [clojure.test :refer [deftest testing is]]
            [br.com.relational-budget :as-alias orc]
            [br.com.relational-budget.budget :as-alias budget]
            [br.com.relational-budget.category :as-alias category]
            [br.com.relational-budget.account :as-alias account]
            [br.com.relational-budget.entry :as-alias entry]
            [matcher-combinators.test :refer [match?]]
            [matcher-combinators.matchers :as m]
            [edenferreira.relational-budget.api :as api]))

(def as-of #inst "1900-01-01T00:00:00Z")

(deftest integration
  (let [db (-> {}
               (api/create-budget :name "my budget"
                                  :as-of as-of)
               (api/create-category :name "groceries"
                                    :as-of as-of)
               (api/create-account :name "banbank"
                                   :type ::account/checking
                                   :initial-balance 100M
                                   :as-of as-of)
               (api/add-entry :type ::entry/credit
                              :amount 10.50M
                              :other-paty "Farmers Market"
                              :budget "my budget"
                              :category "groceries"
                              :account "banbank"
                              :as-of as-of))]
    (is (match?
         (m/match-with [map? m/equals]
          #::orc{:budgets
                 #{#::budget{:name "my budget"
                             :created-at as-of}}
                 :categories
                 #{#::category{:name "groceries"
                               :created-at as-of}}
                 :accounts
                 #{#::account{:name "banbank"
                              :type ::account/checking
                              :initial-balance 100M
                              :created-at as-of}}
                 :entries
                 #{#::entry{:type ::entry/credit
                            :amount 10.50M
                            :other-party nil
                            :when as-of
                            ::budget/name "my budget"
                            ::category/name "groceries"
                            ::account/name "banbank"}}})
         db))))

(comment
  )
