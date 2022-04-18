(ns edenferreira.orcamento.dev
  (:require [edenferreira.orcamento.main :as main]
            [edenferreira.rawd.persistence :as rawd.persitence]
            [br.com.orcamento :as-alias orc]
            [br.com.orcamento.budget :as-alias budget]
            [br.com.orcamento.category :as-alias category]
            [br.com.orcamento.account :as-alias account]
            [br.com.orcamento.entry :as-alias entry]
            [edenferreira.rawd.instant :as instant])
  (:import [java.time Instant]
           [java.time.temporal ChronoUnit]))

(def original-now (instant/parse "2000-01-01T08:00:00Z"))

(def now (atom original-now))

(def tick ChronoUnit/HOURS)

(defn tick-forward! []
  (swap! now #(.plus % 7 tick)))

(comment
  (swap! main/db empty)
  (do "base"
      (main/create-budget
       :id (random-uuid)
       :name "my budget"
       :as-of @now)
      (main/create-category
       :id (random-uuid)
       :name "salary"
       :as-of (tick-forward!))
      (main/create-category
       :id (random-uuid)
       :name "groceries"
       :as-of (tick-forward!))
      (main/create-category
       :id (random-uuid)
       :name "rent"
       :as-of (tick-forward!))
      (main/create-account
       :id (random-uuid)
       :name "nu"
       :type "checking"
       :initial-balance 678M
       :as-of (tick-forward!))
      (main/create-account
       :id (random-uuid)
       :name "tau"
       :type "savings"
       :initial-balance 0M
       :as-of (tick-forward!)))
  (do
    (main/add-entry
     :id (random-uuid)
     :type ::entry/debit
     :amount 1000M
     :other-party "my employeer"
     :budget "my budget"
     :account "nu"
     :category "salary"
     :as-of (tick-forward!))
    (main/add-entry
     :id (random-uuid)
     :type ::entry/credit
     :amount 59M
     :other-party "eskina"
     :budget "my budget"
     :account "nu"
     :category "groceries"
     :as-of (tick-forward!))
    (main/add-entry
     :id (random-uuid)
     :type ::entry/credit
     :amount 300M
     :other-party "sr barriga"
     :budget "my budget"
     :account "nu"
     :category "rent"
     :as-of (tick-forward!))
    (main/add-entry
     :id (random-uuid)
     :type ::entry/credit
     :amount 30M
     :other-party "dia"
     :budget "my budget"
     :account "nu"
     :category "groceries"
     :as-of (tick-forward!))
    (let [as-of (tick-forward!)
          amount 200M]
      (main/add-entry
       :id (random-uuid)
       :type ::entry/credit
       :amount amount
       :other-party "tau"
       :budget "my budget"
       :account "nu"
       :category "salary"
       :as-of as-of)
      (main/add-entry
       :id (random-uuid)
       :type ::entry/debit
       :amount amount
       :other-party "nu"
       :budget "my budget"
       :account "tau"
       :category "salary"
       :as-of as-of)))
  ;; template
  #_(main/add-entry
     :id (random-uuid)
     :type #{::entry/debit ::entry/credit}
     :amount 0M
     :other-party ""
     :budget "my budget"
     :account #{"nu" "tau"}
     :category #{"salary" "groceries" "rent"}
     :as-of (tick-forward!))
  @main/db
  '_)
