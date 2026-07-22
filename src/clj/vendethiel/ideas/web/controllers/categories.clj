(ns vendethiel.ideas.web.controllers.categories
 (:require
  [clojure.walk :refer [keywordize-keys]]
  [malli.core :as m]
  [malli.error :as me]
  [ring.util.http-response :as http-response]
  [vendethiel.ideas.web.pages.layout :as layout]))

(def category-shape
  [:map [:name [:string {:min 1}]]])

(defn update-category [{:keys [query-fn]} {:keys [path-params form-params] :as request}]
  (let [id (:id path-params)
        category (and id (query-fn :get-category {:id id}))
        data (keywordize-keys form-params)
        errors (m/explain category-shape data)]
    (if errors
      (layout/render request "categories/edit.html"
                     {:id id
                      :category (or category data)
                      :errors (me/humanize errors)})
      (if id
        (let [update (query-fn :update-category! (merge data {:id id}))]
          (if (= 1 (:next.jdbc/update-count update))
            (http-response/see-other (str "/categories/" id))
            (http-response/not-found)))
        (let [created (query-fn :create-category! data)]
          (http-response/see-other (str "/categories/" (:id created))))))))

(defn delete-category [{:keys [query-fn]} {:keys [path-params]}]
  (let [delete (query-fn :delete-category {:id (:id path-params)})]
    (if (= 1 (:next.jdbc/update-count delete))
      (http-response/see-other "/")
      (http-response/not-found))))
