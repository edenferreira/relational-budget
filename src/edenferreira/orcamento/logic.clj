(ns edenferreira.orcamento.logic
  (:require [br.com.orcamento :as-alias orc]
            [br.com.orcamento.budget :as-alias budget]
            [br.com.orcamento.category :as-alias category]
            [br.com.orcamento.account :as-alias account]
            [br.com.orcamento.assignment :as-alias assignment]
            [br.com.orcamento.entry :as-alias entry])
  (:import [java.time Instant]
           [java.time.temporal ChronoUnit]))

(defn reduce-to-key [k f init]
  (fn [rels]
    {k
     (reduce f init rels)}))

(defn updated-balance-from-entry
  [balance {::entry/keys [amount type]}]
  (if type
   (case type
     ::entry/credit (- balance amount)
     ::entry/debit (+ balance amount))
    balance))

(defn updated-balance-from-assignment [sum {::assignment/keys [amount]
                                            :or {amount 0M}}]
  (+ sum amount))

(defn entry-when->day [{::entry/keys [when]}]
  (.truncatedTo ^Instant when ChronoUnit/DAYS))
