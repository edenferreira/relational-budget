(ns edenferreira.relational-budget.api-test
  (:require [clojure.test :refer [deftest testing is]]
            [edenferreira.relational-budget.domain :as domain]
            [br.com.relational-budget :as-alias rebu]
            [br.com.relational-budget.budget :as-alias budget]
            [br.com.relational-budget.category :as-alias category]
            [br.com.relational-budget.account :as-alias account]
            [br.com.relational-budget.entry :as-alias entry]
            [edenferreira.relational-budget.api :as api]
            [matcher-combinators.test :refer [match?]]
            [matcher-combinators.matchers :as m]))

(deftest create-budget
  (is (match?
       (m/match-with [m/equals]
                     (let [budget #::budget{:name "some budget"
                                            :created-at #inst "2000-01-01T00:00:00Z"}]
                       #::rebu{:budgets #{budget}}))
       (api/create-budget
        {}
        :name "some budget"
        :as-of #inst "2000-01-01T00:00:00Z"))))

(deftest create-category
  (is (match?
       (let [category #::category{:name "category name"
                                  :created-at #inst "2000-01-01T00:00:00Z"}]
         #::rebu{:categories #{category}})
       (api/create-category
        {}
        :name "category name"
        :as-of #inst "2000-01-01T00:00:00Z"))))

(deftest create-account
  (is (match?
       (m/match-with [m/equals]
                     (let [account #::account{:name "account name"
                                              :type ::account/checking
                                              :initial-balance 1000M
                                              :created-at #inst "2000-01-01T00:00:00Z"}]
                       #::rebu{:accounts #{account}}))
       (api/create-account
        {}
        :name "account name"
        :type ::account/checking
        :initial-balance 1000M
        :as-of #inst "2000-01-01T00:00:00Z"))))

(deftest add-entry
  (is (match?
       (m/match-with [m/equals]
                     (let [entry #::entry{:type ::entry/credit
                                          :amount 100M
                                          :other-party "merchant"
                                          :when #inst "2000-01-01T00:00:00Z"
                                          ::budget/name "name of budget"
                                          ::category/name "name of category"
                                          ::account/name "name of account"}]
                       #::rebu{:entries #{entry}}))
       (api/add-entry
        #::rebu{:budgets #{#::budget{:name "name of budget"}}
               :categories #{#::category{:name "name of category"}}
               :accounts #{#::account{:name "name of account"}}}
        :type ::entry/credit
        :amount 100M
        :other-party "merchant"
        :budget "name of budget"
        :category "name of category"
        :account "name of account"
        :as-of #inst "2000-01-01T00:00:00Z"))))


(comment
  (edev/e-la-vamos-nos))
