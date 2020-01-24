(ns env.dev.pro.core
  (:require ezmonic.core
            ezmonic.config))

(def config
  {:env :dev
   :flavor :pro})

(defn init []
  (reset! ezmonic.config/config config)
  (ezmonic.core/init))
