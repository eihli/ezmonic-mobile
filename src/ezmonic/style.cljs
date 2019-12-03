(ns ezmonic.style
  (:require ["react-native" :as rn]))


(def styles
  ^js (-> {:container
           {:flex 1
            :backgroundColor "#fff"
            :alignItems "center"
            :justifyContent "center"}
           :inputButton
           {:padding 20
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
            :color "blue"}}
          (clj->js)
          (rn/StyleSheet.create)))
