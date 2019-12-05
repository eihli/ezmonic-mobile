(ns ezmonic.helper
  (:require ["react-native" :as rn
             :refer [Platform]
             :rename {Platform platform}]))


(defn ->clj
  "The same as js->clj with keywordize-keys set to true, but just
  shorter to type."
  [arg]
  (-> arg
      (js->clj :keywordize-keys true)))


(def ios? (= "ios" (.-OS platform)))
