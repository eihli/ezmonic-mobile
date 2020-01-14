(ns ezmonic.views.saved-mnemonics
  (:require [clojure.string :as s]
            [ezmonic.db :as db]
            [ezmonic.style :as style]
            [ezmonic.views.shared :refer [edit-bar]]
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
             #(rf/dispatch [:navigate [:saved-edit mnemonic]])}
            "Edit" ]]]
         [:> View
          [:> Text (string/join
                    " "
                    (map
                     ::db/mnemonic-chosen-word
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

(defn mnemonic-input
  [mnemonic-story]
  (let [val (rg/atom mnemonic-story)]
    [:> rn/TextInput
     {:value @val}]))

(defnav edit-mnemonic
  []
  (let [mnemonic-to-edit (rf/subscribe [:mnemonic-to-edit])]
    [mnemonic-input (::db/mnemonic @mnemonic-to-edit)])
  (fn [] 
    (clj->js
     {:title "Edit mnemonic"
      :headerStyle style/header})))

(def saved-stack
  (let [stack (. react-navigation-stack createStackNavigator
                 #js {:saved-home saved-mnemonics
                      :saved-edit edit-mnemonic})]
    (doto stack
      (goog.object/set "navigationOptions" #js {:tabBarLabel "Saved"}))))
