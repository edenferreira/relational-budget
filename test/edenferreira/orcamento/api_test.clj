(ns edenferreira.orcamento.api-test
  (:require [clojure.test :refer [deftest testing is]]
            [br.com.orcamento :as-alias orc]
            [br.com.orcamento.budget :as-alias budget]
            [edenferreira.orcamento.api :as api]))

(deftest create-budget
  (is (= {::orc/budgets
           #{#::budget{:name "some budget"
                       :created-at #inst "2000-01-01T00:00:00Z"}}}
         (api/create-budget
          "some budget"
          #inst "2000-01-01T00:00:00Z"
          {}))))


(comment
  )
