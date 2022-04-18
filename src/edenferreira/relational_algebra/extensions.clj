(ns edenferreira.relational-algebra.extensions
  (:refer-clojure :exclude [extend])
  (:require [clojure.set :as set]))

(defn extend [k f rel]
  (set
   (map (fn [r]
          (assoc r k (f r)))
        rel)))

(defn summarize [projection f rels]
  (set
   (map (fn [[projected rels]]
          (merge projected (f rels)))
        (set/index rels projection))))

(defn max-attr [k]
  (fn [rels]
    (last (sort-by k rels))))

(defn min-attr [k]
  (fn [rels]
    (first (sort-by k rels))))

(defn sum-attr [k]
  (fn [rels]
    (reduce
     (fn [agg rel]
       (update agg k + (get rel k)))
     rels)))

(comment

  (= #{{:a 2 :b 3 :d 6}
       {:a 1 :b 2 :d 3}}
     (summarize #{{:a 1 :b 2 :d 3}
                  {:a 2 :b 3 :d 4}
                  {:a 2 :b 3 :d 5}
                  {:a 2 :b 3 :d 6}}
                [:a :b]
                (max-attr :d)))

(= #{{:a 2 :b 3 :d 4}
       {:a 1 :b 2 :d 3}}
     (summarize #{{:a 1 :b 2 :d 3}
                  {:a 2 :b 3 :d 4}
                  {:a 2 :b 3 :d 5}
                  {:a 2 :b 3 :d 6}}
                [:a :b]
                (min-attr :d)))

  (= #{{:a 2 :b 3 :d 15}
       {:a 1 :b 2 :d 3}}
     (summarize #{{:a 1 :b 2 :d 3}
                  {:a 2 :b 3 :d 4}
                  {:a 2 :b 3 :d 5}
                  {:a 2 :b 3 :d 6}}
                [:a :b]
                (sum-attr :d)))

  (= #{{:a 2 :b 3 :d 5 :e 10}
       {:a 1 :b 2 :d 3 :e 6}
       {:a 2 :b 3 :d 4 :e 9}
       {:a 2 :b 3 :d 6 :e 11}}
     (extend :e
       (comp (partial reduce +) vals)
       #{{:a 1 :b 2 :d 3}
         {:a 2 :b 3 :d 4}
         {:a 2 :b 3 :d 5}
         {:a 2 :b 3 :d 6}}))
  '_)
