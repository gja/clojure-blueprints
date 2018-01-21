(ns log-processing.custom-transducer)

(ns log-processing.transducer
  (:import [java.io PrintWriter InputStream])
  (:require [log-processing.list-files :as list-files]
            [log-processing.get-records :as records]))

(defn records-transform [xf]
  (fn
    ([]
     (xf))
    ([result]
     (xf result))
    ([result next]
     (if (= "allocation" (:type next))
       (xf result (:country next))
       result))))

(defn- process-single-file [bucket object]
  (->> object
       (records/object->records bucket)
       (transduce records-transform
                  (fn
                    ([x]
                     (.println ^PrintWriter *err* (str "Done Processing: " object))
                     (persistent! x))
                    ([acc country]
                     (assoc! acc country (inc (get acc country 0)))))
                  (transient {}))))

(defn- repeated-ditto [xf]
  (let [current-item (atom nil)]
    (fn
      ([]
       (xf))
      ([result]
       (-> result (xf "done") (xf "done")))
      ([result next]
       (if (= @current-item next)
         (xf result "ditto")
         (xf result (reset! current-item next)))))))

(defn count-number-of-allocations-by-country [bucket prefix]
  (->> prefix
       (list-files/list-files bucket)
       (pmap #(process-single-file bucket %))
       (reduce #(merge-with + %1 %2) {})))

;; log-processing.transducer> (time (count-number-of-allocations-by-country "clojure-blueprints" "chapter-2/20171124"))
;; Fetching: chapter-2/2017112400.log.gz
;; Fetching: chapter-2/2017112423.log.gz
;; Fetching: chapter-2/2017112422.log.gz
;; Fetching: chapter-2/2017112421.log.gz
;; Fetching: chapter-2/2017112420.log.gz
;; Fetching: chapter-2/2017112416.log.gz
;; Fetching: chapter-2/2017112419.log.gz
;; Fetching: chapter-2/2017112415.log.gz
;; Fetching: chapter-2/2017112418.log.gz
;; Fetching: chapter-2/2017112417.log.gz
;; Fetching: chapter-2/2017112414.log.gz
;; Fetching: chapter-2/2017112409.log.gz
;; Fetching: chapter-2/2017112412.log.gz
;; Fetching: chapter-2/2017112410.log.gz
;; Fetching: chapter-2/2017112411.log.gz
;; Fetching: chapter-2/2017112407.log.gz
;; Fetching: chapter-2/2017112413.log.gz
;; Fetching: chapter-2/2017112408.log.gz
;; Fetching: chapter-2/2017112406.log.gz
;; Fetching: chapter-2/2017112404.log.gz
;; Fetching: chapter-2/2017112403.log.gz
;; Fetching: chapter-2/2017112401.log.gz
;; Fetching: chapter-2/2017112405.log.gz
;; Fetching: chapter-2/2017112402.log.gz
;; Done Processing: chapter-2/2017112413.log.gz
;; Done Processing: chapter-2/2017112406.log.gz
;; Done Processing: chapter-2/2017112401.log.gz
;; Done Processing: chapter-2/2017112405.log.gz
;; Done Processing: chapter-2/2017112404.log.gz
;; Done Processing: chapter-2/2017112416.log.gz
;; Done Processing: chapter-2/2017112423.log.gz
;; Done Processing: chapter-2/2017112409.log.gz
;; Done Processing: chapter-2/2017112402.log.gz
;; Done Processing: chapter-2/2017112400.log.gz
;; Done Processing: chapter-2/2017112414.log.gz
;; Done Processing: chapter-2/2017112410.log.gz
;; Done Processing: chapter-2/2017112407.log.gz
;; Done Processing: chapter-2/2017112418.log.gz
;; Done Processing: chapter-2/2017112421.log.gz
;; Done Processing: chapter-2/2017112415.log.gz
;; Done Processing: chapter-2/2017112419.log.gz
;; Done Processing: chapter-2/2017112420.log.gz
;; Done Processing: chapter-2/2017112422.log.gz
;; Done Processing: chapter-2/2017112411.log.gz
;; Done Processing: chapter-2/2017112408.log.gz
;; Done Processing: chapter-2/2017112417.log.gz
;; Done Processing: chapter-2/2017112403.log.gz
;; Done Processing: chapter-2/2017112412.log.gz
;; "Elapsed time: 9912.217378 msecs"
;; {"India" 746997, "USA" 765247, "Australia" 730104, "Japan" 713040}
