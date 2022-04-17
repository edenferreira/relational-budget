(ns edenferreira.orcamento.main
  (:require [edenferreira.orcamento.integrity :as integrity]
            [br.com.orcamento :as-alias orc]
            [br.com.orcamento.budget :as-alias budget]
            [br.com.orcamento.category :as-alias category]
            [br.com.orcamento.account :as-alias account]
            [br.com.orcamento.entry :as-alias entry]
            [edenferreira.orcamento.api :as api]))

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
  (create-budget
   :name "some budget"
   :as-of #inst "2000-01-01T00:00:00Z")
  (create-category
   :name "category name"
   :as-of #inst "2000-01-01T00:00:00Z")
  (create-account
   :name "account name"
   :initial-balance 1000M
   :as-of #inst "2000-01-01T00:00:00Z")
  (add-entry
   :type ::entry/credit
   :amount 100M
   :other-party "merchant"
   :budget "some budget"
   :category "category name"
   :account "account name"
   :as-of #inst "2000-01-01T00:00:00Z")
  @db
  '_)
