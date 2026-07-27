(ns vendethiel.ideas.web.controllers.users
  (:require
   [malli.core :as m]
   [vendethiel.ideas.web.routes.utils :refer [form-transformer]]
   [vendethiel.ideas.web.pages.layout :as layout]
   [ring.util.http-response :as http-response]
   [malli.error :as me]))


(def register-shape
  [:and
   [:map
    [:email [:string {:min 1}]] ;; TODO proper filter
    [:username [:string {:min 1}]]
    [:pass [:string {:min 8}]]
    [:pass_confirmation [:string {:min 1}]]
    ]
   [:fn {:error/message "Passwords don't match"
         :error/path [:pass]}
    (fn [{:keys [pass pass_confirmation]}]
      (= pass pass_confirmation))]])

0(defn register [{:keys [query-fn]} {:keys [form-params] :as request}]
  (let [data (m/decode register-shape form-params form-transformer)
        errors (me/humanize (m/explain register-shape data))]
    (if errors
      (layout/render request "users/new.html"
                     {:data data :errors errors})
      (let [created (query-fn :create-user!
                              (merge data
                                     {:admin false :is_active true}))]
        (-> (http-response/see-other "/")
            (assoc-in [:session :user-id] (:id created)))))))

;;(defn register2 [{:keys [query-fn]}] {:keys [form-params] :as request}
;;  (let [result (update-or-insert {:shape register-shape
;;                                  :data form-params
;;                                  :create-fn (partial query-fn :create-user!)
;;                                  })]))
