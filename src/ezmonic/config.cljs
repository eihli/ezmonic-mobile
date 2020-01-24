(ns ezmonic.config)

;; Defaults are for a release "pro"/paid version.
(def config
  (atom
   {:env :release
    :flavor :pro
    :max-saved js/Infinity
    :max-phrase-options js/Infinity
    :max-length-mnemonic js/Infinity}))
