(ns vendethiel.ideas.web.pages.auth
  (:require
    [vendethiel.ideas.web.pages.layout :as layout]))

(defn login-form [_ request]
  (layout/render request "auth/login.html"))
