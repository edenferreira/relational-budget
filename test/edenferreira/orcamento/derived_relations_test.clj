(ns edenferreira.orcamento.derived-relations-test
  (:require [clojure.test :refer [deftest testing is] :as t]
            [edenferreira.orcamento.generators :as generators]
            [edenferreira.orcamento.domain :as domain]
            [br.com.orcamento :as-alias orc]
            [br.com.orcamento.budget :as-alias budget]
            [br.com.orcamento.category :as-alias category]
            [br.com.orcamento.account :as-alias account]
            [br.com.orcamento.entry :as-alias entry]
            [edenferreira.orcamento.derived-relations :as derived-relations]
            [clojure.test.check :as test.check]
            [clojure.test.check.properties :as prop]
            [matcher-combinators.test :refer [match?]]
            [clojure.set :as set]
            [edenferreira.orcamento.logic :as logic]
            [edenferreira.clojure.set.extensions :as set.ext]
            [clojure.alpha.spec :as s]
            [clojure.alpha.spec.test :as stest]
            )
  (:import [java.time Instant]))

(t/use-fixtures :once #(do (stest/instrument) (%) (stest/unstrument)))

(s/fdef derived-relations/accounts-with-balances
  :args (s/cat :accounts ::orc/accounts
               :entries
               (s/coll-of (s/keys :req [::entry/amount
                                        ::entry/type
                                        ::account/name])
                          :kind set?)))

(deftest accounts-with-balances
  (let [account-id (random-uuid)
        account-id2 (random-uuid)]
    (is (match?
         #{#::account{:id account-id
                      :name "account name"
                      :created-at (Instant/parse "2000-01-01T00:00:00Z")
                      :balance -10M}}
         (derived-relations/accounts-with-balances
          #{#::account{:id account-id
                       :name "account name"
                       :created-at (Instant/parse "2000-01-01T00:00:00Z")
                       :initial-balance 0M}}
          #{#::entry{:amount 10M
                     :type ::entry/credit
                     ::account/name "account name"}})))
    (is (match?
         #{#::account{:id account-id
                      :name "account name"
                      :created-at (Instant/parse "2000-01-01T00:00:00Z")
                      :balance 20M}
           #::account{:id account-id2
                      :name "another account"
                      :created-at (Instant/parse "2000-01-02T00:00:00Z")
                      :balance 5M}}
         (derived-relations/accounts-with-balances
          #{#::account{:id account-id
                       :name "account name"
                       :created-at (Instant/parse "2000-01-01T00:00:00Z")
                       :initial-balance 10M}
            #::account{:id account-id2
                       :name "another account"
                       :created-at (Instant/parse "2000-01-02T00:00:00Z")
                       :initial-balance 5M}}
          #{#::entry{:amount 10M
                     :type ::entry/debit
                     ::account/name "account name"}})))))

(comment
  (clojure.test/run-tests)
  (derived-relations/accounts-with-balances
   #{#::account{:id (random-uuid)
                :name "account name"
                :created-at (Instant/parse "2000-01-01T00:00:00Z")
                :initial-balance 10M}
     #::account{:id (random-uuid)
                :name "another account"
                :created-at (Instant/parse "2000-01-02T00:00:00Z")
                :initial-balance 5M}}
   #{#::entry{:amount 10M
              :type ::entry/debit
              ::account/name "account name"}})
  (derived-relations/accounts-with-balances
   #{#::account{:id (random-uuid)
                :name "account name"
                :created-at (Instant/parse "2000-01-01T00:00:00Z")
                :initial-balance 0M}}
   #{#::entry{:amount 10M
              :type ::entry/credit
              ::account/name "account name"}})
  '_)
