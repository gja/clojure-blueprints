(defproject core-async-chat-server "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [ring "1.6.3"]
                 [commons-codec "1.11"]]
  :plugins [[lein-ring "0.12.1"]]
  :ring {:handler core-async-chat-server.core/ring-handler
         :auto-reload? true
         :port 3000
         :nrepl {:start? true :port 3001}}
  :main ^:skip-aot core-async-chat-server.core
  :profiles {:uberjar {:aot :all}})
