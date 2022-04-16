(ns edenferreira.relational-algebra.extensions
  (:refer-clojure :exclude [extend]))

(defn extend [k f rel]
  (set
   (map (fn [r]
          (assoc r k (f r)))
        rel)))
