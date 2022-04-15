(ns edenferreira.orcamento.generators
  (:require [clojure.set :as set]
            [br.com.orcamento :as-alias orc]
            [br.com.orcamento.budget :as-alias budget]
            [br.com.orcamento.category :as-alias category]
            [br.com.orcamento.account :as-alias account]
            [br.com.orcamento.entry :as-alias entry]
            [clojure.test.check.generators :as gen]
            [clojure.alpha.spec :as s]))

(declare entries-with-accounts
         entries-with-categories
         entries-with-budgets
         enties-with-X
         accounts
         categories
         budgets
         given-accounts-exist-input-db
         given-categories-exist-input-db
         given-budgets-exist-input-db
         given-X-exist-input-db)

(defn db []
  (gen/fmap
   (fn [[wo-entries w-entries]]
     (apply merge-with set/union
            wo-entries w-entries))
   (gen/tuple
    (gen/hash-map
     ::orc/categories (s/gen ::orc/categories)
     ::orc/budgets (s/gen ::orc/budgets)
     ::orc/accounts (s/gen ::orc/accounts))
    (gen/vector
     (gen/fmap
      (fn [[entries m]]
        (let [entries (into
                       (empty entries)
                       (map
                        #(assoc %
                                ::category/name (::category/name (::orc/category m))
                                ::account/name (::account/name (::orc/account m))
                                ::budget/name (::budget/name (::orc/budget m))))
                       entries)]
          (assoc m
                 ::orc/entries entries
                 ::orc/categories #{(::orc/category m)}
                 ::orc/accounts #{(::orc/account m)}
                 ::orc/budgets #{(::orc/budget m)})))
      (gen/tuple
       (s/gen ::orc/entries)
       (gen/hash-map
        ::orc/category (s/gen
                        (s/select ::orc/category
                                  [*]))
        ::orc/budget (s/gen
                      (s/select ::orc/budget
                                [*]))
        ::orc/account (s/gen
                       (s/select ::orc/account
                                 [*])))))
     2))))

