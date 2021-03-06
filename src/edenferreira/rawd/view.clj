(ns edenferreira.rawd.view
  (:require [edenferreira.rawd :as-alias rwd]
            [clojure.set :as set]
            [edenferreira.clojure.set.extensions :as set.ext]
            [edenferreira.rawd.derived-relations :as rawd.rels]
            [clojure.string :as string]
            [clojure.alpha.spec :as s]
            [edenferreira.rawd.view :as view]))

;; slightly modified from https://stackoverflow.com/questions/12679754/idiomatic-way-of-rendering-style-info-using-clojure-hiccup
(defn style [& info]
  (.trim (apply str (map #(let [[kwd val] %]
                            (str (name kwd) ":" val "; "))
                         (apply hash-map info)))))

(defn rels->table [rels]
  (let [ks (-> rels first keys vec)]
    [:table {:class "table table-striped"}
     [:tr
      (map #(do [:td
                 (if-some [n (namespace %)]
                   (str (-> n (string/split #"\.") last)
                        " "
                        (string/replace (name %) "-" " "))
                   (string/replace (name %) "-" " "))])
           ks)]
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

(defn create-form-html
  [{:form/keys [id enctype method action button-label items]}]
  (create-form
   :id id
   :enctype enctype
   :method method
   :action action
   :items items
   :button-label button-label))

(defn entities->forms [entities attributes]
  (-> (rawd.rels/entities-and-attributes-inputs-html
       entities
       attributes)
      (set.ext/extend :input/html
        (fn [{:input/keys [type parameters]}]
          (case type
            "select"
            (create-select-input parameters)
            (create-input parameters))))
      (set.ext/summarize [:form/id
                          :form/enctype
                          :form/method
                          :form/action
                          :form/button-label]
                         :form/items (partial map :input/html))
      (set.ext/extend :form/html create-form-html)))

(defn create-as-of-filter [& {:keys [as-of]}]
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
     :button-label "Filter")))

(defn index [& {:keys [as-of state]
                ::rwd/keys [entities attributes filters]}]
  (list
   [:head [:title "RAWD"]
    [:link {:rel "stylesheet"
            :href "https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css"
            :integrity "sha384-1BmE4kWBq78iYhFldvKuhfTAU6auU8tT94WrHftjDbrCEXSU1oBoqyl2QvZ6jIW3"
            :crossorigin "anonymous"}]]
   [:body
    [:div {:class "container"}
     [:details
      [:summary [:span {:class "h1"} "CruD"]]
      [:div {:class "row"}
       (->> (entities->forms entities attributes)
            (sort-by ::rwd/entity)
            reverse
            (map :form/html))]]
     [:hr]
     [:details
      [:summary [:span {:class "h1"} "Filters"]]
      [:div {:class "row"}
       (create-as-of-filter :as-of as-of)
       (map :form/html
            (set.ext/extend
                ;; JOIN with attributes for the type
             filters
              :form/html
              (fn [{::rwd/keys [attribute]}]
                (let [attribute (name attribute)]
                  (view/create-form
                   :id (str "form-filter-" attribute)
                   :enctype "application/x-www-form-urlencoded"
                   :method "POST"
                   :action "/filter/attributes"
                   :button-label "Filter"
                   :items [(create-input :id (str "filter-" attribute)
                                         :type "text"
                                         :name (str "filter-" attribute)
                                         :form (str "form-filter-" attribute)
                                         :label (str "filter " attribute))])))))
       (view/create-form
        :id "form-clear-filter-"
        :enctype "application/x-www-form-urlencoded"
        :method "GET"
        :action "/"
        :button-label "Clear Filters")]]
     [:hr]
     [:div {:class "row"}
      [:h1 {} "State"]
      (db->table state)]]]))
