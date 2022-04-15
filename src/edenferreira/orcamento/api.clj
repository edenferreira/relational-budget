(ns edenferreira.orcamento.api
  (:require [br.com.orcamento :as-alias orc]
            [br.com.orcamento.budget :as-alias budget]
            [br.com.orcamento.entry :as-alias entry]))

(defn create-budget [budget-name as-of db]
  (update db ::orc/budgets
          (fnil conj #{})
          #::budget{:name budget-name
                    :created-at as-of}))
