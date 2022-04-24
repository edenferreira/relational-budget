(ns edenferreira.relational-budget.main
  (:require [edenferreira.relational-budget.integrity :as integrity]
            [br.com.relational-budget :as-alias rebu]
            [br.com.relational-budget.budget :as-alias budget]
            [br.com.relational-budget.category :as-alias category]
            [br.com.relational-budget.account :as-alias account]
            [br.com.relational-budget.entry :as-alias entry]
            [edenferreira.relational-budget.api :as api]
            [edenferreira.relational-budget.main :as main]))

(defonce db
  (atom {}))

(def integrity
  [integrity/entry-must-have-existing-account
   integrity/entry-must-have-existing-category
   integrity/entry-must-have-existing-budget
   integrity/budget-name-must-be-unique
   integrity/category-name-must-be-unique
   integrity/account-name-must-be-unique])

(defn build-validate-integrity! [integrity]
  (fn [state]
    (let [result (reduce
                  (fn [agg f]
                    (if (f state)
                      (update agg ::success conj f)
                      (update agg ::fail conj f)))
                  {::success []
                   ::fail []}
                  integrity)]
      (if (seq (::fail result))
        (let [failed-fn-names (map #((requiring-resolve `clojure.main/demunge)
                                     (-> %
                                         .getClass
                                         .getSimpleName))
                                   (::fail result))]
          (throw (ex-info "Integrity failures"
                          {::failures failed-fn-names})))
        true))))

;; The db is def only once but the validator
;; is always set again to allow for fater
;; feedback cycles
(set-validator! db
                (build-validate-integrity! integrity))

(defn create-budget [& {:as m}]
  (swap! db api/create-budget m))

(defn create-category [& {:as m}]
  (swap! db api/create-category m))

(defn create-assignment [& {:as m}]
  (swap! db api/create-assignment m))

(defn create-account [& {:as m}]
  (swap! db api/create-account m))

(defn add-entry [& {:as m}]
  (swap! db api/add-entry m))


(comment
  (swap! db empty)
  (require '[edenferreira.rawd.persistence :as rawd.persitence]
           '[edenferreira.rawd.instant :as instant])

(add-entry
     :id (random-uuid)
     :type ::entry/credit
     :amount 100M
     :other-party "merchant"
     :budget "some budget not exist"
     :category "category name not exist"
     :account "account name not exist"
     :as-of (instant/parse "2000-01-01T00:00:00Z"))
  (do
    (create-budget
     :id (random-uuid)
     :name "some budget"
     :as-of (instant/parse "2000-01-01T00:00:00Z"))
    (create-category
     :id (random-uuid)
     :name "category name"
     :as-of (instant/parse "2000-01-01T00:00:00Z"))
    (create-account
     :id (random-uuid)
     :name "account name"
     :type "checking"
     :initial-balance 1000M
     :as-of (instant/parse "2000-01-01T00:00:00Z"))
    (add-entry
     :id (random-uuid)
     :type ::entry/credit
     :amount 100M
     :other-party "merchant"
     :budget "some budget"
     :category "category name"
     :account "account name"
     :as-of (instant/parse "2000-01-01T00:00:00Z")))
  (integrity/category-name-must-be-unique @db)
  (def path "./persisted.edn")
  ((requiring-resolve `edenferreira.rawd.persistence/persist!)
   @main/db
   path)
  (reset! main/db
          ((requiring-resolve `edenferreira.rawd.persistence/read!)
           path))
  @main/db
  '_)

(comment
  (def v (validate! [(fn abc [& _] false)]))
  (v {:a :B})
  (keys (bean (:class (bean (first integrity)))))
  ((requiring-resolve `clojure.main/demunge)
   (:simpleName (bean (:class (bean (first integrity))))))

  (keys (a integrity)))
