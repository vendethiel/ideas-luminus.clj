(ns vendethiel.ideas.web.middleware.auth
  (:require
   [reitit.ring :as ring]
   [clojure.set :as set]
   [vendethiel.ideas.web.can :as can]
   [ring.util.http-response :as http-response]))

(defn login-middleware [{:keys [query-fn]}]
  (fn [handler]
    (fn [request]
      (if-let [user-id (get-in request [:session :user-id])]
        (if-let [user (query-fn :get-user-profile {:id user-id})]
          (handler (assoc request
                          :user user
                          :user-can (can/user-cans user)))
          (handler (assoc request :user-can can/LOGGED_OUT_CANS)))
        (handler (assoc request :user-can can/LOGGED_OUT_CANS))))))

(defn roles-can-middleware [handler]
  (fn [{:keys [user user-can] :as request}]
    ;; TODO remove roles entirely
    (let [roles (cond
                  (and user (:admin user)) #{:user :admin}
                  user #{:user}
                  :else #{:anon})
          req-data (-> request (ring/get-match) :data)
          required-roles (:roles req-data)
          role-ko (and (seq required-roles) (not (set/subset? required-roles roles)))
          can-form (:can req-data)
          can-ko (and (some? can-form) (not (can/check-can user user-can can-form)))
        ]
      (if (or role-ko can-ko)
        (http-response/unauthorized)
        (handler request)))))
