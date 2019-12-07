(ns ezmonic.helper
  (:require ["react-native" :as rn
             :refer [Platform]
             :rename {Platform platform}]))


(def ios? (= "ios" (.-OS platform)))
