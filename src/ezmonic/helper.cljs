(ns ezmonic.helper)

(defn ->clj
  "The same as js->clj with keywordize-keys set to true, but just
  shorter to type."
  [arg]
  (-> arg
      (js->clj :keywordize-keys true)))
