(ns edenferreira.rawd.domain
  (:require [clojure.alpha.spec :as s]
            [clojure.alpha.spec.gen :as s.gen]
            [edenferreira.rawd :as-alias rwd]))

(s/def ::rwd/entity keyword?)
(s/def ::rwd/adapter ifn?)
(s/def ::rwd/handler ifn?)
(s/def ::rwd/entities
  (s/coll-of
   (s/keys :req [::rwd/entity
                 ::rwd/adapter
                 ::rwd/handler])
   :kind set))

(s/def ::rwd/attribute keyword?)
(s/def ::rwd/type keyword?)
(s/def ::rwd/select-options (s/coll-of string? :min-count 1))
(s/def ::rwd/attributes
  (s/coll-of
   (s/keys :req [::rwd/attribute
                 ::rwd/type
                 ::rwd/entity]
           :opt [::rwd/select-options])
   :kind set))

(s/def ::rwd/filters
  (s/coll-of
   (s/keys :req [::rwd/attribute
                 ::rwd/adapter])
   :kind set))
