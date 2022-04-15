(ns edenferreira.orcamento.balances
  (:require [br.com.orcamento :as-alias orc]
            [br.com.orcamento.budget :as-alias budget]
            [br.com.orcamento.category :as-alias category]
            [br.com.orcamento.account :as-alias account]
            [clojure.set :as set]))

(defn account [& {:keys [name accounts]}]
  (reduce
   (fn [balance {::account/keys [initial-balance]}]
     (+ balance initial-balance))
   0M
   (set/select
    (fn [{account-name ::account/name}]
      (= account-name name))
    accounts)))
