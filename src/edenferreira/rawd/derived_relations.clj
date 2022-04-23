(ns edenferreira.rawd.derived-relations
  (:require [edenferreira.rawd :as-alias rwd]
            [edenferreira.clojure.set.extensions :as set.ext]
            [edenferreira.rawd.logic :as logic]
            [clojure.set :as set]
            [clojure.string :as string]))

(defn entities-forms [entities]
  (set.ext/extend entities
    :form/id (comp (partial str "form") name ::rwd/entity)
    :form/enctype (constantly "application/x-www-form-urlencoded")
    :form/method (constantly "POST")
    :form/action (comp #(str "/" % "/create") name ::rwd/entity)
    :form/button-label (comp (partial str "Create ") name ::rwd/entity)))

(defn attributes-inputs [attributes]
  (set.ext/extend attributes
    :input/id (comp name ::rwd/attribute)
    :input/label (comp #(string/replace % "-" " ") name ::rwd/attribute)
    :input/name (comp name ::rwd/attribute)
    :input/type ::rwd/type))

(defn entities-and-attributes-inputs-html [entities attributes]
 (-> (set/join (entities-forms entities)
                (attributes-inputs attributes))
      (set.ext/extend :input/form :form/id)
      (set.ext/extend :input/parameters logic/attribute-input-parameters)))
