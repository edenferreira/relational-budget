(ns edenferreira.clojure.set.extensions-test
  (:refer-clojure :exclude [extend])
  (:require [edenferreira.clojure.set.extensions :as set.ext]
            [clojure.test :refer [deftest testing is]]
            [matcher-combinators.test :refer [match?]]))

(deftest project-away
  (is (match?
       #{{:b 2 :c 3}
         {:b 2 :c 4}}
       (set.ext/project-away #{{:a 1 :b 2 :c 3}
                               {:a 1 :b 2 :c 4}}
                             [:a])))
  (is (match?
       #{{:a 1 :b 2}}
       (set.ext/project-away #{{:a 1 :b 2 :c 3}
                               {:a 1 :b 2 :c 4}}
                             [:c]))))

(deftest extend
  (is (match?
       #{{:a 1 :b 1 :c 2}
         {:a 2 :b 2 :c 3}}
       (set.ext/extend #{{:a 1 :b 1}
                         {:a 2 :b 2}}
         :c (comp inc :b))))
  (is (match?
       #{{:a 1 :b 2}
         {:a 2 :b 3}}
       (set.ext/extend #{{:a 1 :b 1}
                         {:a 2 :b 2}}
         :b (comp inc :b))))
  (is (match?
       #{{:a 3 :b 3}}
       (set.ext/extend #{{:a 1 :b 1}
                         {:a 2 :b 2}}
         :a (constantly 3)
         :b (constantly 3)
         :c (constantly :something)))))

(deftest summarize
  (is (match?
       #{{:a 2 :sum-b 11}}
       (set.ext/summarize #{{:a 2 :b 5}
                            {:a 2 :b 6}}
                          [:a]
                          :sum-b #(->> % (map :b) (reduce +)))))

  (is (match?
       #{{:a 2 :b 5 :c 5}}
       (set.ext/summarize #{{:a 2 :b 5 :c 3}
                            {:a 2 :b 5 :c 4}
                            {:a 2 :b 5 :c 5}}
                          [:a :b]
                          :c #(->> % (map :c) sort last)))))

(comment
  '_)
