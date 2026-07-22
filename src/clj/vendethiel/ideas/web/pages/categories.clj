(ns vendethiel.ideas.web.pages.categories
  (:require [vendethiel.ideas.web.pages.layout :as layout]))

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
  (let [category (query-fn :get-category {:id (:id path-params)})]
    ))

(defn delete-category [{:keys [query-fn]} {:keys [path-params] :as request}]
  )
