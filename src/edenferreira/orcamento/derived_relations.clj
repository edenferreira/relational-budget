(ns edenferreira.orcamento.derived-relations
  (:require [br.com.orcamento :as-alias orc]
            [br.com.orcamento.budget :as-alias budget]
            [br.com.orcamento.category :as-alias category]
            [br.com.orcamento.account :as-alias account]
            [br.com.orcamento.entry :as-alias entry]
            [clojure.set :as set]
            [edenferreira.orcamento.logic :as logic]
            [edenferreira.clojure.set.extensions :as set.ext]))

(defn accounts-with-balances [accounts entries]
  (let [account-with-entries (set/join accounts entries)]
    (set.ext/project-away
     (set/union
      (set.ext/extend (set/difference
                       (set/project
                        accounts
                        [::account/id
                         ::account/name
                         ::account/created-at
                         ::account/initial-balance])
                       (set/project
                        account-with-entries
                        [::account/id
                         ::account/name
                         ::account/created-at
                         ::account/initial-balance]))
        ::account/balance ::account/initial-balance)
      (set.ext/extend (set.ext/summarize
                        account-with-entries
                       [::account/id
                        ::account/name
                        ::account/created-at
                        ::account/initial-balance]
                       ::account/balance
                       #(reduce logic/updated-balance-from-entry 0M %))
        ::account/balance #(+ (::account/balance %)
                              (::account/initial-balance %))))
     [::account/initial-balance])))

(defn categories-with-balances [categories entries]
  (set.ext/summarize
   (set/join categories entries)
   [::category/id
    ::category/name
    ::category/created-at]
   ::category/balance #(reduce logic/updated-balance-from-entry 0M %)))

(defn budgets-with-balances [budgets entries]
  (set.ext/summarize
   (set/join budgets entries)
   [::budget/id
    ::budget/name
    ::budget/created-at]
   ::budget/balance #(reduce logic/updated-balance-from-entry 0M %)))

(defn entries-on-days [days entries]
  (set/join days
            (set.ext/extend entries
              ::entry/day
              logic/entry-when->day)))

(defn entries-balances-by-days [entries]
  (set.ext/summarize
   (set.ext/extend entries
     ::entry/day
     logic/entry-when->day)
   [::entry/day
    ::budget/name]
   :day/balance #(reduce logic/updated-balance-from-entry 0M %)))

(defn accounts-balances-by-days [accounts entries]
  (set/rename
   (set.ext/extend
       (set.ext/summarize
        (set.ext/extend (set/join accounts entries)
          ::entry/day
          logic/entry-when->day)
        [::entry/day
         ::account/initial-balance
         ::account/name
         ::budget/name]
        ::account/balance-for-the-day #(reduce logic/updated-balance-from-entry 0M %))
     ::account/balance-for-the-day #(+ (::account/balance-for-the-day %)
                                       (::account/initial-balance %)))
     {::entry/day ::account/day}))

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
  (entries-balances-by-days (::orc/entries @edenferreira.orcamento.main/db))
  (accounts-balances-by-days (::orc/accounts @edenferreira.orcamento.main/db)
                             (::orc/entries @edenferreira.orcamento.main/db))

  '_)
