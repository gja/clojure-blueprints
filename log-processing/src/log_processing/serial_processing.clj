(ns log-processing.serial-processing
  (:require [log-processing.list-files :as list-files]
            [log-processing.get-records :as records]))

(defn- get-all-records [bucket prefix]
  (->> prefix
       (list-files/list-files bucket)
       (mapcat #(records/object->records bucket %))))

(defn count-number-of-allocations-by-country [bucket prefix]
  (->> (get-all-records bucket prefix)
       (filter #(= "allocation" (:type %)))
       (map :country)
       (reduce (fn [acc country]
                 (update acc country #(inc (or % 0))))
               {})))

;; (time (count-number-of-allocations-by-country "clojure-blueprints" "chapter-2/20171124"))
;; Fetching: chapter-2/2017112400.log.gz
;; Fetching: chapter-2/2017112401.log.gz
;; Fetching: chapter-2/2017112402.log.gz
;; Fetching: chapter-2/2017112403.log.gz
;; Fetching: chapter-2/2017112404.log.gz
;; Fetching: chapter-2/2017112405.log.gz
;; Fetching: chapter-2/2017112406.log.gz
;; Fetching: chapter-2/2017112407.log.gz
;; Fetching: chapter-2/2017112408.log.gz
;; Fetching: chapter-2/2017112409.log.gz
;; Fetching: chapter-2/2017112410.log.gz
;; Fetching: chapter-2/2017112411.log.gz
;; Fetching: chapter-2/2017112412.log.gz
;; Fetching: chapter-2/2017112413.log.gz
;; Fetching: chapter-2/2017112414.log.gz
;; Fetching: chapter-2/2017112415.log.gz
;; Fetching: chapter-2/2017112416.log.gz
;; Fetching: chapter-2/2017112417.log.gz
;; Fetching: chapter-2/2017112418.log.gz
;; Fetching: chapter-2/2017112419.log.gz
;; Fetching: chapter-2/2017112420.log.gz
;; Fetching: chapter-2/2017112421.log.gz
;; Fetching: chapter-2/2017112422.log.gz
;; Fetching: chapter-2/2017112423.log.gz
;; "Elapsed time: 85661.10999 msecs"
;; {"India" 746997, "USA" 765247, "Australia" 730104, "Japan" 713040}
