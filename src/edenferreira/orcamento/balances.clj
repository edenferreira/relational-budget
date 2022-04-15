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

(defn calculate
  [& {:keys [initial-balance query-rels entries rels]
      :or {initial-balance 0M}}]
  (let [joined (reduce set/join entries rels)]
    (reduce
     (fn [balance {::entry/keys [amount type]}]
       (case type
         ::entry/credit (- balance amount)
         ::entry/debit (+ balance amount)))
     initial-balance
     (set/join query-rels joined))))

(defn account-initial-balance [accounts]
  (reduce
   (fn [balance {::account/keys [initial-balance]}]
     (+ balance initial-balance))
   0M
   accounts))

(defn account [& {:keys [name accounts entries]}]
  (calculate
   :initial-balance (account-initial-balance
                     (set/select (comp #{name} ::account/name)
                                 accounts))
   :query-rels #{{::account/name name}}
   :entries entries
   :rels [accounts]))

(defn category [& {:keys [name categories entries]}]
  (calculate
   :query-rels #{{::category/name name}}
   :entries entries
   :rels [categories]))

(defn budget [& {:keys [name budgets entries]}]
  (calculate
   :query-rels #{{::budget/name name}}
   :entries entries
   :rels [budgets]))

(comment)
