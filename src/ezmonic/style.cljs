(ns ezmonic.style
  (:require ["react-native" :as rn :refer [Platform] :rename {Platform platform}]
            [medley.core :as m :refer [deep-merge]]
            [cljs-bean.core :refer [->js]]))

(def ios-only-styles
  {:inputButton {:borderRadius 10}
   :inputButtonText {:marginTop 3}})


(def all-styles
  {:paragraph
   {:paddingTop 10}
   :container
   {:flex 1
    :backgroundColor "#fff"
    :alignItems "center"
    :justifyContent "center"}})


(def platform-style
  ((.-select platform)
   #js {:ios (m/deep-merge all-styles ios-only-styles)
        :android all-styles}))


(def styles
  (-> platform-style
      ->js
      rn/StyleSheet.create))
