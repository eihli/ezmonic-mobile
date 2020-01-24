(ns env.dev.free.core
  (:require ezmonic.core
            ezmonic.config))

(def config
  {:env :dev
   :flavor :free})

(defn init []
  (reset! ezmonic.config/config config)
  (ezmonic.core/init))
