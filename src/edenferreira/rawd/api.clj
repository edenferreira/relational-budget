(ns ^{:doc "CRUD for development"}
    edenferreira.rawd.api)

;; slightly modified from https://stackoverflow.com/questions/12679754/idiomatic-way-of-rendering-style-info-using-clojure-hiccup
(defn style [& info]
  (.trim (apply str (map #(let [[kwd val] %]
                            (str (name kwd) ":" val "; "))
                         (apply hash-map info)))))

(defn create-select-input
  [& {:keys [id form label name options]}]
  [:li
   [:label {:for id
            :style (style :margin-right "8px")} label]
   [:select {:id id
             :name name
             :form form}
    (map #(do [:option {:value %} %])
         options)]])

(defn create-input [& {:keys [id form type label name placeholder]}]
  [:li
   [:label {:for id
            :style (style :margin-right "8px")} label]
   [:input (cond-> {:type type
                    :id id
                    :name name
                    :form form}
             placeholder (assoc :placeholder placeholder))]])

(defn create-form [& {:keys [id enctype action method items button-label]}]
  [:form {:id id
          :action action
          :method method
          :enctype enctype}
   [:ul {:style (style :list-style-type "none")}
    (concat (seq items)
            [[:button button-label]])]])

(defn entity->form [& {:keys [name attributes]}]
  (let [form-id (str (clojure.core/name name)
                     "form")]
    (create-form
     :id form-id
     :enctype "application/x-www-form-urlencoded"
     :method "POST"
     :action (str "/" (clojure.core/name name) "/create")
     :items (map
             (fn [[attr {:keys [type] :as opt}]]
               (case type
                 "select"
                 (create-select-input :id (str (clojure.core/name name)
                                               "-"
                                               (clojure.core/name attr))
                                      :form form-id
                                      :label (str (clojure.core/name name)
                                                  " "
                                                  (clojure.core/name attr))
                                      :name (str (clojure.core/name name)
                                                 "-"
                                                 (clojure.core/name attr))
                                      opt)
                 (create-input :id (str (clojure.core/name name)
                                        "-"
                                        (clojure.core/name attr))
                               :form form-id
                               :type type
                               :label (str (clojure.core/name name)
                                           " "
                                           (clojure.core/name attr))
                               :name (str (clojure.core/name name)
                                          "-"
                                          (clojure.core/name attr)))))
             attributes)
     :button-label (str "Create "
                        (clojure.core/name name)))))

(defn entities->forms [entities]
  [:div (map entity->form entities)])
