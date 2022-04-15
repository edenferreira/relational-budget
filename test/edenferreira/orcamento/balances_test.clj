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
                                 :initial-balance 0M}}))))

(comment
  )
