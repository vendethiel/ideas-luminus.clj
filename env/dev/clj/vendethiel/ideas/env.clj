(ns vendethiel.ideas.env
  (:require
    [clojure.tools.logging :as log]
    [vendethiel.ideas.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init       (fn []
                 (log/info "\n-=[ideas starting using the development or test profile]=-"))
   :start      (fn []
                 (log/info "\n-=[ideas started successfully using the development or test profile]=-"))
   :stop       (fn []
                 (log/info "\n-=[ideas has shut down successfully]=-"))
   :middleware wrap-dev
   :opts       {:profile       :dev}})
