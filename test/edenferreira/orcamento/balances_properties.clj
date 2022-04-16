(ns edenferreira.orcamento.balances-properties
  (:require [clojure.test :refer [deftest is]]
            [edenferreira.orcamento.generators :as generators]
            [edenferreira.relational-algebra.extensions :as rel]
            [br.com.orcamento :as-alias orc]
            [br.com.orcamento.budget :as-alias budget]
            [br.com.orcamento.category :as-alias category]
            [br.com.orcamento.account :as-alias account]
            [br.com.orcamento.entry :as-alias entry]
            [edenferreira.orcamento.domain :as domain]
            [edenferreira.orcamento.balances :as balances]
            [clojure.test.check :as test.check]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [matcher-combinators.test :refer [match?]]
            [clojure.set :as set]
            [clojure.alpha.spec :as s]))

(def all-credit-entries-always-negative-balance
  (prop/for-all [{::orc/keys [entries accounts]}
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
                   #::orc{:entries entries
                          :accounts accounts})]
                (every? (fn [{::account/keys [name]}]
                          ((some-fn zero? neg?)
                           (balances/account
                            :name name
                            :accounts accounts
                            :entries entries)))
                        accounts)))

(deftest check-all-credit-entries-always-negative-balance
  (is
   (match? {:pass? true}
           (test.check/quick-check
            30
            all-credit-entries-always-negative-balance))))

(def all-debit-entries-always-positive-balance
  (prop/for-all [{::orc/keys [entries accounts]}
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
                   #::orc{:entries entries
                          :accounts accounts})]
                (every? (fn [{::account/keys [name]}]
                          ((some-fn zero? pos?)
                           (balances/account
                            :name name
                            :accounts accounts
                            :entries entries)))
                        accounts)))

(deftest check-all-debit-entries-always-positive-balance
  (is
   (match? {:pass? true}
           (test.check/quick-check
            30
            all-debit-entries-always-positive-balance))))

(comment
  (gen/generate
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
     #::orc{:entries entries
            :accounts accounts}))
  '_)
