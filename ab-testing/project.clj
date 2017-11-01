(defproject ab-testing "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [cheshire "5.4.0"]
                 [ring "1.6.3"]]
  :plugins [[lein-ring "0.12.1"]]
  :ring {:handler ab-testing.core/ring-handler
         :auto-reload? true
         :port 3000
         :nrepl {:start? true :port 3001}}
  :main ^:skip-aot ab-testing.core
  :profiles {:uberjar {:aot :all}})
