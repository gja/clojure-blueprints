(defproject log-processing "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [cheshire "5.8.0"]
                 [com.amazonaws/aws-java-sdk-s3 "1.11.250"]]
  :main ^:skip-aot log-processing.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
