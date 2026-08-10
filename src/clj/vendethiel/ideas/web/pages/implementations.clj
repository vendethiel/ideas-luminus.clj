(ns vendethiel.ideas.web.pages.implementations
  (:require
   [jsonista.core :as j]
   [vendethiel.ideas.web.pages.layout :as layout]
   [ring.util.http-response :as http-response]))

(defn get-implementation [{:keys [query-fn]} {:keys [path-params can-delayed] :as request}]
  (let [implementation (query-fn :get-implementation-details
                                 {:id (:id path-params)})
        data (map (juxt identity #(j/read-value (%1 implementation)))
                  [:comments :tags :idea_tags])]
    (can-delayed [:implementations :read (:user_id implementation)])
    (layout/render request "implementations/show.html"
                   (into {:implementation implementation} data))))

(defn edit-implementation [{:keys [query-fn]} {:keys [path-params can-delayed] {:strs [idea]} :query-params :as request}]
  (let [id (:id path-params)
        implementation (when id (query-fn :get-implementation {:id id}))
        for-idea (query-fn :get-idea {:id
                                      (if id (:idea_id implementation) idea)})]
    (can-delayed [:implementations (if id :edit :new) (:user_id implementation)])
    (println (:query-params request))
    (if (or (not for-idea) (and id (not implementation)))
      (http-response/not-found)
      (layout/render request "implementations/edit.html" {:id id
                                                          :implementation implementation
                                                          :idea for-idea}))))
