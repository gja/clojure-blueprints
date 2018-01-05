(ns log-processing.list-files
  (:import [com.amazonaws.services.s3 AmazonS3Client AmazonS3ClientBuilder]
           [com.amazonaws.services.s3.model ObjectListing S3ObjectSummary]))

(set! *warn-on-reflection* true)

(defn- iterate-results [^AmazonS3Client client ^ObjectListing results]
  (lazy-cat (.getObjectSummaries results)
            (when (.isTruncated results)
              (iterate-results client (.listNextBatchOfObjects client results)))))

(defn- listing->key [^S3ObjectSummary summary]
  (.getKey summary))

(defn list-files [^String bucket ^String prefix]
  (let [client (AmazonS3ClientBuilder/defaultClient)
        results (.listObjects client bucket prefix)]
    (map listing->key (iterate-results client results))))
