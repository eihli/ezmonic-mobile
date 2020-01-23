(ns ezmonic.views.home
  (:require ["react-native" :as rn]
            [re-frame.core :as rf]
            [ezmonic.style :as style]
            [ezmonic.views.shared
             :refer [div center-quote]
             :as shared]
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

(defn number-input
  [{:keys [on-submit number]}]
  (fn [{:keys [on-submit number]}]
    [:> rn/View
     {:style {:display "flex"
              :flexDirection "row"}}
     [:> rn/TextInput
      {:style (merge
               style/text-input
               {:flex 7
                :height 40
                :margin-right 2
                :padding-left 4})
       :keyboardType "phone-pad"
       :placeholder "Enter a number"
       :placeholder-text-color "grey"
       :value @number
       :on-change-text (fn [text]
                         (reset! number (s/replace text #"\D" ""))
                         (r/flush))
       :on-submit-editing #(on-submit @number)}]
     [:> rn/Button
      {:title "mnemorize"
       :style {:flex 5}
       :on-press #(on-submit @number)}]]))

(defn -Home
  []
  (let [submitted-val (r/atom "")
        number (r/atom "")
        calculating-mnemonic? (rf/subscribe [:calculating-mnemonic?])]
    (fn []
      (let [all-possible-mnemonic (r/atom @(rf/subscribe [:all-possible-mnemonic]))]
        [:> rn/SafeAreaView {}
         [:> rn/ScrollView {:style {:margin 10}}
          [number-input
           {:number number
            :on-submit (fn [val]
                         (reset! submitted-val val)
                         (rf/dispatch [:mnemonic-submitted-for-calculation val]))}]
          (cond
            (and
             (not (empty? @all-possible-mnemonic))
             (not (empty? @submitted-val))
             (not @calculating-mnemonic?))
            [:> rn/View
             [shared/new-mnemonic-form all-possible-mnemonic
              {:on-save (fn [mnemonic]
                          (rf/dispatch [:navigate [:saved-home]]))
               :on-reset (fn []
                           (reset! number "")
                           (reset! submitted-val ""))}]]
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
