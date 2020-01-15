(ns ezmonic.views.home
  (:require ["react-native" :as rn]
            [re-frame.core :as rf]
            [ezmonic.style :as style]
            [ezmonic.views.shared :as shared]
            [ezmonic.db :as db]
            [clojure.string :as s]
            [reagent.core :as r]
            ["react-navigation-stack" :as react-navigation-stack]))

(def PickerItem (.. rn -Picker -Item))

(defn picker
  [picker-idx mnemonic-subelement]
  (let [val (r/atom (::db/mnemonic-chosen-word mnemonic-subelement))]  
    (fn [picker-idx mnemonic-subelement]
      [:> rn/View
       [:> rn/Text {:style {:margin-left "auto"
                            :margin-right "auto"}} (::db/mnemonic-number mnemonic-subelement)]
       (into
        [:> rn/Picker {:style {:width 140}
                       :item-style {:font-size 10}
                       :selectedValue @val
                       :onValueChange (fn [v]
                                        (reset! val v)
                                        (r/flush))
                       :enabled true}]
        (map-indexed
         (fn [idx word]
           ^{:key idx} [:> PickerItem {:label word
                                       :value word}])
         (::db/mnemonic-word-choices mnemonic-subelement)))])))

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

(defn mnemonic-utils [mnemonic]
  [:> rn/View
   [:> rn/Text
    "You can mezmorize the number " (::db/number @mnemonic) " with the words"
    " " (s/join " " (map ::db/chosen-word (::db/elements @mnemonic)))
    " Use the pickers below to change the words into a phrase"
    " that you find easy to remember."]
   [shared/mnemonic-form @mnemonic]])

(defn number-input
  []
  (let [number (r/atom "")]
    (fn [{:keys [on-submit]}]
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
         :value @number
         :on-change-text (fn [text]
                           (reset! number text)
                           (r/flush))
         :on-submit-editing #(on-submit @number)}]
       [:> rn/Button
        {:title "mnemorize"
         :style {:flex 5}
         :on-press #(on-submit @number)}]])))

(defn -Home
  []
  (let [submitted-val (r/atom "")
        calculating-mnemonic? (rf/subscribe [:calculating-mnemonic?])]
    (fn []
      (let [mnemonic (r/atom @(rf/subscribe [:mnemonic]))]
        [:> rn/SafeAreaView {}
         [:> rn/ScrollView {:style {:margin 10}}
          [number-input
           {:on-submit (fn [val]
                         (reset! submitted-val val)
                         (rf/dispatch [:mnemonic-submitted-for-calculation val]))}]
          (cond
            (and (not (empty? @submitted-val)) (not @calculating-mnemonic?))
            [mnemonic-utils mnemonic]
            @calculating-mnemonic?
            [:> rn/View
             [:> rn/Text
              "Calculating mnemonic for " @submitted-val ". Please wait..."]])]]))))

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
