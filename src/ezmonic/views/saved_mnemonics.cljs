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
  ;; This gets passed to .withNavigation which screws
  ;; up the function args. That's why we are read-string
  ;; mnemonic from props.
  (fn [number mnemonic]
    [:> View
     [:> View style/flex-row
      [:> Text number " "]
      [:> TouchableHighlight
       [:> Text
        {:on-press
         #(rf/dispatch [:navigate [:saved-edit number]])}
        "Edit" ]]]
     [:> View
      [:> Text (string/join
                " "
                (map
                 ::db/mnemonic-chosen-word
                 (::db/mnemonic mnemonic)))]]
     [:> View
      [:> Text "" (::db/mnemonic-story mnemonic)]]]))

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
        mnemonic @(rf/subscribe [:saved-mnemonic number])
        editable-mnemonic (rg/atom mnemonic)]
    [:> rn/ScrollView {:style {:padding-top 20 :margin 10}}
     [:> View
      [:> Text number]
      [shared/native-pickers editable-mnemonic]
      [:> Text
       "Give this mnemonic a name. "
       " Write a sentence or story that uses these words."
       " Save it for reference."]      
      [text-input (rg/cursor editable-mnemonic [:db/mnemonic-story])]
      [:> rn/View
       {:style {:display "flex"
                :flex-direction "row"
                :justify-content "space-between"}}
       [:> rn/Button
        {:title "Clear"
         :style {:flex 1}}]
       [:> rn/Button
        {:title "Saves"
         :style {:flex 1}
         :on-press #(rf/dispatch
                     [:editable-mnemonic-story-submitted
                      number
                      @editable-mnemonic])}]]]]))

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

