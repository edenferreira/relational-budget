(ns edenferreira.orcamento.domain
  (:require [clojure.alpha.spec :as s]
            [clojure.set :as set]
            [br.com.orcamento :as-alias orc]
            [br.com.orcamento.budget :as-alias budget]
            [br.com.orcamento.entry :as-alias entry]))

(s/def ::budget/id uuid?)
(s/def ::entry/id uuid?)
(s/def ::entry/amount decimal?)
(s/def ::entry/type decimal?)
(s/def ::entry/account keyword?)
(s/def ::entry/other-party keyword?)
(s/def ::entry/budget keyword?)
(s/def ::entry/category keyword?)
(s/def ::entry/account keyword?)
(s/def ::entry/when inst?)

(def entries
  #{#::entry{:amount 10
             :type :credit
             :account :checking
             :other-party :feira
             :category :mercado
             :when #inst "2000-01-01T00:00:00Z"}
    #::entry{:amount 15
             :type :debit
             :account :checking
             :other-party :empregador
             :category :salário
             :when #inst "2000-01-02T00:00:00Z"}
    #::entry{:amount 2
             :type :credit
             :account :checking
             :other-party :bar-da-esquina
             :category :mercado
             :when #inst "2000-01-02T00:00:00Z"}})

(defn entries-by-attribute [x v entries]
  (set/select
   #(= (get % x) v)
   entries))

(defn total-for-category [category entries]
  (reduce (fn [s {::entry/keys [amount type]}]
            (case type
              :credit (- s amount)
              :debit (+ s amount)))
          0
          (set/project
           (entries-by-attribute ::entry/category category entries)
           [::entry/amount ::entry/type])))


(comment

  (entries-by-attribute ::entry/category :mercado entries)
  (total-for-category :mercado entries)

  (def a #{{:a 1 :b 2}
           {:a 2 :b 2}
           {:a 2 :b 3}
           {:a 3 :b 4}})
  (def b #{{:b 2 :d 4}
           {:b 3 :d 5}})
  (set
   (for [x a
         y b]
     (merge x y)))
  '_)
