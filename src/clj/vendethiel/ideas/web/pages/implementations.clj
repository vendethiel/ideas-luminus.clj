(ns vendethiel.ideas.web.pages.implementations
  (:require
   [jsonista.core :as j]
   [vendethiel.ideas.web.pages.layout :as layout]))

(defn get-implementation [{:keys [query-fn]} {:keys [path-params] :as request}]
  (let [implementation (query-fn :get-implementation-details
                                 {:id (:id path-params)})
        data (map (juxt identity #(j/read-value (%1 implementation)))
                  [:comments :tags :idea_tags])]
    (layout/render request "implementations/show.html"
                   (into {:implementation implementation} data))))
