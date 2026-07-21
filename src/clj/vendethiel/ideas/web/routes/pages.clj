(ns vendethiel.ideas.web.routes.pages
  (:require
    [vendethiel.ideas.web.middleware.exception :as exception]
    [vendethiel.ideas.web.pages.layout :as layout]
    [integrant.core :as ig]
    [reitit.ring.middleware.muuntaja :as muuntaja]
    [reitit.ring.middleware.parameters :as parameters]
    [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
    [vendethiel.ideas.web.pages.categories :as categories]
    [vendethiel.ideas.web.pages.ideas :as ideas]
    [vendethiel.ideas.web.pages.implementations :as implementations]))

(defn wrap-page-defaults []
  (let [error-page (layout/error-page
                     {:status 403
                      :title "Invalid anti-forgery token"})]
    #(wrap-anti-forgery % {:error-response error-page})))

;; Routes
(defn page-routes [opts]
  [["/" {:get (partial categories/list-categories opts)
         }]
   ["/categories"
    {}
    ["/:id" {:get {:handler (partial categories/get-category opts)
                   :parameters {:path [:map [:id int?]]}}}]
    ]

   ["/ideas"
    {}
    ["/:id" {:get {:handler (partial ideas/get-idea opts)
                   :parameters {:path [:map [:id int?]]}}}]
    ]

   ["/implementations"
    {}
    ["/:id" {:get {:handler (partial implementations/get-implementation opts)
                   :parameters {:path [:map [:id int?]]}}}]
    ]
   ])

(def route-data
  {:middleware 
   [;; Default middleware for pages
    (wrap-page-defaults)
    ;; query-params & form-params
    parameters/parameters-middleware
    ;; encoding response body
    muuntaja/format-response-middleware
    ;; exception handling
    exception/wrap-exception]})

(derive :reitit.routes/pages :reitit/routes)

(defmethod ig/init-key :reitit.routes/pages
  [_ {:keys [base-path]
      :or   {base-path ""}
      :as   opts}]
  (layout/init-selmer! opts)
  (fn [] [base-path route-data (page-routes opts)]))

