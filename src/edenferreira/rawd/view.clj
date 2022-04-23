(ns edenferreira.rawd.view)

;; slightly modified from https://stackoverflow.com/questions/12679754/idiomatic-way-of-rendering-style-info-using-clojure-hiccup
(defn style [& info]
  (.trim (apply str (map #(let [[kwd val] %]
                            (str (name kwd) ":" val "; "))
                         (apply hash-map info)))))

(defn rels->table [rels]
  (let [ks (-> rels first keys vec)]
    [:table {:class "table table-striped"}
     [:tr
      (map #(do [:td %]) ks)]
     (map (fn [rel]
            [:tr (map #(do [:td (get rel %)]) ks)])
          rels)]))

(defn db->table [m]
  [:div
   (map (fn [[k v]]
          [:details
           [:summary k]
           (rels->table v)])
        m)])

(defn create-select-input
  [& {:keys [id form label name options]}]
  [:div {:class "form-group"}
   [:label {:for id} label]
   [:select {:id id
             :name name
             :form form
             :style (style :margin "8px")}
    (map #(do [:option {:value %} %])
         options)]])

(defn create-datetime-input [& {:keys [id form label name value placeholder]}]
  [:div {:class "form-group"}
   [:label {:for id
            :style (style :margin-right "8px")} label]
   [:input (cond-> {:type "datetime-local"
                    :id id
                    :name name
                    :form form}
             value (assoc :value value)
             placeholder (assoc :placeholder placeholder))]])

(defn create-input [& {:keys [id form type label name placeholder]}]
  [:div {:class "form-group"}
   [:label {:for id
            :style (style :margin-right "8px")} label]
   [:input (cond-> {:type type
                    :id id
                    :name name
                    :form form
                    :step "0.01"
                    :min 0}
             placeholder (assoc :placeholder placeholder))]])

(defn create-form [& {:keys [id enctype action method items button-label]}]
  [:form {:id id
          :action action
          :method method
          :enctype enctype
          :class "col-sm"}
   (seq items)
   [:button {:type "submit"
             :class "btn btn-primary"
             :style (style :margin-top "8px"
                           :margin-bottom "8px")}
    button-label]])

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
  [:div {:class "row"} (map entity->form entities)])

(defn index [& {:keys [entities as-of state]}]
  (list
   [:head [:title "RAWD"]
    [:link {:rel "stylesheet"
            :href "https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css"
            :integrity "sha384-1BmE4kWBq78iYhFldvKuhfTAU6auU8tT94WrHftjDbrCEXSU1oBoqyl2QvZ6jIW3"
            :crossorigin "anonymous"}]]
   [:body
    [:div {:class "container"}
     [:h1 {:class ""} "Rawd"]
     (entities->forms entities)
     [:hr]
     [:h1 {:class "text-success"} "Filters"]
     (let [form-id "as-of-filter-form"]
       (create-form
        :id form-id
        :enctype "application/x-www-form-urlencoded"
        :method "POST"
        :action "/filter/as-of"
        :items [(create-datetime-input :id "as-of-filter"
                                       :form form-id
                                       :label "as-of"
                                       :name "as-of"
                                       :value (when as-of
                                                (subs (str as-of)
                                                      0
                                                      (- (count (str as-of))
                                                         4))))]
        :button-label "Filter"))

     [:hr]
     [:h1 {:class "text-success"} "State"]
     (db->table state)]]))
