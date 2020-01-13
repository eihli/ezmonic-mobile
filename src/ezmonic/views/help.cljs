(ns ezmonic.views.help
  (:require [ezmonic.db :as db]
            [ezmonic.style :as style]
            ["react-native" :refer [View
                                    Text
                                    TouchableHighlight]]
            ["react-navigation" :as react-navigation]
            ["react-navigation-stack" :as react-navigation-stack]
            [re-frame.core :as rf]
            [reagent.core :as rg])
  (:require-macros [ezmonic.util :refer [defnav]]))

(defnav help
  []
  [:> View
   [:> Text "Quickref"]
   [:> Text "0 -> s\n 1 -> t"]]
  (fn [{:keys [navigation]} props]
    (clj->js
     {:title "Help"
      :headerStyle style/header})))

(def help-stack
  (let [stack (. react-navigation-stack createStackNavigator
                 #js {:help-home help})]
    (doto stack
      (goog.object/set "navigationOptions" #js {:tabBarLabel "Help"}))))
