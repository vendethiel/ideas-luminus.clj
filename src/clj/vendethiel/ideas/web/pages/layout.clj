(ns vendethiel.ideas.web.pages.layout
  (:require
   [clojure.java.io]
   [clojure.string :as str]
   [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]
   [ring.util.anti-forgery :refer [anti-forgery-field]]
   [ring.util.http-response :refer [content-type ok]]
   [ring.util.response]
   [selmer.parser :as parser]))

(def selmer-opts {:custom-resource-path (clojure.java.io/resource "html")})

(defn humanize [value]
  (let [strval (if (or (symbol? value) (keyword? value)) (name value) value)]
    (str/replace strval #"[_-]" " ")))

(defn init-selmer!
  [{:keys [env]}]
  ;; disable HTML template caching for live reloading during development
  (when (= :dev env) (parser/cache-off!))
  (parser/add-tag! :csrf-field (fn [_ _] (anti-forgery-field)))
  (parser/add-tag! :ifcan
      (fn [_args {:keys [user user-can] :as context-map} content]
        (let [args (map #(parser/resolve-arg % context-map) _args)
              [main sub & object-user-id] args
              can (get-in user-can [main sub])
              own-sub (keyword (str (name sub) "-own"))
              own-can (when (and object-user-id (= (:id user) object-user-id))
                        (get-in user-can [main own-sub]))]
          (if (or can own-can)
            (get-in content [:ifcan :content])
            (get-in content [:ifcant :content]))))
    :ifcant :endcan)
  (parser/add-filter! :humanize humanize)
  )

(def CAN_ALL {:read true :write true :edit true :delete true})
(def CAN_READ {:read true})
(def CAN_RW {:read true :write true})
(def CAN_SELF {:read true :write true :edit-own true :delete-own true})
(defn user-cans
  "Generates a list of 'can-do's for a user"
  [user]
  (if user
    (if (:is_admin user)
      {:auth {:logout true}
       :categories CAN_ALL :ideas CAN_ALL
       :implementations CAN_ALL :comments CAN_ALL}
      {:auth {:logout true}
       :categories CAN_READ :ideas CAN_READ
       :implementations CAN_SELF :comments CAN_SELF})
    {:auth {:login true}
     :categories CAN_READ :ideas CAN_READ
     :implementations CAN_READ :comments CAN_READ}))

(defn render
  [{:keys [user] :as request} template & [params]]
  (-> (parser/render-file template
                          (assoc params
                                 :page template
                                 :csrf-token *anti-forgery-token*
                                 :user user
                                 :user-can (user-cans user))
                          selmer-opts)
      (ok)
      (content-type "text/html; charset=utf-8")))

(defn error-page
  "error-details should be a map containing the following keys:
   :status - error status
   :title - error title (optional)
   :message - detailed error message (optional)
   returns a response map with the error page as the body
   and the status specified by the status key"
  [error-details]
  {:status  (:status error-details)
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body    (parser/render-file "error.html" error-details selmer-opts)})
