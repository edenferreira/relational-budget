(ns edenferreira.rawd.instant
  (:import [java.time Instant]))

(defmethod print-method Instant
  [v ^java.io.Writer w]
  (.write w  (str "#rawd/instant \"" v "\"")))

(defn parse [s]
  (Instant/parse s))
