(ns vendethiel.ideas.web.controllers.auth
  (:require [clojure.tools.logging :as log]
            [ring.util.http-response :as http-response]
            [malli.core :as m]
            [malli.error :as me]
            [clojure.walk :refer [keywordize-keys]]
            [vendethiel.ideas.web.pages.layout :as layout]))


(def login-shape
  [:map
   [:email [:string {:min 1}]]
   [:pass [:string {:min 1}]]])


(defn login [{:keys [query-fn]} {:keys [form-params] :as request}]
  (let [data (keywordize-keys form-params)
        errors (m/explain login-shape data)]
    (if errors
      (layout/render request "auth/login.html"
                     {:email (:email data)
                      :errors (me/humanize errors)})
      (let [logged-in-id (query-fn :login-user data)
            _ (println "login:" logged-in-id)]
        (if logged-in-id
          (-> (http-response/see-other "/")
              (assoc-in [:session :user-id] logged-in-id))
          (layout/render request "auth/login.html"
                         {:email (:email data)
                          :errors {:email ["User not found"]}}))))))

(defn logout [_ request]
  (-> (http-response/see-other "/")
      (update-in [:session] dissoc :user-id)))
