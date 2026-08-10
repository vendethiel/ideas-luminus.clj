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
    [vendethiel.ideas.web.controllers.ideas :as cideas]
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
         :can [:categories :list]
         :get (partial categories/list-categories opts)
         }]

   ["/categories"
    {}
    ["/" {:can [:categories :new]
          :post {:handler (partial ccategories/update-category opts)
                 :parameters {:body ccategories/category-shape}}}]
    ["/new" {:name :new-category
             :conflicting true
             :can :delayed
             :get (partial categories/edit-category opts)}]
    ["/:id" {:name :get-category
             :conflicting true
             :parameters {:path {:id int?}}
             :can :delayed
             :get (partial categories/get-category opts)
             :post {:handler (partial ccategories/update-category opts)
                    :body ccategories/category-shape}
             :delete (partial ccategories/delete-category opts)}]
    ["/:id/edit" {:name :edit-category
                  :can :delayed
                  :get (partial categories/edit-category opts)}]
    ]

   ["/ideas"
    {}
    ["/" {:can :delayed
          :post (partial cideas/update-idea opts)}]
    ["/new" {:name :new-idea
             :conflicting true
             :can :delayed
             :get (partial ideas/edit-idea opts)}]
    ["/unassigned" {:name :unassigned-ideas
                    :conflicting true
                    :can [:ideas :admin]
                    :get (partial ideas/list-unassigned-ideas opts)}]
    ["/:id" {:name :get-idea
             :conflicting true
             :parameters {:path {:id int?}}
             :can :delayed
             :get (partial ideas/get-idea opts)
             :post (partial cideas/update-idea opts)}]
    ["/:id/edit" {:name :edit-idea
                  :parameters {:path {:id int?}}
                  :can :delayed
                  :get (partial ideas/edit-idea opts)}]
    ["/:id/assign" {:name :assign-idea
                    :parameters {:path {:id int?}}
                    :can [:ideas :admin]
                    :get (partial ideas/assign-idea opts)
                    :post (partial cideas/assign-idea opts)}]
    ]

   ["/implementations"
    {}
    ["/new" {:name :new-implementation
             :conflicting true
             :parameters {:query {:id int?}}
             :can :delayed
             :get (partial implementations/edit-implementation opts)}]
    ["/:id" {:name :get-implementation
             :conflicting true
             :parameters {:path {:id int?}}
             :can :delayed
             :get (partial implementations/get-implementation opts)}]
    ["/:id/edit" {:name :edit-implementation
                  :parameters {:path {:id int?}}
                  :can :delayed
                  :get (partial implementations/edit-implementation opts)}]
    ]

   ["/login"
    {:name :login
     :can [:auth :login]
     :get (partial auth/login-form opts)
     :post (partial cauth/login opts)}]
   ["/logout"
    {:name :register
     :can [:auth :logout]
     :post (partial cauth/logout opts)}]
   ["/users"
    {}
    [["/new"
      {:name :logout
       :conflicting true
       :can [:users :new]
       :get (partial users/register-form opts)
       :post (partial cusers/register opts)}]
     ["/:id"
      {:name :get-user
       :conflicting true
       :parameters {:path {:id int?}}
       :can [:users :read]
       :get (partial users/get-user opts)
       }]]]
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
