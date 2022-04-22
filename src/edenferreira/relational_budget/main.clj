(ns edenferreira.relational-budget.main
  (:require [edenferreira.relational-budget.integrity :as integrity]
            [br.com.relational-budget :as-alias orc]
            [br.com.relational-budget.budget :as-alias budget]
            [br.com.relational-budget.category :as-alias category]
            [br.com.relational-budget.account :as-alias account]
            [br.com.relational-budget.entry :as-alias entry]
            [edenferreira.relational-budget.api :as api]
            [edenferreira.relational-budget.main :as main]))

(defonce db
  (atom {}))

;; The db is def only once but the validator
;; is always set again to allow for fater
;; feedback cycles
(set-validator! db
                (every-pred
                 integrity/entry-must-have-existing-account
                 integrity/entry-must-have-existing-category
                 integrity/entry-must-have-existing-budget
                 integrity/budget-name-must-be-unique
                 integrity/category-name-must-be-unique
                 integrity/account-name-must-be-unique))

(defn create-budget [& {:as m}]
  (swap! db api/create-budget m))

(defn create-category [& {:as m}]
  (swap! db api/create-category m))

(defn create-account [& {:as m}]
  (swap! db api/create-account m))

(defn add-entry [& {:as m}]
  (swap! db api/add-entry m))


(comment
  (swap! db empty)
  (require '[edenferreira.rawd.persistence :as rawd.persitence]
           '[edenferreira.rawd.instant :as instant])
  (do
    (create-budget
     :id (random-uuid)
     :name "some budget"
     :as-of (instant/parse "2000-01-01T00:00:00Z"))
    (create-category
     :id (random-uuid)
     :name "category name"
     :as-of (instant/parse "2000-01-01T00:00:00Z"))
    (create-account
     :id (random-uuid)
     :name "account name"
     :type "checking"
     :initial-balance 1000M
     :as-of (instant/parse "2000-01-01T00:00:00Z"))
    (add-entry
     :id (random-uuid)
     :type ::entry/credit
     :amount 100M
     :other-party "merchant"
     :budget "some budget"
     :category "category name"
     :account "account name"
     :as-of (instant/parse "2000-01-01T00:00:00Z")))
  (integrity/category-name-must-be-unique @db)
  (def path "./persisted.edn")
  ((requiring-resolve `edenferreira.rawd.persistence/persist!)
   @main/db
   path)
  (reset! main/db
          ((requiring-resolve `edenferreira.rawd.persistence/read!)
           path))
  @main/db
  '_)
