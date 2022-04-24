(ns edenferreira.clojure.set.extensions-properties
  (:require [clojure.test :refer [deftest is]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [edenferreira.clojure.set.extensions :as set.ext]
            [matcher-combinators.test :refer [match?]]
            [clojure.test.check :as test.check]
            [clojure.set :as set]))

(def rel-generator
  (gen/bind
   (gen/tuple (gen/vector-distinct gen/keyword
                                   {:min-elements 5
                                    :max-elements 15})
              (gen/vector gen/simple-type-printable-equatable 150))
   (fn [[ks vs]]
     (gen/set
      (apply gen/hash-map
             (mapcat #(do [% (gen/elements vs)])
                     ks))))))

(def left-join-itself-is-itself
  (prop/for-all
   [rels rel-generator]
   (= rels (set.ext/left-join rels rels))))

(deftest check-left-join-itself-is-itself
  (is
   (match? {:pass? true}
           (test.check/quick-check
            30
            left-join-itself-is-itself))))

(def left-join-empty-set-is-itself
  (prop/for-all
   [rels rel-generator]
   (= rels (set.ext/left-join rels #{}))))

(deftest check-left-join-empty-set-is-itself
  (is
   (match? {:pass? true}
           (test.check/quick-check
            30
            left-join-empty-set-is-itself))))

;; This test is as example and show the difference
;; from left-join
(def join-empty-set-is-empty-set
  (prop/for-all
   [rels rel-generator]
   (= #{} (set/join rels #{}))))

(deftest check-join-empty-set-is-empty-set
  (is
   (match? {:pass? true}
           (test.check/quick-check
            30
            join-empty-set-is-empty-set))))

(comment
  (gen/generate left-join-different-rels-is-union)

  (clojure.test/run-tests)
  '_)
