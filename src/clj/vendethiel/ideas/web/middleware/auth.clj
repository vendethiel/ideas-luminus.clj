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
    (let [req-data (-> request (ring/get-match) :data)
          can-form (:can req-data)
          can-ko (and (vector? can-form) (not (can/check-can user user-can can-form)))
          can-delayed-called (atom false)
          can-delayed (when (= :delayed can-form)
                        (fn [k]
                          (reset! can-delayed-called true)
                          (can/assert-can user user-can k)))]
      (if can-ko
        (http-response/unauthorized)
        (let [resp (handler (assoc request :can-delayed can-delayed))]
          (if (and can-delayed (not @can-delayed-called))
            (http-response/internal-server-error ":can not checked!")
            resp))))))
