(ns vendethiel.ideas.web.routes.pages
  (:require
    [vendethiel.ideas.web.middleware.exception :as exception]
    [vendethiel.ideas.web.pages.layout :as layout]
    [integrant.core :as ig]
    [reitit.ring.middleware.muuntaja :as muuntaja]
    [reitit.ring.middleware.parameters :as parameters]
    [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
    [vendethiel.ideas.web.middleware.auth :refer [login-middleware roles-can-middleware]]
    [vendethiel.ideas.web.controllers.auth :as cauth]
    [vendethiel.ideas.web.controllers.categories :as ccategories]
    [vendethiel.ideas.web.controllers.users :as cusers]
    [vendethiel.ideas.web.pages.auth :as auth]
    [vendethiel.ideas.web.pages.categories :as categories]
    [vendethiel.ideas.web.pages.ideas :as ideas]
    [vendethiel.ideas.web.pages.implementations :as implementations]
    [vendethiel.ideas.web.pages.users :as users]
    ))

(defn wrap-page-defaults []
  (let [error-page (layout/error-page
                     {:status 403
                      :title "Invalid anti-forgery token"})]
    #(wrap-anti-forgery % {:error-response error-page})))

;; Routes
(defn page-routes [opts]
  [["/" {:name :list-categories
         :get (partial categories/list-categories opts)
         }]

   ["/categories"
    {}
    ["/" {:post {:handler (partial ccategories/update-category opts)
                 :can [:categories :new]
                 :parameters {:body ccategories/category-shape}}}]
    ["/new" {:name :new-category
             :conflicting true
             :get {:handler (partial categories/edit-category opts)
                   :can [:categories :new]}}]
    ["/:id" {:name :get-category
             :conflicting true
             :parameters {:path {:id int?}}
             :get (partial categories/get-category opts)
             :post {:handler (partial ccategories/update-category opts)
                    :roles #{:admin}
                    :body ccategories/category-shape}
             :delete {:handler (partial ccategories/delete-category opts)
                      :roles #{:admin}
                      }}]
    ["/:id/edit" {:name :edit-category
                  :get (partial categories/edit-category opts)}]
    ]

   ["/ideas"
    {}
    ["/:id" {:name :get-idea
             :get {:handler (partial ideas/get-idea opts)
                   :parameters {:path {:id int?}}}}]
    ]

   ["/implementations"
    {}
    ["/:id" {:name :get-implementation
             :get {:handler (partial implementations/get-implementation opts)
                   :parameters {:path {:id int?}}}}]
    ]

   ["/login"
    {:name :login
     :roles #{:anon}
     :get (partial auth/login-form opts)
     :post (partial cauth/login opts)}]
   ["/logout"
    {:name :register
     :roles #{:user}
     :post (partial cauth/logout opts)}]
   ["/users"
    {}
    [["/new"
      {:name :logout
       :conflicting true
       :roles #{:anon}
       :get (partial users/register-form opts)
       :post (partial cusers/register opts)}]
     ["/:id"
      {:name :get-user
       :conflicting true
       :get (partial users/get-user opts)
       :parameters {:path {:id int?}}}]]]
   ])

(defn route-data [opts]
  {:middleware 
   [;; Default middleware for pages
    (wrap-page-defaults)
    ;; query-params & form-params
    parameters/parameters-middleware
    ;; encoding response body
    muuntaja/format-response-middleware
    ;; exception handling
    exception/wrap-exception
    ;; auth
    (login-middleware opts)
    roles-can-middleware
    ]})

(derive :reitit.routes/pages :reitit/routes)

(defmethod ig/init-key :reitit.routes/pages
  [_ {:keys [base-path]
      :or   {base-path ""}
      :as   opts}]
  (layout/init-selmer! opts)
  (fn [] [base-path (route-data opts) (page-routes opts)]))
