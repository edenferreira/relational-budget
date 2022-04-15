(ns edenferreira.orcamento.balances-test
  (:require [clojure.test :refer [deftest testing is]]
            [br.com.orcamento :as-alias orc]
            [br.com.orcamento.budget :as-alias budget]
            [br.com.orcamento.category :as-alias category]
            [br.com.orcamento.account :as-alias account]
            [br.com.orcamento.entry :as-alias entry]
            [edenferreira.orcamento.balances :as balances]
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
  (is (= 0M
         (balances/account
          :name "my account"
          :accounts #{#::account{:name "my account"
                                 :initial-balance 100M}}
          :entries #{#::entry{::account/name "my account"
                              :type ::entry/credit
                              :amount 100M}}))))

(comment
(balances/account
          :name "my account"
          :accounts #{#::account{:name "my account"
                                 :initial-balance 100M}}
          :entries #{#::entry{::account/name "my account"
                              :type ::entry/credit
                              :amount 100M}})
  )
