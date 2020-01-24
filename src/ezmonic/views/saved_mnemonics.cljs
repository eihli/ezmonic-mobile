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
            [ezmonic.navigation :as navigation]
            [ezmonic.util :as util])
  (:require-macros [ezmonic.util :refer [defnav]]))

(defn saved-mnemonic [number mnemonic]
  (fn [number mnemonic]
    [:> View
     [:> Text "Name: " (::db/name mnemonic) " "]
     [:> Text "Number: " (::db/number mnemonic)]
     [:> View
      [:> Text "Words: " (string/join " " (map ::db/chosen-word (::db/elements mnemonic)))]]
     (if (not (empty? (::db/story mnemonic)))
       [:> View
        [:> Text "Story: " (::db/story mnemonic)]])
     ;; Button displayed last so it will be drawn over other elements
     ;; without the use of zIndex.
     ;; This would probably be better as a two-column layout and force stuff
     ;; to wrap.
     [:> View
      {:position "absolute"
       :top 0
       :right 0}
      [:> rn/Button 
       {:on-press
        #(rf/dispatch [:navigate [:saved-edit number]])
        :title "Edit"} ]]]))

(defn max-saved-notification [num-saved]
  (if (>= num-saved (util/max-saved-mnemonics))
    [:> rn/View
     [:> rn/Text {:style {:color "red"}}
      "You've reached the maximum number of saved mnemonics for the free version of the app. To save unlimited mnemonics, please purchase the paid version of Ezmonic from the app store."]]))

(defn saved-mnemonics
  []
  (let [mnemonics (rf/subscribe [:saved-mnemonics])]
    (fn []
      [shared/safe-scroll-wrapper
       [max-saved-notification (count @mnemonics)]
       (for [[number mnemonic] @mnemonics]
         ^{:key number}
         [:> View
          {:style style/card}
          [saved-mnemonic number mnemonic]])])))

(defn edit-mnemonic
  []
  (let [number @(rf/subscribe [:screen-params])
        mnemonic (rg/atom @(rf/subscribe [:saved-mnemonic number]))]
    [shared/safe-scroll-wrapper
     [shared/mnemonic-form
      mnemonic
      nil
      {:on-save (fn [mnemonic]
                  (rf/dispatch [:navigate [:saved-home]]))
       :on-delete (fn [mnemonic]
                    (rf/dispatch [:delete-mnemonic (::db/uuid mnemonic)]))}]]))

(def saved-stack
  (let [stack (. react-navigation-stack createStackNavigator
                 (clj->js
                  {:saved-home (doto (rg/reactify-component saved-mnemonics)
                                 (goog.object/set
                                  "navigationOptions"
                                  (clj->js {:title "saved mnemonics"
                                            :headerStyle style/header})))
                   :saved-edit (doto (rg/reactify-component edit-mnemonic)
                                 (goog.object/set
                                  "navigationOptions"
                                  (clj->js {:title "edit"
                                            :headerStyle style/header})))}))]
    (doto stack
      (goog.object/set "navigationOptions" #js {:tabBarLabel "Saved"}))))

