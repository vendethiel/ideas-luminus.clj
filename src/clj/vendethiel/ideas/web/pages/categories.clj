(ns vendethiel.ideas.web.pages.categories
  (:require [vendethiel.ideas.web.pages.layout :as layout]
            [ring.util.http-response :as http-response]))

(defn list-categories [{:keys [query-fn]} request]
  (layout/render request "categories/index.html"
                 {:categories (query-fn :list-categories {})}))

(defn get-category [{:keys [query-fn]} {:keys [path-params] :as request}]
  (let [id (:id path-params)]
    (layout/render request "categories/show.html"
                   {:category (query-fn :get-category
                                        {:id id})
                    :ideas (query-fn :list-category-ideas {:category id})})))

(defn edit-category [{:keys [query-fn]} {:keys [path-params] :as request}]
  (let [id (:id path-params)
        category (and id (query-fn :get-category {:id id}))]
    (if (and id (nil? category))
      (http-response/not-found)
      (layout/render request "categories/edit.html"
                     {:id id :category category}))))
