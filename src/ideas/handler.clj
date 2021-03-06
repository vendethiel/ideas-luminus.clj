(ns ideas.handler
  (:require [compojure.core :refer [defroutes]]
            [ideas.middleware :as middleware]
            [ideas.online :refer [update-online-list]]
            [noir.util.middleware :refer [app-handler]]
            [compojure.route :as route]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.3rd-party.rotor :as rotor]
            [selmer.parser :as parser]
            [environ.core :refer [env]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [ideas.routes.auth :refer [auth-routes]]
            [ideas.routes.cljs :refer [cljs-routes]]
            [ideas.routes.home :refer [home-routes]]
            [ideas.routes.ideas :refer [ideas-routes]]
            [ideas.routes.implementations :refer [implementations-routes]]
            [ideas.routes.users :refer [users-routes]]))

(defroutes
  app-routes
  (route/resources "/")
  (route/not-found "Not Found"))

(defn init
  "init will be called once when
   app is deployed as a servlet on
   an app server such as Tomcat
   put any initialization code here"
  []
  (timbre/set-config!
    {:appenders {:rotor {:min-level :info
                         :enabled? true
                         :async? false
                         :max-message-per-msecs nil
                         :fn rotor/rotor-appender}}
     :shared-appender-config {:rotor {:path "ideas.log"
                                      :max-size (* 512 1024)
                                      :backlog 10}}})

  (parser/add-tag! :csrf-token (fn [_ _] (anti-forgery-field)))
  (if (env :dev) (parser/cache-off!))

  (timbre/info "ideas started successfully"))

(defn destroy
  "destroy will be called when your application\r
   shuts down, put any clean up code here"
  []
  (timbre/info "ideas is shutting down..."))

(def app
  (app-handler
    [home-routes auth-routes
     ideas-routes implementations-routes users-routes
     cljs-routes app-routes]
    :middleware [update-online-list
                 middleware/template-error-page
                 middleware/log-request]
    :access-rules []
    :formats [:json-kw :edn]))
