(ns edenferreira.rawd.logic
  (:require [edenferreira.rawd :as-alias rwd] ))

(defn attribute-input-parameters
  [{:input/keys [id form label name type]
    ::rwd/keys [select-options]}]
  (case type
    "select"
    {:id id
     :form form
     :label label
     :name name
     :options select-options}
    {:id id
     :form form
     :type type
     :label label
     :name name}))
