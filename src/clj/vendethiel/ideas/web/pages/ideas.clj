(ns vendethiel.ideas.web.pages.ideas
  (:require
   [jsonista.core :as j]
   [vendethiel.ideas.web.pages.layout :as layout]))

(defn get-idea [{:keys [query-fn]} {:keys [path-params can-delayed] :as request}]
  (let [idea (query-fn :get-idea-details
                       {:id (:id path-params)})
        data (map (juxt identity #(j/read-value (%1 idea)))
                  [:tags :comments :categories :implementations])]
    (can-delayed [:ideas :read (:user_id idea)])
    (layout/render request "ideas/show.html"
                   (into {:idea idea} data))))

(defn edit-idea [{:keys [query-fn]} {:keys [path-params can-delayed] :as request}]
  (let [id (:id path-params)
        idea (when id (query-fn :get-idea {:id id}))]
    (can-delayed [:ideas (if id :edit :new) (:user_id idea)])
    (layout/render request "ideas/edit.html" {:id id :idea idea})))
