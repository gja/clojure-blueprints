(ns ab-testing.core
  (:gen-class))

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

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello," " World!"))
