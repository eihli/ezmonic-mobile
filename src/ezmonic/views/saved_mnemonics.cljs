(ns ezmonic.views.saved-mnemonics
  (:require [ezmonic.db :as db]
            [ezmonic.styles :as styles]
            ["react-native" :refer [View
                                    Text
                                    TouchableHighlight]]
            [re-frame.core :as rf]))

(defn saved-mnemonic [number mnemonic]
  (fn [number mnemonic]
    [:> View
     [:> View styles/flex-row
      [:> Text number " "]
      [:> TouchableHighlight [:> Text "Edit"]]]
     [:> View
      [:> Text (::db/mnemonic-story mnemonic)]]]))

(defn saved-mnemonics []
  (let [mnemonics (rf/subscribe [:saved-mnemonics])]
    (fn []
      [:> View
       (for [[number mnemonic] @mnemonics]
         ^{:key number} [saved-mnemonic number mnemonic])])))
