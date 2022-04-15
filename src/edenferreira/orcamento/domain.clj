(ns edenferreira.orcamento.domain
  (:require [clojure.alpha.spec :as s]
            [clojure.string :as str]
            [br.com.orcamento :as-alias orc]
            [br.com.orcamento.budget :as-alias budget]
            [br.com.orcamento.category :as-alias category]
            [br.com.orcamento.account :as-alias account]
            [br.com.orcamento.entry :as-alias entry]))

(s/def ::budget/id uuid?)
(s/def ::budget/name (s/and string? (complement str/blank?)))
(s/def ::budget/created-at inst?)
(s/def ::orc/budget
  (s/schema [::budget/id
             ::budget/name
             ::budget/created-at]))
(s/def ::orc/budgets
  (s/coll-of (s/select ::orc/budget [*])
             :kind set?))

(s/def ::category/id uuid?)
(s/def ::category/name (s/and string? (complement str/blank?)))
(s/def ::category/created-at inst?)
(s/def ::orc/category
  (s/schema [::category/id
             ::category/name
             ::category/created-at]))
(s/def ::orc/categories
  (s/coll-of (s/select ::orc/category [*])
             :kind set?))

(s/def ::account/id uuid?)
(s/def ::account/name (s/and string? (complement str/blank?)))
(s/def ::account/initial-balance decimal?)
(s/def ::account/created-at inst?)
(s/def ::orc/account
  (s/schema [::account/id
             ::account/name
             ::account/initial-balance
             ::account/created-at]))
(s/def ::orc/accounts
  (s/coll-of (s/select ::orc/account [*])
             :kind set?))

(s/def ::entry/id uuid?)
(s/def ::entry/amount decimal?)
(s/def ::entry/type #{::entry/credit ::entry/debit}) ;; credit out, debit in
(s/def ::entry/other-party (s/and string? (complement str/blank?)))
(s/def ::entry/when inst?)
(s/def ::orc/entry
  (s/schema [::entry/id
             ::entry/amount
             ::entry/type
             ::entry/other-party
             ::entry/when
             ::account/name
             ::category/name
             ::budget/name]))
(s/def ::orc/entries
  (s/coll-of (s/select ::orc/entry [*])
             :kind set?))

(s/def ::orc/entire-setup
  (s/schema [::orc/budgets
             ::orc/categories
             ::orc/accounts
             ::orc/entries]))

(comment
  (require '[clojure.spec.gen.alpha :as gen])
  (gen/generate
   (s/gen (s/select ::orc/entire-setup [*])))
  '_)
