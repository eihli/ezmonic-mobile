(ns ezmonic.views.mnemonics
  (:require ["react-native" :as rn]
            [reagent.core :as reagent]
            [ezmonic.style :as style]))


(defn text-input
  ;; https://github.com/reagent-project/reagent/issues/119#issuecomment-141396203
  [val]
  [:> rn/View
   [:> rn/TextInput
    {:style style/text-input
     :value @val
     :text-align-vertical "top"
     :multiline true
     :number-of-lines 5
     :on-change-text (fn [text]
                       (reset! val text)
                       (reagent/flush))}]])

