(ns log-processing.get-records
  (:import [com.amazonaws.services.s3 AmazonS3ClientBuilder]
           [java.util Scanner]
           [java.util.zip GZIPInputStream]
           [java.io PrintWriter InputStream])
  (:require [cheshire.core :as json]))

(set! *warn-on-reflection* true)

(defn- row->record [row]
  (json/parse-string row keyword))

(defn- scanner->records [^Scanner scanner]
  (lazy-seq
   (if (.hasNext scanner)
     (cons
      (row->record (.next scanner))
      (scanner->records scanner))
     (do (.close scanner)
         nil))))

(defn- stream->records [^InputStream stream]
  (let [scanner (doto (new Scanner stream)
                  (.useDelimiter "\n"))]
    (scanner->records scanner)))

(defn object->records [^String bucket ^String key]
  (.println ^PrintWriter *err* (str "Fetching: " key))
  (let [client (AmazonS3ClientBuilder/defaultClient)
        stream (->> key
                    (.getObject client bucket)
                    .getObjectContent
                    (new GZIPInputStream))]
    (stream->records stream)))
