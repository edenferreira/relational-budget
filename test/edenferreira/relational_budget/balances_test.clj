(ns edenferreira.relational-budget.balances-test
  (:require [clojure.test :refer [deftest testing is]]
            [edenferreira.relational-budget.generators :as generators]
            [edenferreira.relational-budget.domain :as domain]
            [br.com.relational-budget :as-alias rebu]
            [br.com.relational-budget.budget :as-alias budget]
            [br.com.relational-budget.category :as-alias category]
            [br.com.relational-budget.account :as-alias account]
            [br.com.relational-budget.entry :as-alias entry]
            [edenferreira.relational-budget.balances :as balances]
            [clojure.test.check :as test.check]
            [clojure.test.check.properties :as prop]
            [matcher-combinators.test :refer [match?]]))

(deftest account-balance
  (is (= 0M
         (balances/account
          :name "my account"
          :accounts #{#::account{:name "my account"
                                 :initial-balance 0M}
                      #::account{:name "my other account"
                                 :initial-balance 100M}})))
  (is (= 10M
         (balances/account
          :name "my account"
          :accounts #{#::account{:name "my account"
                                 :initial-balance 10M}})))
  ;; TODO test entry other category
  (is (= 0M
         (balances/account
          :name "my account"
          :accounts #{#::account{:name "my account"
                                 :initial-balance 100M}}
          :entries #{#::entry{::account/name "my account"
                              :type ::entry/credit
                              :amount 100M}}))))

(deftest category-balance
  (is (= 0M
         (balances/category
          :name "my category"
          :categories #{#::category{:name "my category"}})))
  (is (= -100M
         (balances/category
          :name "my category"
          :categories #{#::category{:name "my category"}}
          :entries #{#::entry{::category/name "my category"
                              :type ::entry/credit
                              :amount 100M}})))
  (is (= 0M
         (balances/category
          :name "my category"
          :categories #{#::category{:name "my category"}}
          :entries #{#::entry{::category/name "my other category"
                              :type ::entry/credit
                              :amount 100M}})))
  (is (= 0M
         (balances/category
          :name "my category"
          :categories #{#::category{:name "my other category"}}
          :entries #{#::entry{::category/name "my other category"
                              :type ::entry/credit
                              :amount 100M}}))))

(deftest budget-balance
  (is (= 0M
         (balances/budget
          :name "my budget"
          :budgets #{#::budget{:name "my budget"}})))
  (is (= 100M
         (balances/budget
          :name "my budget"
          :budgets #{#::budget{:name "my budget"}}
          :entries #{#::entry{::budget/name "my budget"
                              :type ::entry/debit
                              :amount 100M}})))
  (is (= 0M
         (balances/budget
          :name "my budget"
          :budgets #{#::budget{:name "my budget"}}
          :entries #{#::entry{::budget/name "my other budget"
                              :type ::entry/credit
                              :amount 100M}})))
  (is (= 0M
         (balances/budget
          :name "my budget"
          :budgets #{#::budget{:name "my other budget"}}
          :entries #{#::entry{::budget/name "my other budget"
                              :type ::entry/credit
                              :amount 100M}}))))

(comment
  '_)
