(ns edenferreira.orcamento.api-test
  (:require [clojure.test :refer [deftest testing is]]
            [br.com.orcamento :as-alias orc]
            [br.com.orcamento.budget :as-alias budget]
            [br.com.orcamento.category :as-alias category]
            [br.com.orcamento.account :as-alias account]
            [br.com.orcamento.entry :as-alias entry]
            [edenferreira.orcamento.api :as api]
            [matcher-combinators.test :refer [match?]]))

(deftest create-budget
  (is (match?
       {::orc/budgets
        #{#::budget{:name "some budget"
                    :created-at #inst "2000-01-01T00:00:00Z"}}}
       (api/create-budget
        {}
        :name "some budget"
        :as-of #inst "2000-01-01T00:00:00Z"))))

(deftest create-category
  (is (match?
       {::orc/categories
        #{#::category{:name "category name"
                      :created-at #inst "2000-01-01T00:00:00Z"}}}
       (api/create-category
        {}
        :name "category name"
        :as-of #inst "2000-01-01T00:00:00Z"))))

(deftest create-account
  (is (match?
       {::orc/accounts
        #{#::account{:name "account name"
                     :type ::account/checking
                     :balance 1000M
                     :created-at #inst "2000-01-01T00:00:00Z"}}}
       (api/create-account
        {}
        :name "account name"
        :type ::account/checking
        :initial-balance 1000M
        :as-of #inst "2000-01-01T00:00:00Z"))))

(deftest add-entry
  (is (match?
       #::orc{:entries #{#::entry{:type ::entry/credit
                                  :amount 100M
                                  :other-party "merchant"
                                  :budget "name of budget"
                                  :category "name of category"
                                  :account "name of account"
                                  :when #inst "2000-01-01T00:00:00Z"}}}
       (api/add-entry
        #::orc{:budgets #{#::budget{:name "name of budget"}}
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
  (edev/e-la-vamos-nos)
  )
