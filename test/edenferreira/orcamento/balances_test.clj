(ns edenferreira.orcamento.balances-test
  (:require [clojure.test :refer [deftest testing is]]
            [edenferreira.orcamento.generators :as generators]
            [edenferreira.orcamento.domain :as domain]
            [br.com.orcamento :as-alias orc]
            [br.com.orcamento.budget :as-alias budget]
            [br.com.orcamento.category :as-alias category]
            [br.com.orcamento.account :as-alias account]
            [br.com.orcamento.entry :as-alias entry]
            [edenferreira.orcamento.balances :as balances]
            [clojure.test.check :as test.check]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [matcher-combinators.test :refer [match?]]
            [clojure.alpha.spec :as s]
            [clojure.set :as set]))

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

(declare account-balance-prop
         category-balance-prop
         budget-balance-prop)

(def property
  (prop/for-all
   [db (generators/entire-setup)]
   (for [account (::orc/accounts db)
         :let [account-name (::account/name account)]]
     (decimal?
      (balances/account
       :name account-name
       :accounts (::orc/accounts db)
       :entries (::orc/entries db))))))

(deftest property-based
  (is
   (match? {:pass? true}
           (test.check/quick-check 10 property))))

(comment
  (gen/generate (generators/entire-setup))
  '_)
