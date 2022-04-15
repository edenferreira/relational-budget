(ns edenferreira.orcamento.integration
  (:require [clojure.test :refer [deftest testing is]]
            [br.com.orcamento :as-alias orc]
            [br.com.orcamento.budget :as-alias budget]
            [br.com.orcamento.category :as-alias category]
            [br.com.orcamento.account :as-alias account]
            [br.com.orcamento.entry :as-alias entry]
            [matcher-combinators.test :refer [match?]]
            [matcher-combinators.matchers :as m]
            [edenferreira.orcamento.api :as api]))

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
                              :balance 100M
                              :created-at as-of}}
                 :entries
                 #{#::entry{:type ::entry/credit
                            :amount 10.50M
                            :other-party nil
                            :budget "my budget"
                            :category "groceries"
                            :account "banbank"
                            :when as-of}}})
         db))))

(comment
  )
