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

(defn create-index [get-state entities]
  (fn index [_request]
    (h.page/html5
     {}
     (h/html
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
        [:h1 {:class "text-success"} "State"]
        (db->table (get-state))]]))))

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
        {:status 303
         :headers {"Location" "/index.html"}
         :body {}}
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
