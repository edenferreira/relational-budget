(ns edenferreira.orcamento.balances
  (:require [br.com.orcamento :as-alias orc]
            [br.com.orcamento.budget :as-alias budget]
            [br.com.orcamento.category :as-alias category]
            [br.com.orcamento.account :as-alias account]
            [br.com.orcamento.entry :as-alias entry]
            [clojure.set :as set]))

(defn ^:private entries-balance [entries]
  (reduce
   (fn [balance {::entry/keys [amount type]}]
     (case type
       ::entry/credit (- balance amount)
       ::entry/debit (+ balance amount)))
   0M
   entries))

(defn account [& {:keys [name accounts entries]}]
  (+ (reduce
      (fn [balance {::account/keys [initial-balance]}]
        (+ balance initial-balance))
      0M
      (set/select (comp #{name} ::account/name) accounts))
     (entries-balance (set/join accounts entries))))

(defn category [& {:keys [name categories entries]}]
  (entries-balance
   (set/join
    (set/select (comp #{name} ::category/name) categories)
    entries)))

(defn budget [& {:keys [name budgets entries]}]
  (entries-balance
   (set/join
    (set/select (comp #{name} ::budget/name) budgets)
    entries)))

(comment
  )
