(ns core-async-chat-server.core
  (:import [org.apache.commons.codec.digest DigestUtils]
           [org.apache.commons.codec.binary Base64])
  (:require [ring.adapter.jetty9 :refer [run-jetty]]
            [ring.adapter.jetty9.websocket :as ws]
            [cheshire.core :as json])
  (:gen-class))

(def ws-handler {})

(defn app [request]
  {:status 200
   :body "pong"})

(def channel-registry
  (atom {:channel-to-sockets {}
         :socket-to-channels {}}))

(defn- add-to-set [set item]
  (conj (or set #{})
        item))

(defn- process-message [ws {:keys [type channel] :as m}]
  (case type
    "join" (do (swap! channel-registry update-in [:channel-to-sockets channel] add-to-set ws)
               (swap! channel-registry update-in [:socket-to-channels ws] add-to-set channel)
               (prn (str "Joining Channel:" channel)))
    "msg" (let [message (json/encode {:message (:message m)})]
            (doseq [socket (get-in @channel-registry [:channel-to-sockets channel])]
              (ws/send! socket message)))
    "leave" (swap! channel-registry update-in [:socket-to-channels ws] disj channel)))

(defn- remove-socket [ws status-code reason]
  (prn "Closing Socket")
  (let [channels (get-in @channel-registry [:socket-to-channels ws] #{})]
    (swap! channel-registry update-in [:socket-to-channels] dissoc ws)
    (doseq [channel channels]
      (swap! channel-registry update-in [:channel-to-sockets channel] disj ws))))

(defn -main [& args]
  (run-jetty app {:port 3000
                  :websockets {"/connect" {:on-connect (fn [ws]
                                                         (prn "connected"))
                                           :on-text (fn [ws text]
                                                      (process-message ws (json/decode text keyword)))
                                           :on-close remove-socket}}
                  :allow-null-path-info true}))
