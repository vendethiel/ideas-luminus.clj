(ns vendethiel.ideas.web.pages.layout
  (:require
   [clojure.java.io]
   [clojure.string :as str]
   [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]
   [ring.util.anti-forgery :refer [anti-forgery-field]]
   [ring.util.http-response :refer [content-type ok]]
   [ring.util.response]
   [selmer.parser :as parser]
   [vendethiel.ideas.web.can :as can]))

(def selmer-opts {:custom-resource-path (clojure.java.io/resource "html")})

(defn humanize [value]
  (let [strval (if (or (symbol? value) (keyword? value)) (name value) value)]
    (str/replace strval #"[_-]" " ")))

(defn kw-or-resolve [resolve]
  (fn [str]
    (if (= \: (first str))
      (keyword (subs str 1))
      (resolve str))))
(defn ifcan [_args {:keys [user user-can] :as context-map} content]
  (let [args (map (kw-or-resolve #(parser/resolve-arg % context-map)) _args)
        can (can/check-can user user-can args)]
    (if can
      (get-in content [:ifcan :content])
      (get-in content [:ifcant :content]))))

(defn init-selmer!
  [{:keys [env]}]
  ;; disable HTML template caching for live reloading during development
  (when (= :dev env) (parser/cache-off!))
  (parser/add-tag! :csrf-field (fn [_ _] (anti-forgery-field)))
  (parser/add-tag! :ifcan #'ifcan :ifcant :endcan)
  (parser/add-filter! :humanize humanize)
  )

(defn render
  [{:keys [user user-can] :as request} template & [params]]
  (-> (parser/render-file template
                          (assoc params
                                 :page template
                                 :csrf-token *anti-forgery-token*
                                 :user user
                                 :user-can user-can)
                          selmer-opts)
      (ok)
      (content-type "text/html; charset=utf-8")))

(defn error-page
  "error-details should be a map containing the following keys:
   :status - error status
   :title - error title (optional)
   :message - detailed error message (optional)
   returns a response map with the error page as the body
   and the status specified by the status key"
  [error-details]
  {:status  (:status error-details)
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body    (parser/render-file "error.html" error-details selmer-opts)})
