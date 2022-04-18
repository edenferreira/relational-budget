(ns edenferreira.orcamento.derived-relations
  (:require [br.com.orcamento :as-alias orc]
            [br.com.orcamento.budget :as-alias budget]
            [br.com.orcamento.category :as-alias category]
            [br.com.orcamento.account :as-alias account]
            [br.com.orcamento.entry :as-alias entry]
            [clojure.set :as set]
            [edenferreira.orcamento.logic :as logic]
            [edenferreira.relational-algebra.extensions :as rel]))

(defn accounts-with-balances [accounts entries]
  (rel/extend
   ::account/balance
    #(+ (::account/balance %)
        (::account/initial-balance %))
    (rel/summarize
     [::account/id
      ::account/name
      ::account/created-at
      ::account/initial-balance]
     (logic/reduce-to-key ::account/balance
                          logic/updated-balance-from-entry
                          0M)
     (set/join accounts entries))))

(defn categories-with-balances [categories entries]
  (rel/summarize
   [::category/id
    ::category/name
    ::category/created-at]
   (logic/reduce-to-key ::category/balance
                        logic/updated-balance-from-entry
                        0M)
   (set/join categories entries)))

(defn budgets-with-balances [budgets entries]
  (rel/summarize
   [::budget/id
    ::budget/name
    ::budget/created-at]
   (logic/reduce-to-key ::budget/balance
                        logic/updated-balance-from-entry
                        0M)
   (set/join budgets entries)))

(defn entries-on-days [days entries]
  (set/join days
            (rel/extend
             ::entry/day
              logic/entry-when->day
              entries)))

(comment
  (accounts-with-balances
   (::orc/accounts @edenferreira.orcamento.main/db)
   (::orc/entries @edenferreira.orcamento.main/db))
  (categories-with-balances
   (::orc/categories @edenferreira.orcamento.main/db)
   (::orc/entries @edenferreira.orcamento.main/db))
  (budgets-with-balances
   (::orc/budgets @edenferreira.orcamento.main/db)
   (::orc/entries @edenferreira.orcamento.main/db))
  (entries-on-days
   #{{::entry/day (java.time.Instant/parse "2022-04-10T00:00:00Z")}}
   (::orc/entries @edenferreira.orcamento.main/db))
  (entries-on-days
   #{{::entry/day (java.time.Instant/parse "2022-04-18T00:00:00Z")}}
   (::orc/entries @edenferreira.orcamento.main/db))
  '_)
