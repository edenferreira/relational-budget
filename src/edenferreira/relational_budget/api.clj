(ns edenferreira.relational-budget.api
  (:require [br.com.relational-budget :as-alias rebu]
            [br.com.relational-budget.budget :as-alias budget]
            [br.com.relational-budget.category :as-alias category]
            [br.com.relational-budget.assignment :as-alias assignment]
            [br.com.relational-budget.account :as-alias account]
            [br.com.relational-budget.entry :as-alias entry]))

(defn create-budget [db
                     & {:keys [id name as-of]}]
  (let [budget #::budget{:id id
                         :name name
                         :created-at as-of}]
    (-> db
        (update ::rebu/budgets (fnil conj #{}) budget))))

(defn create-category [db & {:keys [id name as-of]}]
  (let [category
        #::category{:id id
                    :name name
                    :created-at as-of}]
    (-> db
        (update ::rebu/categories (fnil conj #{}) category))))

(defn create-assignment [db & {:keys [id amount category as-of]}]
  (let [assignment
        #::assignment{:id id
                      :amount amount
                      ::category/name category
                      :created-at as-of}]
    (-> db
        (update ::rebu/assignments (fnil conj #{}) assignment))))

(defn create-account [db & {:keys [id name type initial-balance as-of]}]
  (let [account
        #::account{:id id
                   :name name
                   :type type
                   :initial-balance initial-balance
                   :created-at as-of}]
    (-> db
        (update ::rebu/accounts (fnil conj #{}) account))))

(defn add-entry [db
                 & {:keys [id
                           type
                           amount
                           other-party
                           budget
                           category
                           account
                           as-of]}]
  (let [entry #::entry{:id id
                       :type type
                       :amount amount
                       :other-party other-party
                       :when as-of
                       ::budget/name budget
                       ::category/name category
                       ::account/name account}]
    (-> db
        (update ::rebu/entries (fnil conj #{}) entry))))
