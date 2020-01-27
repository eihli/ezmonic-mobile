(ns env.pro.core
  (:require ezmonic.prod
            ezmonic.config))

(def config {}) ; default is pro/release

(defn init []
  (swap!
   ezmonic.config/config
   #(merge % config))
  (ezmonic.prod/init))
