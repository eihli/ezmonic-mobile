(ns ezmonic.views.mnemonics
  (:require ["react-native" :as rn]
            [reagent.core :as reagent]
            [ezmonic.style :as style]))


(defn text-input
  ;; https://github.com/reagent-project/reagent/issues/119#issuecomment-141396203
  [val]
  (let [value (reagent/atom val)]
    (fn [v]
      [:> rn/View
       [:> rn/TextInput
        {:style style/text-input
         :value @value
         :text-align-vertical "top"
         :number-of-lines 5
         :on-change-text (fn [text]
                           (print text)
                           (reset! value text)
                           (reagent/flush))}]])))

