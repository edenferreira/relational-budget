(ns edenferreira.orcamento.api-test
  (:require [clojure.test :refer [deftest testing is]]
            [br.com.orcamento :as-alias orc]
            [br.com.orcamento.budget :as-alias budget]
            [br.com.orcamento.category :as-alias category]
            [br.com.orcamento.account :as-alias account]
            [edenferreira.orcamento.api :as api]
            [matcher-combinators.test :refer [match?]]))

(deftest create-budget
  (is (match?
       {::orc/budgets
        #{#::budget{:name "some budget"
                    :created-at #inst "2000-01-01T00:00:00Z"}}}
       (api/create-budget
        "some budget"
        #inst "2000-01-01T00:00:00Z"
        {}))))

(deftest create-category
  (is (match?
       {::orc/categories
        #{#::category{:name "category name"
                      :created-at #inst "2000-01-01T00:00:00Z"}}}
       (api/create-category
        "category name"
        #inst "2000-01-01T00:00:00Z"
        {}))))

(deftest create-account
  (is (match?
        {::orc/accounts
         #{#::account{:name "account name"
                      :type ::account/checking
                      :created-at #inst "2000-01-01T00:00:00Z"}}}
        (api/create-account
         "account name"
         ::account/checking
         #inst "2000-01-01T00:00:00Z"
         {}))))


(comment
  (edev/e-la-vamos-nos)
  )
