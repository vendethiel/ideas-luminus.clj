(ns vendethiel.ideas.web.routes.utils
  (:require
   [malli.core :as m]
   [malli.error :as me]
   [malli.transform :as mt]))

(def route-data-path [:reitit.core/match :data])

(defn route-data
  [req]
  (get-in req route-data-path))

(defn route-data-key
  [req k]
  (get-in req (conj route-data-path k)))

(def form-transformer
  "Transformer used for form-params, keywordize and trim extra keys for safety"
  (mt/transformer
   (mt/key-transformer {:decode keyword})
   mt/strip-extra-keys-transformer
   ))

(defn update-or-insert [{:keys [id create-fn update-fn data shape]}]
  (let [data (m/decode shape data form-transformer)
        errors (m/explain shape data)]
    (if errors
      {:result :validation-error :data data
       :errors (me/humanize errors) :raw-errors errors}
      (if id
        (try
          (let [updated (merge data {:id id})
                update (update-fn updated)]
            (if (= 1 (:next.jdbc/update-count update))
              {:result :updated :id id :data updated}
              {:result :not-found}))
          (catch Exception e
            {:type :create-exception :exception e}))
        (try
          (let [created (create-fn data)]
            {:result :created :id (:id created) :data (merge data created)})
          (catch Exception e
            {:type :update-exception :exception e}))))))
