(ns ezmonic.views.saved-mnemonics
  (:require [ezmonic.db :as db]
            [ezmonic.styles :as styles]
            ["react-native" :refer [View
                                    Text
                                    TouchableHighlight]]
            ["react-navigation" :as react-navigation]
            ["react-navigation-stack" :as react-navigation-stack]
            [re-frame.core :as rf]
            [reagent.core :as rg])
  (:require-macros [ezmonic.util :refer [defnav]]))

(defn -saved-mnemonic [number mnemonic]
  (this-as this
    (fn [number mnemonic]
      [:> View
       [:> View styles/flex-row
        [:> Text number " "]
        [:> TouchableHighlight
         {:on-press (fn []
                      (. (.. this -props -navigation) navigate "edit"))}
         [:> Text "Edit"]]]
       [:> View
        [:> Text (::db/mnemonic-story mnemonic)]]])))

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
         ^{:key number} [saved-mnemonic number mnemonic])]))
  (fn [{:keys [navigation]} props]
    (clj->js
     {:title "Saved mnemonics"})))

(defnav edit-mnemonic
  [mnemonic]
  [:> View
   [:> Text "Editing mnemonic: " mnemonic]]
  (fn [{:keys [navigation]} props]
    (clj->js
     {:title "Edit"})))

(def saved-stack
  (. react-navigation-stack createStackNavigator
     (clj->js {:home {:screen saved-mnemonics}
               :edit {:screen edit-mnemonic}})))
