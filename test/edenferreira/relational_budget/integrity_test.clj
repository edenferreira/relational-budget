(ns edenferreira.relational-budget.integrity-test
  (:require [clojure.test :refer [deftest testing is] :as t]
            [edenferreira.relational-budget.generators :as generators]
            [edenferreira.relational-budget.domain :as domain]
            [br.com.relational-budget :as-alias rebu]
            [br.com.relational-budget.budget :as-alias budget]
            [br.com.relational-budget.category :as-alias category]
            [br.com.relational-budget.account :as-alias account]
            [br.com.relational-budget.assignment :as-alias assignment]
            [br.com.relational-budget.entry :as-alias entry]
            [clojure.test.check :as test.check]
            [matcher-combinators.test :refer [match?]]
            [clojure.set :as set]
            [edenferreira.relational-budget.integrity :as integrity]
            [edenferreira.clojure.set.extensions :as set.ext]
            [clojure.alpha.spec :as s]
            [clojure.alpha.spec.test :as stest]))

(deftest entry-must-have-existing-account
  (is (integrity/entry-must-have-existing-account
       ::rebu/accounts #{}
       ::rebu/entries #{}))

  (is (not
       (integrity/entry-must-have-existing-account
        ::rebu/accounts #{}
        ::rebu/entries #{{::entry/amount 10M
                          ::account/name "unkown account"}})))

  (is (integrity/entry-must-have-existing-account
       ::rebu/accounts #{{::account/name "account"}}
       ::rebu/entries #{{::entry/amount 10M
                         ::account/name "account"}}))

  (is (not
       (integrity/entry-must-have-existing-account
        ::rebu/accounts #{{::account/name "other account"}}
        ::rebu/entries #{{::entry/amount 10M
                          ::account/name "unkown account"}})))

  (is (not
       (integrity/entry-must-have-existing-account
        ::rebu/accounts #{{::account/name "other account"}}
        ::rebu/entries #{{::entry/amount 5M
                          ::account/name "other account"}
                         {::entry/amount 10M
                            ::account/name "unkown account"}}))))
