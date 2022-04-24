(ns edenferreira.relational-budget.derived-relations-properties
  (:require [clojure.test :refer [deftest is]]
            [edenferreira.relational-budget.generators :as generators]
            [edenferreira.relational-algebra.extensions :as rel]
            [br.com.relational-budget :as-alias rebu]
            [br.com.relational-budget.budget :as-alias budget]
            [br.com.relational-budget.category :as-alias category]
            [br.com.relational-budget.account :as-alias account]
            [br.com.relational-budget.entry :as-alias entry]
            [edenferreira.relational-budget.derived-relations :as derived-rels]
            [edenferreira.relational-budget.domain :as domain]
            [clojure.test.check :as test.check]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [matcher-combinators.test :refer [match?]]
            [clojure.set :as set]
            [clojure.alpha.spec :as s]))

(def all-credit-entries-always-negative-balance
  (prop/for-all
   [[accounts entries]
    (gen/let [entries (gen/fmap (partial
                                 rel/extend
                                 ::entry/type
                                 (constantly ::entry/credit))
                                (generators/many-entries))
              accounts (gen/fmap
                        (partial
                         rel/extend
                         ::account/initial-balance
                         (constantly 0M))
                        (generators/many-accounts-from-entries entries))]
      [accounts entries])]
   (let [accounts-with-balances
         (derived-rels/accounts-with-balances accounts entries)]
     (every? (fn [{::account/keys [balance]}]
               (or (zero? balance)
                   (neg? balance)))
             accounts-with-balances))))

(deftest check-all-credit-entries-always-negative-balance
  (is
   (match? {:pass? true}
           (test.check/quick-check
            30
            all-credit-entries-always-negative-balance))))

(def all-debit-entries-always-positive-balance
  (prop/for-all
   [[accounts entries]
    (gen/let [entries (gen/fmap (partial
                                 rel/extend
                                 ::entry/type
                                 (constantly ::entry/debit))
                                (generators/many-entries))
              accounts (gen/fmap
                        (partial
                         rel/extend
                         ::account/initial-balance
                         (constantly 0M))
                        (generators/many-accounts-from-entries entries))]
      [accounts entries])]
   (let [accounts-with-balances
         (derived-rels/accounts-with-balances accounts entries)]
     (every? (fn [{::account/keys [balance]}]
               (or (zero? balance)
                   (pos? balance)))
             accounts-with-balances))))

(deftest check-all-debit-entries-always-positive-balance
  (is
   (match? {:pass? true}
           (test.check/quick-check
            30
            all-debit-entries-always-positive-balance))))

(comment
  (gen/generate all-credit-entries-always-negative-balance))
