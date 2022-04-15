(ns edenferreira.orcamento.integration
  (:require [clojure.test :refer [deftest testing is]]))

(deftest integration
  (create-budget)
  (create-category)
  (create-account)
  (add-entry)
  (add-entry)
  (add-entry)
  (get-remaining-money-in-budget)
  (get-money-expand-in-category)
  (get-money-in-account)
  )
