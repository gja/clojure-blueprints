(ns ab-testing.core
  (:require [cheshire.core :as json]))

(defn- state->conversion-rates [{:keys [buckets statistics]}]
  (map (fn [bucket-name]
         (let [{:keys [conversions allocations]} (get statistics bucket-name)]
           {:bucket-name bucket-name
            :conversion-rate (/ conversions allocations)}))
       buckets))

(defn pick-bucket [{:keys [buckets] :as state} random-number]
  (let [rates (state->conversion-rates state)]
    (loop [n (* random-number (->> rates
                                   (map :conversion-rate)
                                   (reduce + 0)))
           [{:keys [bucket-name conversion-rate]} & rest] rates]
      (if (<= n conversion-rate)
        bucket-name
        (recur (- n conversion-rate) rest)))))

(def state
  (atom {:buckets ["bucket-1", "bucket-2"]
         :statistics {"bucket-1" {:conversions 75 :allocations 100}
                      "bucket-2" {:conversions 100 :allocations 200}}}))

(defn allocate-bucket [random-number]
  (let [bucket (pick-bucket @state random-number)]
    (swap! state update-in [:statistics bucket :allocations] inc)
    bucket))

(defn record-conversion [bucket]
  (swap! state update-in [:statistics bucket :conversions] inc)
  bucket)

(defn route-request [{:keys [body uri request-method] :as request}]
  (case [request-method uri]
    [:post "/api/allocate"]
    {:status 201
     :body {:bucket (allocate-bucket (rand))}}

    [:post "/api/conversion"]
    {:status 201
     :body {:bucket (record-conversion (:bucket body))}}

    ([:get "/"] [:get "/api/statistics"])
    {:status 200
     :body {:statistics (:statistics @state)}}

    {:status 404
     :body (str request-method " " uri " not found")}))

(defn- wrap-format-json-response [f]
  (fn [request]
    (let [{:keys [body] :as response} (f request)]
      (if (coll? body)
        (-> response
            (update :body json/encode)
            (assoc-in [:headers "Content-Type"] "application/json"))
        response))))

(defn- wrap-parse-json-input [f]
  (fn [{:keys [body headers] :as request}]
    (if (= "application/json" (get headers "content-type"))
      (f (assoc request :body (-> body slurp (json/decode keyword))))
      (f request))))

(def ring-handler
  (-> route-request
      wrap-format-json-response
      wrap-parse-json-input))
