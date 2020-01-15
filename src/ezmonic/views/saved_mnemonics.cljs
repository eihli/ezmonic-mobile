(ns ezmonic.views.saved-mnemonics
  (:require [clojure.string :as s]
            [ezmonic.db :as db]
            [ezmonic.style :as style]
            [ezmonic.views.mnemonics :refer [text-input]]
            [ezmonic.views.shared :refer [edit-bar] :as shared]
            [ezmonic.views.home :as home]
            ["react-native"
             :refer [View
                     Text
                     TouchableHighlight]
             :as rn]
            ["react-navigation" :as react-navigation]
            ["react-navigation-stack" :as react-navigation-stack]
            [re-frame.core :as rf]
            [reagent.core :as rg]
            [clojure.string :as string]
            [ezmonic.navigation :as navigation])
  (:require-macros [ezmonic.util :refer [defnav]]))

(defn saved-mnemonic [number mnemonic]
  (fn [number mnemonic]
    [:> View
     [:> View style/flex-row
      [:> Text "Name: " (::db/name mnemonic) " "]
      [:> TouchableHighlight
       [:> Text
        {:on-press
         #(rf/dispatch [:navigate [:saved-edit number]])}
        "Edit" ]]]
     [:> Text "Number: " (::db/number mnemonic)]
     [:> View
      [:> Text "Words: " (string/join " " (map ::db/chosen-word (::db/elements mnemonic)))]]
     [:> View
      [:> Text "Story: " (::db/story mnemonic)]]]))

(defn saved-mnemonics
  []
  (let [mnemonics (rf/subscribe [:saved-mnemonics])]
    (fn []
      [:> View
       (for [[number mnemonic] @mnemonics]
         ^{:key number}
         [:> View style/card
          [saved-mnemonic number mnemonic]])])))

(defn edit-mnemonic
  []
  (let [number @(rf/subscribe [:screen-params])
        mnemonic @(rf/subscribe [:saved-mnemonic number])]
    (print mnemonic)
    [:> rn/ScrollView {:style {:padding-top 20 :margin 10}}
     [shared/mnemonic-form
      mnemonic
      {:on-save (fn [mnemonic]
                  (rf/dispatch [:navigate [:saved-home]]))}]]))

(def saved-stack
  (let [stack (. react-navigation-stack createStackNavigator
                 (clj->js
                  {:saved-home (doto (rg/reactify-component saved-mnemonics)
                                 (goog.object/set
                                  "navigationOptions"
                                  (clj->js {:title "Saved mnemonic"
                                            :headerStyle style/header})))
                   :saved-edit (doto (rg/reactify-component edit-mnemonic)
                                 (goog.object/set
                                  "navigationOptions"
                                  (clj->js {:title "Edit"
                                            :headerStyle style/header})))}))]
    (doto stack
      (goog.object/set "navigationOptions" #js {:tabBarLabel "Saved"}))))

