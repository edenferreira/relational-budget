(ns edenferreira.relational-budget.derived-relations
  (:require [br.com.relational-budget :as-alias orc]
            [br.com.relational-budget.budget :as-alias budget]
            [br.com.relational-budget.category :as-alias category]
            [br.com.relational-budget.account :as-alias account]
            [br.com.relational-budget.assignment :as-alias assignment]
            [br.com.relational-budget.entry :as-alias entry]
            [clojure.set :as set]
            [edenferreira.relational-budget.logic :as logic]
            [edenferreira.clojure.set.extensions :as set.ext]))

(defn accounts-with-balances [accounts entries]
  (let [account-with-entries (set.ext/left-join accounts entries)]
    (set.ext/project-away
     (set.ext/extend (set.ext/summarize
                      account-with-entries
                      [::account/id
                       ::account/name
                       ::account/created-at
                       ::account/initial-balance]
                      ::account/balance
                      #(reduce logic/updated-balance-from-entry 0M %))
       ::account/balance #(+ (::account/balance %)
                             (::account/initial-balance %)))
     [::account/initial-balance])))


(defn categories-with-initial-balance [categories assigments]
  (set.ext/summarize
   (set.ext/left-join categories assigments)
   [::category/id
    ::category/name
    ::category/created-at]
   ::category/balance
   #(reduce logic/updated-balance-from-assignment 0M %)))

(defn categories-with-balances [categories assignments entries]
  (let [categories-with-entries (set.ext/left-join
                                 (categories-with-initial-balance categories assignments)
                                 entries)]
    (set.ext/project-away
     (set.ext/extend
         (set.ext/summarize
          categories-with-entries
          [::category/id
           ::category/name
           ::category/created-at
           ::category/balance]
          ::category/entry-balance #(reduce logic/updated-balance-from-entry 0M %))
       ::category/balance #(+ (::category/balance %)
                              (::category/entry-balance %)))
     [::category/entry-balance])))

(defn budgets-with-balances [budgets entries]
  (let [budgets-with-entries (set.ext/left-join budgets entries)]
    (set.ext/summarize
     budgets-with-entries
     [::budget/id
      ::budget/name
      ::budget/created-at]
     ::budget/balance #(reduce logic/updated-balance-from-entry 0M %))))

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
   (::orc/accounts @edenferreira.relational-budget.main/db)
   (::orc/entries @edenferreira.relational-budget.main/db))
  (categories-with-balances
   (::orc/categories @edenferreira.relational-budget.main/db)
   (::orc/entries @edenferreira.relational-budget.main/db))
  (budgets-with-balances
   (::orc/budgets @edenferreira.relational-budget.main/db)
   (::orc/entries @edenferreira.relational-budget.main/db))
  (entries-on-days
   #{{::entry/day (java.time.Instant/parse "2022-04-10T00:00:00Z")}}
   (::orc/entries @edenferreira.relational-budget.main/db))
  (entries-on-days
   #{{::entry/day (java.time.Instant/parse "2022-04-18T00:00:00Z")}}
   (::orc/entries @edenferreira.relational-budget.main/db))
  (entries-balances-by-days (::orc/entries @edenferreira.relational-budget.main/db))
  (accounts-balances-by-days (::orc/accounts @edenferreira.relational-budget.main/db)
                             (::orc/entries @edenferreira.relational-budget.main/db))

  '_)
