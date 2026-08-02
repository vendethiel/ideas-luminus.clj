(ns vendethiel.ideas.web.controllers.categories
 (:require
  [clojure.tools.logging :as log]
  [ring.util.http-response :as http-response]
  [vendethiel.ideas.web.pages.layout :as layout]
  [vendethiel.ideas.web.routes.utils :refer [update-or-insert]]))

(def category-shape
  [:map [:name [:string {:min 1}]]])

(defn update-category [{:keys [query-fn]} {:keys [path-params form-params can-delayed] :as request}]
  (let [id (:id path-params)
        category (when id (query-fn :get-category {:id id}))]
    (can-delayed [:categories :edit])
    (let [result (update-or-insert {:id id
                                    :shape category-shape
                                    :data form-params
                                    :create-fn (partial query-fn :create-category!)
                                    :update-fn (partial query-fn :update-category!)})]
      (case (:result result)
        :validation-errors
        (layout/render request "categories/edit.html"
                       {:id id
                        :category (or category (:data result))
                        :errors (:errors result)})

        (:created :updated)
        (http-response/see-other (str "/categories/" (:id result)))

        :not-found
        (http-response/not-found)

        (:create-exception :update-exception)
        (do
          (log/error (:exception result) "update-category" (:result result))
          (http-response/internal-server-error))
        ))))

(defn delete-category [{:keys [query-fn]} {:keys [path-params]}]
  (let [delete (query-fn :delete-category {:id (:id path-params)})]
    (if (= 1 (:next.jdbc/update-count delete))
      (http-response/see-other "/")
      (http-response/not-found))))
