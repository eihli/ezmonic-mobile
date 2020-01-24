(ns env.dev.pro.core
  (:require ezmonic.core
            ezmonic.config))

(def config
  {:env :dev})

(defn init []
  (swap!
   ezmonic.config/config
   #(merge % config))
  (ezmonic.core/init))
