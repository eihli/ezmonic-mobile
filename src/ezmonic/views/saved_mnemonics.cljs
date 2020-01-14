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

(defn saved-mnemonics
  []
  (let [mnemonics (rf/subscribe [:saved-mnemonics])]
    (fn []
      [:> View
       (for [[number mnemonic] @mnemonics]
         ^{:key number}
         [:> View style/card
          [saved-mnemonic number mnemonic]])])))

(defn mnemonic-input
  [mnemonic-story]
  (let [val (rg/atom mnemonic-story)]
    [:> rn/TextInput
     {:value @val}]))


(defn edit-mnemonic
  []
  (let [mnemonic-to-edit (rf/subscribe [:mnemonic-to-edit])
        foo (rg/atom "foo")]
    [:> rn/ScrollView {:style {:padding-top 20 :margin 10}}
     [:> View
      [:> Text "Bar"]
      [views-mnemonics/my-text-input @foo]]]))

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


