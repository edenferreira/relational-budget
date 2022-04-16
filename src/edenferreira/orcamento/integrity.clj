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

(comment
  (entry-must-have-existing-account
   ::orc/accounts #{{::account/name "aabc"}}
   ::orc/entries #{{::account/name "abc"}})
  )
