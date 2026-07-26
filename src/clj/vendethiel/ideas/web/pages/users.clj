(ns vendethiel.ideas.web.pages.users
  (:require
    [vendethiel.ideas.web.pages.layout :as layout]))

(defn get-user [{:keys [query-fn]} {:keys [path-params] :as request}]
  (let [id (:id path-params)
        user (query-fn :get-user-profile
                       {:admin-query? false ;; TODO
                        :id id})]
    (layout/render request "users/show.html"
                   {:user user})))

(defn register-form [_ request]
  (layout/render request "users/new.html"))
