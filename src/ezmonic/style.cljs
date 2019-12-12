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
    :justifyContent "center"}
   :inputButton
   {:padding 10
    :backgroundColor "#01BCD4"}
   :inputButtonText
   {:color "white"
    :fontSize 18
    :fontWeight "bold"}
   :mezmorize_button
   {:padding 20
    :backgroundColor "green"}
   :title
   {:fontWeight "bold"
    :fontSize 24
    :color "blue"}
   :button
   {:fontWeight "bold"
    :fontSize 18
    :padding 6
    :backgroundColor "blue"
    :borderRadius 10}
   :buttonText
   {:paddingLeft 12
    :paddingRight 12
    :fontWeight "bold"
    :fontSize 18
    :color "white"}
   :label
   {:fontWeight "normal"
    :fontSize 15
    :color "blue"}})


(def platform-style
  ((.-select platform)
   #js {:ios (m/deep-merge all-styles ios-only-styles)
        :android all-styles}))


(def styles
  (-> platform-style
      ->js
      rn/StyleSheet.create))
