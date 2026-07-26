(ns vendethiel.ideas.web.middleware.auth
  (:require
   [reitit.ring :as ring]
   [clojure.set :as set]))

(defn login-middleware [{:keys [query-fn]}]
  (fn [handler]
    (fn [request]
      (if-let [user (get-in request [:session :user-id])]
        (if user
          (handler (assoc request :user (query-fn :get-user-profile {:id user})))
          (handler request))
        (handler request)))))

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
