(ns edenferreira.relational-budget.derived-relations
  (:require [br.com.relational-budget :as-alias rebu]
            [br.com.relational-budget.budget :as-alias budget]
            [br.com.relational-budget.category :as-alias category]
            [br.com.relational-budget.account :as-alias account]
            [br.com.relational-budget.assignment :as-alias assignment]
            [br.com.relational-budget.entry :as-alias entry]
            [br.com.relational-budget.other-party :as-alias other-party]
            [clojure.set :as set]
            [edenferreira.relational-budget.logic :as logic]
            [edenferreira.clojure.set.extensions :as set.ext]))

(defn accounts-with-balances [accounts entries]
  (set.ext/project-away
   (set.ext/extend (set.ext/summarize
                    (set.ext/left-join accounts entries)
                    [::account/id
                     ::account/name
                     ::account/created-at
                     ::account/initial-balance]
                    ::account/balance #(reduce logic/updated-balance-from-entry 0M %))
     ::account/balance #(+ (::account/balance %)
                           (::account/initial-balance %)))
   [::account/initial-balance]))


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

(defn other-parties-amount-expended [entries]
  (set.ext/project-away
   (set.ext/summarize entries
                      [::entry/other-party]
                      ::other-party/expended #(- (reduce logic/updated-balance-from-entry 0M %)))
   [::entry/amount]))

(comment
  (accounts-with-balances
   (::rebu/accounts @edenferreira.relational-budget.main/db)
   (::rebu/entries @edenferreira.relational-budget.main/db))
  (categories-with-balances
   (::rebu/categories @edenferreira.relational-budget.main/db)
   (::rebu/entries @edenferreira.relational-budget.main/db))
  (budgets-with-balances
   (::rebu/budgets @edenferreira.relational-budget.main/db)
   (::rebu/entries @edenferreira.relational-budget.main/db))
  (entries-on-days
   #{{::entry/day (java.time.Instant/parse "2022-04-10T00:00:00Z")}}
   (::rebu/entries @edenferreira.relational-budget.main/db))
  (entries-on-days
   #{{::entry/day (java.time.Instant/parse "2022-04-18T00:00:00Z")}}
   (::rebu/entries @edenferreira.relational-budget.main/db))
  (entries-balances-by-days (::rebu/entries @edenferreira.relational-budget.main/db))
  (accounts-balances-by-days (::rebu/accounts @edenferreira.relational-budget.main/db)
                             (::rebu/entries @edenferreira.relational-budget.main/db))

  '_)
