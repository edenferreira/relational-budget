(ns edenferreira.relational-budget.api
  (:require [br.com.relational-budget :as-alias orc]
            [br.com.relational-budget.budget :as-alias budget]
            [br.com.relational-budget.category :as-alias category]
            [br.com.relational-budget.account :as-alias account]
            [br.com.relational-budget.entry :as-alias entry]))

(defn create-budget [db
                     & {:keys [id name as-of]}]
  (let [budget #::budget{:id id
                         :name name
                         :created-at as-of}]
    (-> db
        (update ::orc/budgets (fnil conj #{}) budget))))

(defn create-category [db & {:keys [id name as-of]}]
  (let [category
        #::category{:id id
                    :name name
                    :created-at as-of}]
    (-> db
        (update ::orc/categories (fnil conj #{}) category))))

(defn create-account [db & {:keys [id name type initial-balance as-of]}]
  (let [account
        #::account{:id id
                   :name name
                   :type type
                   :initial-balance initial-balance
                   :created-at as-of}]
    (-> db
        (update ::orc/accounts (fnil conj #{}) account))))

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
        (update ::orc/entries (fnil conj #{}) entry))))
