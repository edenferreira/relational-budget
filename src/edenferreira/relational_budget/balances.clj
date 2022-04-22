(ns edenferreira.relational-budget.balances
  (:require [br.com.relational-budget :as-alias rebu]
            [br.com.relational-budget.budget :as-alias budget]
            [br.com.relational-budget.category :as-alias category]
            [br.com.relational-budget.account :as-alias account]
            [br.com.relational-budget.entry :as-alias entry]
            [clojure.set :as set]))

(defn updated-balance-from-entry
  [balance {::entry/keys [amount type]}]
  (case type
    ::entry/credit (- balance amount)
    ::entry/debit (+ balance amount)))

(defn account [& {:keys [name accounts entries]}]
  (let [accounts (set/join #{{::account/name name}} accounts)
        initial-balance (reduce
                         (fn [balance {::account/keys [initial-balance]}]
                           (+ balance initial-balance))
                         0M
                         accounts)]
    (reduce updated-balance-from-entry
            initial-balance
            (set/join #{{::account/name name}}
                      entries))))

(defn category [& {:keys [name entries]}]
  (reduce updated-balance-from-entry
          0M
          (set/join #{{::category/name name}}
                    entries)))

(defn budget [& {:keys [name entries]}]
  (reduce updated-balance-from-entry
          0M
          (set/join #{{::budget/name name}}
                    entries)))

(comment
  '_)
