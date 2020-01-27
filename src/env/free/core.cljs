(ns env.free.core
  (:require ezmonic.prod
            ezmonic.config))

(def config
  {:flavor :free
   :max-saved-mnemonics 3
   :max-phrase-options 5})

(defn init []
  (swap!
   ezmonic.config/config
   #(merge % config))
  (ezmonic.prod/init))
