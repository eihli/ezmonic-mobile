(ns env.dev.free.core
  (:require ezmonic.core
            ezmonic.config))

(def config
  {:env :dev
   :flavor :free
   :max-saved-mnemonics 3
   :max-phrase-options 5})

(defn init []
  (swap!
   ezmonic.config/config
   #(merge % config))
  (ezmonic.core/init))
