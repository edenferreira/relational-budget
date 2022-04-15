(ns edenferreira.orcamento.api
  (:require [br.com.orcamento :as-alias orc]
            [br.com.orcamento.budget :as-alias budget]
            [br.com.orcamento.category :as-alias category]
            [br.com.orcamento.account :as-alias account]
            [br.com.orcamento.entry :as-alias entry]))

(defn create-budget [db
                     & {:keys [name as-of]}]
  (update db ::orc/budgets
          (fnil conj #{})
          #::budget{:name name
                    :created-at as-of}))

(defn create-category [db & {:keys [name as-of]}]
  (update db ::orc/categories
          (fnil conj #{})
          #::category{:name name
                      :created-at as-of}))

(defn create-account [db & {:keys [name type initial-balance as-of]}]
  (update db ::orc/accounts
          (fnil conj #{})
          #::account{:name name
                     :type type
                     :initial-balance initial-balance
                     :created-at as-of}))

(defn add-entry [db
                 & {:keys [type
                           amount
                           other-party
                           budget
                           category
                           account
                           as-of]}]
  (update db ::orc/entries
          (fnil conj #{})
          #::entry{:type type
                   :amount amount
                   :other-party other-party
                   :when as-of
                   ::budget/name budget
                   ::category/name category
                   ::account/name account}))
