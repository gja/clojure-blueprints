(ns ab-testing.core-test
  (:require [clojure.test :refer :all]
            [ab-testing.core :refer :all]))

(deftest pick-bucket-test
  (testing "with two buckets 75/100 and 100/200"
    (let [state {:buckets ["bucket-1", "bucket-2"]
                 :statistics {"bucket-1" {:conversions 75 :allocations 100}
                              "bucket-2" {:conversions 100 :allocations 200}}}]
      (is (= "bucket-1" (pick-bucket state 0.0000)))
      (is (= "bucket-1" (pick-bucket state 0.5999)))
      (is (= "bucket-2" (pick-bucket state 0.6001)))
      (is (= "bucket-2" (pick-bucket state 0.9999))))))

(deftest allocate-bucket-test
  (testing "it selects a bucket and updates counters"
    (reset! state {:buckets ["bucket-1", "bucket-2"]
                   :statistics {"bucket-1" {:conversions 75 :allocations 100}
                                "bucket-2" {:conversions 100 :allocations 200}}})
    (is (= "bucket-1" (allocate-bucket 0.5999)))
    (is (= 101 (get-in @state [:statistics "bucket-1" :allocations])))))

(deftest record-conversion-test
  (testing "it selects a bucket and updates counters"
    (reset! state {:buckets ["bucket-1", "bucket-2"]
                   :statistics {"bucket-1" {:conversions 75 :allocations 100}
                                "bucket-2" {:conversions 100 :allocations 200}}})
    (record-conversion "bucket-1")
    (is (= 76 (get-in @state [:statistics "bucket-1" :conversions])))))
