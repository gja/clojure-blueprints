(ns log-processing.reduce-over-records
  (:import [java.io PrintWriter InputStream])
  (:require [log-processing.list-files :as list-files]
            [log-processing.get-records :as records]))

(defn- process-single-file [bucket object transducer]
  (->> object
       (records/object->records bucket)
       (transduce transducer
                  (fn
                    ([x]
                     (.println ^PrintWriter *err* (str "Done Processing: " object))
                     (persistent! x))
                    ([acc item]
                     (assoc! acc item (inc (get acc item 0)))))
                  (transient {}))))

(defn reduce-over-records [bucket prefix transducer]
  (->> prefix
       (list-files/list-files bucket)
       (pmap #(process-single-file bucket % transducer))
       (reduce #(merge-with + %1 %2) {})))


;; Transducers here

(def return-allocations-by-country
  (comp
   (filter #(= "allocation" (:type %)))
   (map :country)))

;; (reduce-over-records "clojure-blueprints" "chapter-2/20171124" return-allocations-by-country)
;; => {"India" 746997, "USA" 765247, "Australia" 730104, "Japan" 713040}

(def conversions-by-country-and-bucket
  (comp
   (filter #(= "conversion" (:type %)))
   (map (fn [{:keys [country bucket]}]
          [country bucket]))))

;; (reduce-over-records "clojure-blueprints" "chapter-2/20171124" conversions-by-country-and-bucket)
;;=> {["Japan" "bucket-3"] 53023,
;;    ["Japan" "bucket-2"] 44292,
;;    ["USA" "bucket-1"] 22074,
;;    ["India" "bucket-4"] 53195,
;;    ["India" "bucket-1"] 26452,
;;    ["India" "bucket-2"] 35759,
;;    ["USA" "bucket-3"] 39789,
;;    ["USA" "bucket-4"] 48413,
;;    ["India" "bucket-3"] 43833,
;;    ["Australia" "bucket-1"] 31205,
;;    ["Australia" "bucket-2"] 39763,
;;    ["Australia" "bucket-4"] 57603,
;;    ["Japan" "bucket-4"] 61764,
;;    ["Japan" "bucket-1"] 35601,
;;    ["USA" "bucket-2"] 31216,
;;    ["Australia" "bucket-3"] 48630}

(defn count-conversions-and-allocations-by-bucket [xf]
  (fn
    ([] (xf))
    ([result] (xf result))
    ([result {:keys [bucket type]}]
     (case type
       "allocation" (xf result [bucket :allocation])
       "conversion" (xf result [bucket :conversion])
       result))))

;; (reduce-over-records "clojure-blueprints" "chapter-2/20171124" count-conversions-and-allocations-by-bucket)
;; => {["bucket-1" :allocation] 790938,
;;     ["bucket-1" :conversion] 115332,
;;     ["bucket-2" :allocation] 757366,
;;     ["bucket-2" :conversion] 151030,
;;     ["bucket-3" :allocation] 721544,
;;     ["bucket-3" :conversion] 185275,
;;     ["bucket-4" :allocation] 685540,
;;     ["bucket-4" :conversion] 220975}


(def count-conversions-and-allocations-by-bucket-for-usa
  (comp
   (filter #(= "USA" (:country %)))
   count-conversions-and-allocations-by-bucket))

;; (reduce-over-records "clojure-blueprints" "chapter-2/20171124" count-conversions-and-allocations-by-bucket-for-usa)

;; => {["bucket-1" :allocation] 203989,
;;     ["bucket-1" :conversion] 22074,
;;     ["bucket-2" :allocation] 196579,
;;     ["bucket-2" :conversion] 31216,
;;     ["bucket-3" :allocation] 187246,
;;     ["bucket-3" :conversion] 39789,
;;     ["bucket-4" :allocation] 177433,
;;     ["bucket-4" :conversion] 48413}
