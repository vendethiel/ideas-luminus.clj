(ns ideas.routes.helper.request
  (:require [noir.response :as resp]
            [noir.session :as session]))

(def ^:dynamic *filter-req-redirect-path* "/")

(defn filter-req
  ([cond fn]
    (filter-req cond fn *filter-req-redirect-path*))

  ([cond fn redirect-path]
    (if (cond)
      (fn)
      (resp/redirect redirect-path))))

;; TODO move this outside from here. this file should
;;      only be related to routing
(def ^:dynamic *is-anon!-redirect-path* "/")
(def ^:dynamic *is-auth!-redirect-path* "/")

; predicates
(defn is-anon? []
  (nil? (session/get :user-id)))

(defn is-auth? []
  (not (is-anon?)))

; redirects
(defn is-anon! [fn]
  (filter-req is-anon? fn *is-anon!-redirect-path*))

(defn is-auth! [fn]
  (filter-req is-auth? fn *is-auth!-redirect-path*))
