(ns edenferreira.rawd.persistence
  (:require [edenferreira.rawd.instant :as instant]))

(defn persist! [state path]
  (spit path (prn-str state)))

(defn read! [path]
  (binding [*data-readers* {'rawd/instant instant/parse}]
    (read-string (slurp path))))

(comment
  '_)
