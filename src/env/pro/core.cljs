(ns env.pro.core
  (:require ezmonic.core
            ezmonic.config))

(def config {}) ; default is pro/release

(defn init []
  (swap!
   ezmonic.config/config
   #(merge % config))
  (ezmonic.core/init))
