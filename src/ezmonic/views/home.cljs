(ns ezmonic.views.home
  (:require ["react-native" :as rn]
            [re-frame.core :as rf]
            [ezmonic.style :as style]
            [ezmonic.db :as db]
            [clojure.string :as s]
            [reagent.core :as r]
            ["react-navigation-stack" :as react-navigation-stack]))

(def PickerItem (.. rn -Picker -Item))

(defn picker
  [picker-idx mnemonic-subelement]
  (let [selected-value (::db/mnemonic-chosen-word mnemonic-subelement)]  
    (into
     [:> rn/Picker {:style {:width 140}
                    :item-style {:font-size 10}
                    :selectedValue selected-value
                    :onValueChange #(rf/dispatch
                                     [:select-value picker-idx %1 %2])
                    :enabled true}]
     (map-indexed
      (fn [idx word] ^{:key idx}
        [:> PickerItem {:label word
                        :value word}])
      (::db/mnemonic-word-choices mnemonic-subelement)))))

(defn native-pickers
  "Display pickers full of mnemonics for a given `number`.

  Uses native picker, which looks fine in Android, but for this
  particular app is not the right fit."
  [mnemonic]
  (into [:> rn/View {:style {:flex-direction "row"
                             :flex-wrap "wrap"
                             :justify-content "space-between"
                             :padding 10}}]
        (map-indexed
         (fn [idx mnemonic-subelement]
           (let [mnemonic-number (:db/mnemonic-number mnemonic-subelement)]
             ^{:key idx}
             [:> rn/View {:style {:flex-direction "row"}}
              ^{:key "text"}
              [:> rn/Text {:style {:padding-top 15
                                   :font-weight "bold"
                                   :font-size 18}}
               mnemonic-number]
              ^{:key "picker"}
              [picker idx mnemonic-subelement]]))
         mnemonic)))

(defn mnemonic-utils [submitted-number mnemonic]
  (let [editable-mnemonic-story (rf/subscribe [:editable-mnemonic-story])]
    (fn [submitted-number mnemonic]
      [:> rn/View
       [:> rn/Text
        "You can mezmorize the number " submitted-number " with the simple phrase: "]
       [:> rn/View
        [:> rn/Text
         {:style {:font-weight "bold"}}
         (s/join " " (map ::db/mnemonic-chosen-word mnemonic))]]
       [:> rn/View
        [:> rn/Text
         "Use the pickers below to change the words in the phrase"
         " to something that you find easy to remember."]]
       [native-pickers mnemonic]
       [:> rn/View
        [:> rn/Text
         "Write a sentence or story that uses those words."
         " Save it for later reference."]
        [:> rn/TextInput
         {:style {:borderColor "grey"
                  :borderWidth 1}
          :multiline true
          :text-align-vertical "top"
          :number-of-lines 3
          :on-change-text #(rf/dispatch [:editable-mnemonic-story-changed %])
          :on-submit-editing #(rf/dispatch
                               [:editable-mnemonic-story-submitted
                                submitted-number
                                mnemonic
                                @editable-mnemonic-story])}]
        [:> rn/View
         {:style {:display "flex"
                  :flex-direction "row"
                  :justify-content "space-between"}}
         [:> rn/Button
          {:title "Clear"
           :style {:flex 1}}]
         [:> rn/Button
          {:title "Save"
           :style {:flex 1}
           :on-press #(rf/dispatch
                       [:editable-mnemonic-story-submitted
                        submitted-number
                        mnemonic
                        @editable-mnemonic-story])}]]]])))

(defn -Home [props]
  (let [navigation (:navigation props)
        input-value (rf/subscribe [:input-value])
        submitted-number (rf/subscribe [:submitted-number])
        calculating-mnemonic? (rf/subscribe [:calculating-mnemonic?])
        number-to-mnemorize (rf/subscribe [:number-to-mnemorize])
        mnemonic (rf/subscribe [:mnemonic])]
    (fn []
      [:> rn/SafeAreaView {}
       [:> rn/ScrollView {:style {:padding-top 20 :margin 10}
                          :scroll-enabled false}
        [:> rn/View
         {:style {:display "flex"
                  :flexDirection "row"}}
         [:> rn/TextInput
          {:style (merge
                   style/text-input
                   {:flex 7
                    :height 40})
           :keyboardType "phone-pad"
           :placeholder "Enter a number"
           :on-change-text #(rf/dispatch [:number-input-changed %])
           :on-submit-editing #(rf/dispatch [:mnemonic-submitted-for-calculation @number-to-mnemorize])}]

         [:> rn/Button
          {:title "mnemorize"
           :style {:flex 5}
           :on-press #(rf/dispatch [:mnemonic-submitted-for-calculation @number-to-mnemorize])}]]
        (cond
          (and (not (empty? @submitted-number)) (not @calculating-mnemonic?))
          [mnemonic-utils @submitted-number @mnemonic]
          @calculating-mnemonic?
          [:> rn/View
           [:> rn/Text
            "Calculating mnemonic for " @number-to-mnemorize ". Please wait..."]])]])))

(defn home-navigation-options [props]
  (clj->js {:title "ezmonic"
            :headerStyle style/header}))

(def Home
  (let [comp (r/reactify-component -Home)]
    (doto comp
      (goog.object/set "navigationOptions" home-navigation-options))
    comp))

(def home-stack
  (let [stack (. react-navigation-stack createStackNavigator
                 #js {:home-home Home})]
    (doto stack
      (goog.object/set "navigationOptions" #js {:tabBarLabel "Home"}))))
