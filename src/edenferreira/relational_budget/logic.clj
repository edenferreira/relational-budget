(ns edenferreira.relational-budget.logic
  (:require [br.com.relational-budget :as-alias rebu]
            [br.com.relational-budget.budget :as-alias budget]
            [br.com.relational-budget.category :as-alias category]
            [br.com.relational-budget.account :as-alias account]
            [br.com.relational-budget.assignment :as-alias assignment]
            [br.com.relational-budget.entry :as-alias entry])
  (:import [java.time Instant]
           [java.time.temporal ChronoUnit]))

(defn reduce-to-key [k f init]
  (fn [rels]
    {k
     (reduce f init rels)}))

;; TODO safe option that break without type
(defn updated-balance-from-entry
  [balance {::entry/keys [amount type]}]
  (if type
   (case type
     ::entry/credit (- balance amount)
     ::entry/debit (+ balance amount))
    balance))

(defn balance-from-entries [rels]
  (reduce updated-balance-from-entry 0M rels))

(defn updated-balance-from-assignment [sum {::assignment/keys [amount]
                                            :or {amount 0M}}]
  (+ sum amount))

(defn balance-from-assignments [rels]
  (reduce updated-balance-from-assignment 0M rels))

(defn entry-when->day [{::entry/keys [when]}]
  (.truncatedTo ^Instant when ChronoUnit/DAYS))
