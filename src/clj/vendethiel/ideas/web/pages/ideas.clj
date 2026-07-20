(ns vendethiel.ideas.web.pages.ideas
  (:require
   [jsonista.core :as j]
   [vendethiel.ideas.web.pages.layout :as layout]))

(defn get-idea [{:keys [query-fn]} {:keys [path-params] :as request}]
  (let [id (parse-long (:id path-params))
        idea (query-fn :get-idea-details {:id id})
        data (map (juxt identity #(j/read-value (%1 idea)))
                  [:tags :comments :categories :implementations])
        ]
    (layout/render request "ideas/show.html"
                   (into {:idea idea} data))))
