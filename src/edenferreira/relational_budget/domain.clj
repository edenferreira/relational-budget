(ns edenferreira.relational-budget.domain
  (:require [clojure.alpha.spec :as s]
            [br.com.relational-budget :as-alias rebu]
            [clojure.test.check.generators :as gen]
            [clojure.alpha.spec.gen :as s.gen]
            [br.com.relational-budget.general-sspec :as-alias orc.general]
            [br.com.relational-budget.budget :as-alias budget]
            [br.com.relational-budget.category :as-alias category]
            [br.com.relational-budget.account :as-alias account]
            [br.com.relational-budget.assignment :as-alias assignment]
            [br.com.relational-budget.entry :as-alias entry])
  (:import [java.time Instant]))

(s/def ::positive-bigdec (s/with-gen (s/and decimal? #(or (pos? %) (zero? %)))
                           (constantly (gen/fmap (comp bigdec abs) gen/int))))

(s/def ::instant (s/with-gen #(instance? Instant %)
                   #(gen/fmap (memfn toInstant)
                              (s.gen/gen-for-pred inst?))))

(s/def ::budget/id uuid?)
(s/def ::budget/name (s/and string? #(< 0 (count %))))
(s/def ::budget/created-at ::instant)
(s/def ::budget/balance decimal?)
(s/def ::rebu/budget
  (s/keys :req [::budget/id
                ::budget/name
                ::budget/created-at]))
(s/def ::rebu/budgets
  (s/coll-of ::rebu/budget :kind set?))

(s/def ::category/id uuid?)
(s/def ::category/name (s/and string? #(< 0 (count %))))
(s/def ::category/created-at ::instant)
(s/def ::category/balance decimal?)
(s/def ::rebu/category
  (s/keys :req [::category/id
                ::category/name
                ::category/created-at]))
(s/def ::rebu/categories
  (s/coll-of ::rebu/category :kind set?))

(s/def ::account/id uuid?)
(s/def ::account/name (s/and string? #(< 0 (count %))))
(s/def ::account/initial-balance decimal?)
(s/def ::account/created-at ::instant)
(s/def ::rebu/account
  (s/keys :req [::account/id
                ::account/name
                ::account/initial-balance
                ::account/created-at]))
(s/def ::rebu/accounts
  (s/coll-of ::rebu/account :kind set?))

(s/def ::entry/id uuid?)
(s/def ::entry/amount ::positive-bigdec)
(s/def ::entry/type #{::entry/credit ::entry/debit}) ;; credit out, debit in
(s/def ::entry/other-party (s/and string? #(< 0 (count %))))
(s/def ::entry/when ::instant)
(s/def ::rebu/entry
  (s/keys :req [::entry/id
                ::entry/amount
                ::entry/type
                ::entry/other-party
                ::entry/when
                ::account/name
                ::category/name
                ::budget/name]))
(s/def ::rebu/entries
  (s/coll-of ::rebu/entry :kind set?))

(s/def ::assignment/id uuid?)
(s/def ::assignment/amount ::positive-bigdec)
(s/def ::assignment/created-at ::instant)

(s/def ::rebu/assignment
  (s/keys :req [::assignment/id
                ::assignment/amount
                ::assignment/created-at
                ::category/name]))

(s/def ::rebu/assignments
  (s/coll-of ::rebu/assignment :kind set?))

(s/def ::rebu/entire-setup
  (s/keys :req [::rebu/budgets
                ::rebu/categories
                ::rebu/accounts
                ::rebu/entries
                ::rebu/assignments]))

(comment
  (require '[clojure.spec.gen.alpha :as gen])

  '_)
