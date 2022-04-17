(ns edenferreira.orcamento.integrity
  (:require [clojure.set :as set]
            [br.com.orcamento :as-alias orc]
            [br.com.orcamento.budget :as-alias budget]
            [br.com.orcamento.category :as-alias category]
            [br.com.orcamento.account :as-alias account]
            [br.com.orcamento.entry :as-alias entry]))

(defn entry-must-have-existing-account
  [& {::orc/keys [accounts entries]}]
  (or (empty? entries)
      (seq
       (set/intersection
        (set/project entries [::account/name])
        (set/project accounts [::account/name])))))

(defn entry-must-have-existing-category
  [& {::orc/keys [categories entries]}]
  (or (empty? entries)
      (seq
       (set/intersection
        (set/project entries [::category/name])
        (set/project categories [::category/name])))))

(defn entry-must-have-existing-budget
  [& {::orc/keys [budgets entries]}]
  (or (empty? entries)
      (seq
       (set/intersection
        (set/project entries [::budget/name])
        (set/project budgets [::budget/name])))))

(defn budget-name-must-be-unique
  [& {::orc/keys [budgets]}]
  (every? (fn [[_k v]]
            (< (count v) 2))
          (set/index budgets [::budget/name])))

(defn category-name-must-be-unique
  [& {::orc/keys [categories]}]
  (every? (fn [[_k v]]
            (< (count v) 2))
          (set/index categories [::category/name])))

(defn account-name-must-be-unique
  [& {::orc/keys [accounts]}]
  (every? (fn [[_k v]]
            (< (count v) 2))
          (set/index accounts [::account/name])))

(comment
  (entry-must-have-existing-account
   ::orc/accounts #{{::account/name "aabc"}}
   ::orc/entries #{{::account/name "abc"}})
  )
