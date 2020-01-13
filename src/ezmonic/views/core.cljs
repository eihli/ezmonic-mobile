(ns ezmonic.views.core
  (:require [clojure.string :as s]
            [goog.object]
            [cljs-bean.core :refer [->clj ->js]]
            ["react-native" :as rn :refer [AsyncStorage
                                           Button
                                           View
                                           TextInput
                                           SafeAreaView
                                           ScrollView
                                           Picker
                                           TouchableHighlight
                                           Text]]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [ezmonic.helper :refer [ios?]]
            [ezmonic.db :as db]
            [ezmonic.style :as style]
            [ezmonic.views.saved-mnemonics :as saved-mnemonics]
            [ezmonic.navigation :as navigation]
            [ezmonic.views.help :as help]
            [ezmonic.views.home :as home]
            ["react-navigation" :as react-navigation]
            ["react-navigation-stack" :as react-navigation-stack]
            ["react-navigation-tabs" :as react-navigation-tabs]))


(def PickerItem (.. rn -Picker -Item))
(def create-stack-navigator
  (.-createStackNavigator react-navigation-stack))
(def create-bottom-tab-navigator
  (.-createBottomTabNavigator react-navigation-tabs))
(def create-app-container
  (.-createAppContainer react-navigation))

(def app-bottom-tab-navigator
  (create-bottom-tab-navigator
   #js {:home home/home-stack
        :saved saved-mnemonics/saved-stack
        :help help/help-stack}))

(def app-container
  [(r/adapt-react-class (create-app-container app-bottom-tab-navigator))
   {:ref (fn [r] (reset! navigation/navigator-ref r))}])

