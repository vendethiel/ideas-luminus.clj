(ns vendethiel.ideas.web.pages.users
  (:require
   [jsonista.core :as j]
   [vendethiel.ideas.web.pages.layout :as layout]))

(defn get-user [{:keys [query-fn]} {:keys [path-params] :as request}]
  (let [id (:id path-params)
        user (query-fn :get-user-detailed
                       {:admin-query? (get-in request [:user :admin])
                        :id id})
        comments (query-fn :get-user-comments {:id id :limit 5})
        data (map (juxt identity #(j/read-value (%1 user)))
                  [:implementations])]
    (layout/render request "users/show.html"
                   (into {:user user :comments comments} data))))

(defn register-form [_ request]
  (layout/render request "users/new.html"))
