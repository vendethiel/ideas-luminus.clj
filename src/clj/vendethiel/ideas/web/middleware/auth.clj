(ns vendethiel.ideas.web.middleware.auth
  (:require
   [reitit.ring :as ring]
   [clojure.set :as set]))

(defn login-middleware [handler]
  (fn [{:keys [query-fn] :as request}]
    (if-let [user (get-in request [:session :user-id])]
      ;; TODO check user is found
      (handler (assoc request :user (query-fn :get-user-profile {:id user})))
      (handler request))))

(defn roles-middleware [handler]
  (fn [{:keys [user] :as request}]
    (let [roles (cond
                  (and user (:is_admin user)) #{:user :admin}
                  user #{:user}
                  :else #{:anon})
          required (-> request (ring/get-match) :data :roles)]
      (if (and (seq required) (not (set/subset? required roles)))
        {:status 403 :body "Unauthorized"}
        (handler request)))))
