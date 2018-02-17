(ns core-async-chat-server.core
  (:import [org.apache.commons.codec.digest DigestUtils]
           [org.apache.commons.codec.binary Base64])
  (:gen-class))

(set! *warn-on-reflection* true)

(defn- sha1 [^String str]
  (-> str
      DigestUtils/sha1
      Base64/encodeBase64String))

(defn route-request [{:keys [body uri request-method headers] :as request}]
  (case [request-method uri]
    [:get "/websocket"]
    {:status 101
     :headers {"Upgrade" "websocket"
               "Connection" "upgrade"
               "Sec-WebSocket-Accept" (-> (headers "sec-websocket-key")
                                          (str "258EAFA5-E914-47DA-95CA-C5AB0DC85B11")
                                          sha1)}}

    {:status 404
     :body (str request-method " " uri " not found")}))

(def ring-handler
  (-> route-request))
