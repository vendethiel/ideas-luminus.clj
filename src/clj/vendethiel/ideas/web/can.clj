(ns vendethiel.ideas.web.can
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::can-ns #{:users :categories :ideas :implementations :comments})
; this version of ::can-action doesn't have -own
(s/def ::can-action #{:list :read :new :edit :delete :admin})
(s/def ::auth-action #{:login :logout :api})
(s/def ::can (s/or :delayed #{:delayed}
                   :object (s/tuple ::can-ns ::can-action)
                   :auth (s/tuple #{:auth} ::auth-action)))
(s/def ::can-fn (s/or :object (s/tuple ::can-ns ::can-action (s/? int?))
                      :auth (s/tuple #{:auth} ::auth-action)))

(defn check-can [user user-can [main sub & object-user-id]]
  (let [can (get-in user-can [main sub])
        own-sub (keyword (str (name sub) "-own"))
        own-can (when (and object-user-id (= (:id user) object-user-id))
                  (get-in user-can [main own-sub]))]
    (or (:admin user) can own-can)))
(s/fdef check-can
  :args (s/cat :user any? :user-can any? :check ::can-fn)
  :ret boolean?)

(def CAN_READ {:list true :read true})
(def CAN_RW   {:list true :read true :new true})
(def CAN_ALL  {:list true :read true :new true :edit true :delete true})
(def CAN_SELF {:list true :read true :read-own true :new true :edit-own true :delete-own true})
(def LOGGED_OUT_CANS
  {:auth {:login true :register true} :users CAN_RW
   :categories CAN_READ :ideas CAN_READ
   :implementations CAN_READ :comments CAN_READ})

(defn user-cans
  "Generates a list of 'can-do's for a user"
  [user]
  (if user
    (if (:admin user)
      {:auth {:logout true} :users CAN_ALL
       :categories CAN_ALL :ideas CAN_ALL
       :implementations CAN_ALL :comments CAN_ALL}
      {:auth {:logout true} :users CAN_READ
       :categories CAN_READ :ideas CAN_READ
       :implementations CAN_SELF :comments CAN_SELF})
    LOGGED_OUT_CANS))
