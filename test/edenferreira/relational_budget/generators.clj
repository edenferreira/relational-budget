(ns edenferreira.relational-budget.generators
  (:require [clojure.set :as set]
            [edenferreira.relational-budget.domain :as domain]
            [br.com.relational-budget :as-alias orc]
            [br.com.relational-budget.budget :as-alias budget]
            [br.com.relational-budget.category :as-alias category]
            [br.com.relational-budget.account :as-alias account]
            [br.com.relational-budget.entry :as-alias entry]
            [clojure.test.check.generators :as gen]
            [clojure.alpha.spec :as s]))

(defn one-account []
  (s/gen (s/select ::orc/account [*])))

(defn one-category []
  (s/gen (s/select ::orc/category [*])))

(defn one-budget []
  (s/gen (s/select ::orc/budget [*])))

(defn one-entry []
  (s/gen (s/select ::orc/entry [*])))

(defn one-entry-with-specific-type [type]
  (gen/fmap #(assoc % ::entry/type type) (one-entry)))

(defn many-accounts [& {:keys [min-elements
                               max-elements]
                        :or {min-elements 1
                             max-elements 5}}]
  (gen/set (one-account)
           {:min-elements min-elements
            :max-elements max-elements}))

(defn many-categories [& {:keys [min-elements
                                 max-elements]
                          :or {min-elements 1
                               max-elements 5}}]
  (gen/set (one-category)
           {:min-elements min-elements
            :max-elements max-elements}))

(defn many-budgets [& {:keys [min-elements
                              max-elements]
                       :or {min-elements 1
                            max-elements 5}}]
  (gen/set (one-budget)
           {:min-elements min-elements
            :max-elements max-elements}))

;; TODO explain basis
(defn many-entries [& {:keys [min-elements
                              max-elements
                              min-different-basis
                              max-different-basis]
                       :or {min-elements 3
                            max-elements 1000
                            min-different-basis 2
                            max-different-basis 6}}]

  (let [basis (gen/hash-map
               ::budget/name (s/gen ::budget/name)
               ::category/name (s/gen ::category/name)
               ::account/name (s/gen ::account/name))]
    (gen/fmap
     (fn [[basis entries]]
       (map merge entries (cycle basis)))
     (gen/tuple
      (gen/set basis {:min-elements min-different-basis
                      :max-elements max-different-basis})
      (gen/set (one-entry)
               {:min-elements min-elements
                :max-elements max-elements})))))

(defn ^:private create-generator-from-existing-entries
  [entity-name-attr entity-gen entries]
  (gen/bind (gen/return (set/project entries [entity-name-attr]))
            (fn [elements]
              (gen/fmap set
                        (apply gen/tuple
                               (map (fn [a]
                                      (gen/fmap #(merge % a)
                                                entity-gen))
                                    elements))))))

(defn many-accounts-from-entries [entries]
  (create-generator-from-existing-entries ::account/name
                                          (one-account)
                                          entries))

(defn entire-setup []
  (gen/bind
   (many-entries)
   (fn [entries]
     (let [accounts (gen/fmap
                     (partial apply set/union)
                     (gen/tuple
                      (create-generator-from-existing-entries ::account/name
                                                              (one-account)
                                                              entries)
                      (many-accounts)))
           categories (gen/fmap
                       (partial apply set/union)
                       (gen/tuple
                        (create-generator-from-existing-entries ::category/name
                                                                (one-category)
                                                                entries)
                        (many-categories)))
           budgets (gen/fmap
                    (partial apply set/union)
                    (gen/tuple
                     (create-generator-from-existing-entries ::budget/name
                                                             (one-budget)
                                                             entries)
                     (many-budgets)))]
       (gen/hash-map
        ::orc/accounts accounts
        ::orc/categories categories
        ::orc/budgets budgets
        ::orc/entries (gen/return entries))))))

(comment
  (gen/generate (entire-setup))

  (let [entries (::orc/entries (gen/generate (db)))]
    {:entries-count (count entries)
     :accounts (set/project entries [::account/name
                                     ::category/name
                                     ::budget/name])})
  (let [entries (::orc/entries (gen/generate (entire-setup)))]
    {:entries-count (count entries)
     :accounts (set/project entries [::account/name
                                     ::category/name
                                     ::budget/name])})
  '_)
