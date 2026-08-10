(ns vendethiel.ideas.web.controllers.ideas
  (:require
   [ring.util.http-response :as http-response]
   [vendethiel.ideas.web.pages.layout :as layout]
   [vendethiel.ideas.web.routes.utils :refer [update-or-insert]]
   [clojure.tools.logging :as log]
   [clojure.set :as set]))

(def idea-shape
  [:map
   [:name [:string {:min 1}]]
   [:description :string]])

(defn update-idea [{:keys [query-fn]} {:keys [path-params form-params user can-delayed] :as request}]
  (let [id (:id path-params)
        idea (when id (query-fn :get-idea {:id id}))]
    (can-delayed [:ideas :edit (:user_id idea)])
    (let [add-data (fn [params]
                     (println params)
                     (merge params
                            {:user_id (:id user)
                             :tags (or (:tags idea) [])}))
          result (update-or-insert {:id id
                                    :shape idea-shape
                                    :data form-params
                                    :create-fn (comp (partial query-fn :create-idea!) add-data)
                                    :update-fn (comp (partial query-fn :update-idea!) add-data)})]
      (case (:result result)
        :validation-errors
        (layout/render request "ideas/edit.html"
                       {:id id
                        :idea (or idea (:data result))
                        :errors (:errors result)})


        (:created :updated)
        (http-response/see-other (str "/ideas/" (:id result)))

        :not-found
        (http-response/not-found)

        (:create-exception :update-exception)
        (do
          (log/error (:exception result) "update-idea" (:result result))
          (http-response/internal-server-error))))))

(defn assign-idea [{:keys [query-fn]} {:keys [path-params form-params] :as request}]
  (let [id (:id path-params)
        ;idea (query-fn :get-idea {:id id})
        ;TODO check all categories exist
        ;categories (query-fn :list-categories {})
        current (set (map :category_id (query-fn :list-idea-categories {:idea id})))
        selected-field (get form-params "category[]")
        selected (->> (if (vector? selected-field) selected-field [selected-field])
                      (map Integer/parseInt)
                      set)
        _ (println selected current (set/difference selected current))
        added (set/difference selected current)
        removed (set/difference current selected)]
    (when-not (empty? added)
      (query-fn :link-ideas-categories! {:links (map vector (repeat id) added)}))
    (when-not (empty? removed)
      (query-fn :unlink-idea-categories! {:idea id :categories removed}))
    (http-response/see-other (str "/ideas/" id))))
