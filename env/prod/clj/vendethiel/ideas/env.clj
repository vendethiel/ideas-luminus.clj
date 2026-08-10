(ns vendethiel.ideas.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init       (fn []
                 (log/info "\n-=[ideas starting]=-"))
   :start      (fn []
                 (log/info "\n-=[ideas started successfully]=-"))
   :stop       (fn []
                 (log/info "\n-=[ideas has shut down successfully]=-"))
   :middleware (fn [handler _] handler)
   :opts       {:profile :prod}})
