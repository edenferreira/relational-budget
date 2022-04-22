(ns edenferreira.orcamento.derived-relations-test
  (:require [clojure.test :refer [deftest testing is] :as t]
            [edenferreira.orcamento.generators :as generators]
            [edenferreira.orcamento.domain :as domain]
            [br.com.orcamento :as-alias orc]
            [br.com.orcamento.budget :as-alias budget]
            [br.com.orcamento.category :as-alias category]
            [br.com.orcamento.account :as-alias account]
            [br.com.orcamento.assignment :as-alias assignment]
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

(deftest categories-with-balances
  (let [category-id (random-uuid)
        category-id2 (random-uuid)]
    (is (match?
         #{#::category{:id category-id
                       :name "category name"
                       :created-at (Instant/parse "2000-01-01T00:00:00Z")
                       :balance -10M}}
         (derived-relations/categories-with-balances
          #{#::category{:id category-id
                        :name "category name"
                        :created-at (Instant/parse "2000-01-01T00:00:00Z")}}
          #{}
          #{#::entry{:amount 10M
                     :type ::entry/credit
                     ::category/name "category name"}})))
    (is (match?
         #{#::category{:id category-id
                       :name "category name"
                       :created-at (Instant/parse "2000-01-01T00:00:00Z")
                       :balance 0M}}
         (derived-relations/categories-with-balances
          #{#::category{:id category-id
                        :name "category name"
                        :created-at (Instant/parse "2000-01-01T00:00:00Z")}}
          #{#::assignment{:amount 10M
                          ::category/name "category name"}}
          #{#::entry{:amount 10M
                     :type ::entry/credit
                     ::category/name "category name"}})))
    (is (match?
         #{#::category{:id category-id
                       :name "category name"
                       :created-at (Instant/parse "2000-01-01T00:00:00Z")
                       :balance 500M}
           #::category{:id category-id2
                       :name "another category"
                       :created-at (Instant/parse "2000-01-02T00:00:00Z")
                       :balance 0M}}
         (derived-relations/categories-with-balances
          #{#::category{:id category-id
                        :name "category name"
                        :created-at (Instant/parse "2000-01-01T00:00:00Z")}
            #::category{:id category-id2
                        :name "another category"
                        :created-at (Instant/parse "2000-01-02T00:00:00Z")}}
          #{}
          #{#::entry{:amount 300M
                     :type ::entry/debit
                     ::category/name "category name"}
            #::entry{:amount 200M
                     :type ::entry/debit
                     ::category/name "category name"}})))
    (is (match?
         #{#::category{:id category-id
                       :name "category name"
                       :created-at (Instant/parse "2000-01-01T00:00:00Z")
                       :balance 500M}
           #::category{:id category-id2
                       :name "another category"
                       :created-at (Instant/parse "2000-01-02T00:00:00Z")
                       :balance 100M}}
         (derived-relations/categories-with-balances
          #{#::category{:id category-id
                        :name "category name"
                        :created-at (Instant/parse "2000-01-01T00:00:00Z")}
            #::category{:id category-id2
                        :name "another category"
                        :created-at (Instant/parse "2000-01-02T00:00:00Z")}}
          #{#::assignment{:amount 100M
                          ::category/name "another category"}}
          #{#::entry{:amount 300M
                     :type ::entry/debit
                     ::category/name "category name"}
            #::entry{:amount 200M
                     :type ::entry/debit
                     ::category/name "category name"}})))))

(deftest budgets-with-balances
  (let [budget-id (random-uuid)
        budget-id2 (random-uuid)]
    (is (match?
         #{#::budget{:id budget-id
                     :name "budget name"
                     :created-at (Instant/parse "2000-01-01T00:00:00Z")
                     :balance -10M}}
         (derived-relations/budgets-with-balances
          #{#::budget{:id budget-id
                      :name "budget name"
                      :created-at (Instant/parse "2000-01-01T00:00:00Z")}}
          #{#::entry{:amount 10M
                     :type ::entry/credit
                     ::budget/name "budget name"}})))
    (is (match?
         #{#::budget{:id budget-id
                     :name "budget name"
                     :created-at (Instant/parse "2000-01-01T00:00:00Z")
                     :balance 100M}
           #::budget{:id budget-id2
                     :name "another budget"
                     :created-at (Instant/parse "2000-01-02T00:00:00Z")}}
         (derived-relations/budgets-with-balances
          #{#::budget{:id budget-id
                      :name "budget name"
                      :created-at (Instant/parse "2000-01-01T00:00:00Z")}
            #::budget{:id budget-id2
                      :name "another budget"
                      :created-at (Instant/parse "2000-01-02T00:00:00Z")}}
          #{#::entry{:amount 300M
                     :type ::entry/debit
                     ::budget/name "budget name"}
            #::entry{:amount 200M
                     :type ::entry/credit
                     ::budget/name "budget name"}})))))

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

  (derived-relations/categories-with-balances
   #{#::category{:id (random-uuid)
                 :name "category name"
                 :created-at (Instant/parse "2000-01-01T00:00:00Z")}}
   #{}
   #{#::entry{:amount 10M
              :type ::entry/credit
              ::category/name "category name"}})

  (derived-relations/categories-with-balances
   #{#::category{:id (random-uuid)
                 :name "other category name"
                 :created-at (Instant/parse "2000-01-01T00:00:00Z")}
     #::category{:id (random-uuid)
                 :name "category name"
                 :created-at (Instant/parse "2000-01-01T00:00:00Z")}}
   #{#::assignment{:amount 10M
                   ::category/name "category name"}}
   #{#::entry{:amount 10M
              :type ::entry/credit
              ::category/name "category name"}})
  '_)
