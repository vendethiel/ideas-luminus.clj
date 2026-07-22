(ns vendethiel.ideas.web.controllers.categories
 (:require [clojure.tools.logging :as log]
           [ring.util.http-response :as http-response]
           [malli.core :as m]
           [malli.error :as me]
           [vendethiel.ideas.web.pages.layout :as layout]))

(def category-shape
  {:name [:string {:min 1}]})

(defn update-category [{:keys [query-fn]} {:keys [path-params body-params] :as request}]
  (let [id (:id path-params)
        category (and id (query-fn :get-category {:id id}))]
    (if-let [errors (m/explain category-shape body-params)]
      (layout/render request "categories/edit.html"
                     {:category category
                      :errors (me/humanize errors)})
      (if id
        (let [update-nbr (query-fn :update-category (merge body-params {:id id}))]
          (if (= 1 update-nbr)
            (http-response/temporary-redirect (str "/categories/" id))
            (http-response/not-found)))
        (let [new-id (query-fn :create-category body-params)]
          (http-response/temporary-redirect (str "/categories/" new-id))))
      )))

(defn delete-category [{:keys [query-fn]} {:keys [path-params]}]
  (let [delete-nbr (query-fn :delete-category {:id (:id path-params)})]
    (if (= 1 delete-nbr)
      (http-response/temporary-redirect "/")
      (http-response/not-found))))
