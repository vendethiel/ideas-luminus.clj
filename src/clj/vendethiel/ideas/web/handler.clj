(ns vendethiel.ideas.web.handler
  (:require
    [vendethiel.ideas.web.middleware.core :as middleware]
    [vendethiel.ideas.web.middleware.exception :as exception]
    [integrant.core :as ig]
    [clojure.spec.alpha :as s]
    [spec-tools.spell :as spell]
    [ring.util.response :as response]
    [reitit.ring :as ring]
    [reitit.spec :as rs]
    [reitit.dev.pretty :as pretty]
    [reitit.swagger-ui :as swagger-ui]))

(defmethod ig/init-key :handler/ring
  [_ {:keys [router api-path] :as opts}]
  (ring/ring-handler
   (router)
   (ring/routes
    ;; Handle trailing slash in routes - add it + redirect to it
    ;; https://github.com/metosin/reitit/blob/master/doc/ring/slash_handler.md
    (ring/redirect-trailing-slash-handler)
    (ring/create-resource-handler {:path "/"})
    (when (some? api-path)
      (swagger-ui/create-swagger-ui-handler {:path api-path
                                             :url  (str api-path "/swagger.json")}))
    (ring/create-default-handler
     {:not-found
      (constantly (-> {:status 404, :body "Page not found"}
                      (response/content-type "text/plain")))
      :method-not-allowed
      (constantly (-> {:status 405, :body "Not allowed"}
                      (response/content-type "text/plain")))
      :not-acceptable
      (constantly (-> {:status 406, :body "Not acceptable"}
                      (response/content-type "text/plain")))}))
   ;; XXX Should be under :data?
   {:middleware [;; logs exceptions that escape the route-level exception
                 ;; middleware (e.g. errors thrown while reading the request
                 ;; body) before they reach the server; must stay outermost
                 exception/wrap-log-exceptions
                 (middleware/wrap-base opts)]
    ;; this doesn't work, investigate why
    }))

(defmethod ig/init-key :router/routes
  [_ {:keys [routes]}]
  (mapv (fn [route]
          (if (fn? route)
            (route)
            route))
        routes))

(s/def ::can-ns #{:users :categories :ideas :implementations :comments})
; this version of ::can-action doesn't have -own
(s/def ::can-action #{:list :read :new :edit :delete})
(s/def ::auth-action #{:login :logout :api})
(s/def ::can (s/or :delayed #{:delayed}
                   :object (s/tuple ::can-ns ::can-action)
                   :auth (s/tuple #{:auth} ::auth-action)))

(defmethod ig/init-key :router/core
  [_ {:keys [routes env] :as opts}]
  (let [extra {:validate rs/validate
               :spec (s/merge
                      ::rs/default-data
                      (s/keys :req-un [::can]))
               ; TODO debug
               ;::rs/wrap spell/closed
               :exception (when (= :dev env) pretty/exception)
               }]
    (if (= env :dev)
     #(ring/router ["" opts routes] extra)
     (constantly (ring/router ["" opts routes] extra)))))
