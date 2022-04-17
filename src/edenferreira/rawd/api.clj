(ns ^{:doc "CRUD for development"}
    edenferreira.rawd.api
  (:require [clojure.string :as str]
            [hiccup.page :as h.page]
            [hiccup.core :as h]
            [clojure.set :as set]))

;; slightly modified from https://stackoverflow.com/questions/12679754/idiomatic-way-of-rendering-style-info-using-clojure-hiccup
(defn style [& info]
  (.trim (apply str (map #(let [[kwd val] %]
                            (str (name kwd) ":" val "; "))
                         (apply hash-map info)))))

(defn rels->table [rels]
  [:ul
   (map (fn [rel]
          [:li
           [:ul (map (fn [[k v]]
                       [:li (str (name k) ": " v)])
                     rel)]])
        rels)])

(defn db->table [m]
  [:div
   (map (fn [[k v]]
          [:div [:div k]
           (rels->table v)])
        m)])

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

(defn create-index [get-state entities]
  (fn index [_request]
    (h.page/html5
     {}
     (h/html
      [:div {:class ""}
       [:h1 {:class ""} "Rawd"]
       (entities->forms entities)
       [:hr]
       [:h1 {:class "text-success"} "State"]
       (db->table (get-state))]))))

(defn create-respond-index [get-state entities]
  (let [index (create-index get-state entities)]
    (fn respond-index [request]
      {:status 200 :body (index request)})))

(defn respond-create-entity [entity adapter handler]
  (fn create-entity [request]
    (let [adapted (adapter (update-keys
                            (:params request)
                            #(keyword (str/replace %
                                                   (str (name entity) "-")
                                                   ""))))
          handled (handler adapted)]
      (if (= 200 (:status handled))
        (update handled
                :body
                (comp #(h.page/html5 {} %)
                      db->table))
        handled))))

(defn entities->routes [interceptors entities]
  (set
   (map
    (fn [{:keys [name adapter handler]
          :or {adapter identity
               handler identity}}]
      [(str "/" (clojure.core/name name) "/create")
       :post (conj interceptors
                   (respond-create-entity name adapter handler))
       :route-name (keyword (str "creating-entity-" (clojure.core/name name)))])
    entities)))

(defn routes [interceptors get-state entities]
  (set/union
   #{["/index.html" :get (conj interceptors (create-respond-index get-state entities)) :route-name :index]}
   (entities->routes interceptors entities)))

(comment
  (->> (db->table {:c #{{:a 1 :b 2}
                        {:a 2 :b 3}}})
       (h.page/html5 {}))
  (do h)
  '_)
