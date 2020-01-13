(ns ezmonic.views.saved-mnemonics
  (:require [ezmonic.db :as db]
            [ezmonic.style :as style]
            [ezmonic.views.shared :refer [edit-bar]]
            ["react-native" :refer [View
                                    Text
                                    TouchableHighlight]]
            ["react-navigation" :as react-navigation]
            ["react-navigation-stack" :as react-navigation-stack]
            [re-frame.core :as rf]
            [reagent.core :as rg]
            [clojure.string :as string]
            [ezmonic.navigation :as navigation])
  (:require-macros [ezmonic.util :refer [defnav]]))

(defn -saved-mnemonic [props]
  ;; This gets passed to .withNavigation which screws
  ;; up the function args. That's why we are read-string
  ;; mnemonic from props.
  (this-as this
    (fn [props]
      (let [[number mnemonic-str] (js->clj (:children props))
            mnemonic (cljs.reader/read-string mnemonic-str)]
        [:> View
         [:> View style/flex-row
          [:> Text number " "]
          [:> TouchableHighlight
           [:> Text
            {:on-press
             #(navigation/navigate-to :saved-edit)}
            "Edit" ]]]
         [:> View
          [:> Text (string/join " " (map
                                     :mnemonic-chosen-word
                                     (::db/mnemonic mnemonic)))]]
         [:> View
          [:> Text "" (::db/mnemonic-story mnemonic)]]]))))

(def saved-mnemonic
  (rg/adapt-react-class
   (.withNavigation
    react-navigation
    (rg/reactify-component -saved-mnemonic))))

(defnav saved-mnemonics
  []
  (let [mnemonics (rf/subscribe [:saved-mnemonics])]
    (fn []
      [:> View
       (for [[number mnemonic] @mnemonics]
         ^{:key number}
         [:> View style/card
          [saved-mnemonic number mnemonic]])]))
  (fn [{:keys [navigation]} props]
    (clj->js
     {:title "Saved mnemonics"
      :headerStyle style/header})))

(defnav edit-mnemonic
  [mnemonic]
  [:> View
   [:> Text mnemonic]]
  (fn [{:keys [navigation]} props]
    (clj->js
     {:title "Edit mnemonic"
      :headerStyle style/header})))

(def saved-stack
  (let [stack (. react-navigation-stack createStackNavigator
                 #js {:saved-home saved-mnemonics
                      :saved-edit edit-mnemonic})]
    (doto stack
      (goog.object/set "navigationOptions" #js {:tabBarLabel "Saved"}))))
