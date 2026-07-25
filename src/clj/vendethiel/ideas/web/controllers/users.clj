(ns vendethiel.ideas.web.controllers.users
  (:require
   [malli.core :as m]
   [vendethiel.ideas.web.routes.utils :refer [form-transformer]]
   [vendethiel.ideas.web.pages.layout :as layout]
   [ring.util.http-response :as http-response]))


(def register-shape
  [:map
   [:email [:string {:min 1}]]
   [:username [:string {:min 1}]]
   [:pass [:string {:min 1}]]
   [:pass_confirmation [:string {:min 1}]]
   ])

;;  email TEXT,
;;  username TEXT,
;;  admin BOOLEAN NOT NULL,
;;  last_login TIME,
;;  is_active BOOLEAN NOT NULL,
;;  pass TEXT
(defn register [{:keys [query-fn]} {:keys [form-params] :as request}]
  (let [data (m/decode register-shape form-params form-transformer)
        errors (m/explain register-shape data)]
    (if errors
      (layout/render request "users/register.html"
                     {:data data :errors errors})
      (let [created (query-fn :create-user!
                              (merge data
                                     {:is_admin false :is_active true}))]
        (-> (http-response/see-other "/")
              (assoc-in [:session :user-id] (:id created)))))))

;;(defn register2 [{:keys [query-fn]}] {:keys [form-params] :as request}
;;  (let [result (update-or-insert {:shape register-shape
;;                                  :data form-params
;;                                  :create-fn (partial query-fn :create-user!)
;;                                  })]))
