(ns vendethiel.ideas.web.controllers.ideas)

(defn update-idea [{:keys [query-fn]} {:keys [path-params] :as request}]
  )

;;(def category-shape
;;  [:map [:name [:string {:min 1}]]])
;;
;;(defn update-category [{:keys [query-fn]} {:keys [path-params form-params] :as request}]
;;  (let [id (:id path-params)
;;        category (when id (query-fn :get-category {:id id}))
;;        result (update-or-insert {:id id
;;                                  :shape category-shape
;;                                  :data form-params
;;                                  :create-fn (partial query-fn :create-category!)
;;                                  :update-fn (partial query-fn :update-category!)})]
;;    (case (:result result)
;;      :validation-errors
;;      (layout/render request "categories/edit.html"
;;                     {:id id
;;                      :category (or category (:data result))
;;                      :errors (:errors result)})
;;
;;      (:created :updated)
;;      (http-response/see-other (str "/categories/" (:id result)))
;;
;;      :not-found
;;      (http-response/not-found)
;;
;;      (:create-exception :update-exception)
;;      (do
;;        (log/error (:exception result) "update-category" (:result result))
;;        (http-response/internal-server-error))
;;      )))
