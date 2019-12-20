(ns ezmonic.app
  (:require [clojure.string :as s]
            [cljs.reader :refer [read-string]]
            [goog.object]
            [cljs-bean.core :refer [bean ->clj ->js]]
            ["react-native" :as rn :refer [AsyncStorage] :rename {AsyncStorage async-storage}]
            ["react-native" :refer [Button View TextInput SafeAreaView ScrollView Picker Switch TouchableHighlight Text]]
            ["react" :as react]
            ["create-react-class" :as crc]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [ezmonic.events]
            [ezmonic.helper :refer [ios?]]
            [ezmonic.subs]
            [ezmonic.style :as style]
            [ezmonic.util :as u]
            ["react-native-picker-select" :as react-native-picker-select]
            ["react-navigation" :as react-navigation]
            ["react-navigation-stack" :as react-navigation-stack]
            ["react-native-modal" :as react-native-modal :refer [default] :rename {default modal}]))

(rf/dispatch-sync [:initialize-e-db])

(def safe-area-view (r/adapt-react-class (.-SafeAreaView rn)))
(def view (r/adapt-react-class (.-View rn)))
(def text (r/adapt-react-class (.-Text rn)))
(def textinput (r/adapt-react-class (.-TextInput rn)))
(def scroll-view (r/adapt-react-class (.-ScrollView rn)))
(def picker (r/adapt-react-class (.-Picker rn)))
(def picker-item (r/adapt-react-class (.-Item (.-Picker rn))))
(def touchable-highlight (r/adapt-react-class (.-TouchableHighlight rn)))
(def picker-select (r/adapt-react-class (.-default react-native-picker-select)))
(def rnswitch (r/adapt-react-class (.-Switch rn)))

(def create-stack-navigator (.-createStackNavigator react-navigation-stack))
(def create-app-container (.-createAppContainer react-navigation))


(defn picker-options
  "From given `data`, display picker options."
  [words]
  (map-indexed
   (fn [idx word]
     ^{:key idx}
     [picker-item {:label word
                   :value word}])
   words))

(defn display-native-pickers
  "Display pickers full of mnemonics for a given `number`.

  Uses native picker, which looks fine in Android, but for this
  particular app is not the right fit."
  [number]
  [view {:style {:flex-direction "row"
                 :justify-content "flex-start"
                 :flex-wrap "wrap"
                 :padding 10}}
   (doall
    (map-indexed 
     (fn [idx mnemonic-subelement]
       (let [selected-value (:mnemonic-chosen-word mnemonic-subelement)
             mnemonic-number (:mnemonic-number mnemonic-subelement)]
         ^{:key idx}
         [view {:style {:flex-direction "row"}}
          ^{:key "text"}
          [text {:style {:padding-top 15
                         :font-weight "bold"
                         :font-size 18}}
           mnemonic-number]
          ^{:key "picker"}
          [picker {:style {:width 150}
                   :item-style {:font-size 10}
                   :selectedValue selected-value
                   :onValueChange #(rf/dispatch [:select-value idx %1 %2])
                   :enabled true}
           (picker-options (:mnemonic-word-choices mnemonic-subelement))]])
       )
     @(rf/subscribe [:mnemonic])))])


(defn picker-select-menu
  [data]
  [view {:style {:flex-direction "row"
                 :justify-content "flex-start"
                 :flex-wrap "wrap"
                 :padding 10}}
   (doall
    (for [mnemonics (u/number->mnemonics @data)]
      ^{:key (random-uuid)}
      [view {:style {:flex-direction "row"
                     :flex-wrap "wrap"
                     :align-items "flex-start"
                     :width 150}}
       [view {:style {:width 50}}
        [text {:style {:font-size 20}}
         (key mnemonics)]]
       [picker-select {:items (->> (val mnemonics)
                                   (mapv #(hash-map "label" % "value" %)))
                       :placeholder {:label "Pick a word" #_"▼"}
                       :style {:font-size 18
                               :padding-horizontal 10
                               :padding-vertical 8
                               :border-width 0.5
                               :border-color "purple"
                               :border-radius 8
                               :color "black"
                               :padding-right 30
                               :padding-left 20}
                       :on-value-change #(println "picker-select" %)}]]))])


(defn navigate->
  "Navigate to a given `screen`."
  [navigation screen]
  ((:navigate navigation) screen))


(defn welcome-modal
  ""
  []
  (let [paragraph {:style (.-paragraph style/styles)}]
    [:> modal {:is-visible @(rf/subscribe [:show-welcome])
               :hideModalContentWhileAnimating true
               :animation-out "slideOutDown"
               :swipe-direction ["right" "left" "up" "down"]
               :on-swipe-complete #(do (.setItem async-storage "show-welcome" "false")
                                       (rf/dispatch-sync [:show-welcome false]))
               :on-backdrop-press #(rf/dispatch [:show-welcome false])}
     [view {:background-color "white"
            :padding 20
            :border-radius 10}
      [text {:style {:font-size 22
                     :font-weight "bold"}} "Welcome"]]]))

(defn home-navigation-options [props]
  (let [navigation (:navigation (->clj props))]
    (->js {:title "ezmonic"
           :headerStyle style/header
           :headerRight (r/as-element
                         [touchable-highlight
                          {:on-press #(navigate-> navigation "settings")}
                          [text {:style
                                 {:font-size 30
                                  :color "black"
                                  :padding-right 10}}
                           "☰"]])})))

(defn -Home [props]
  (let [navigation (:navigation props)
        input-value (rf/subscribe [:input-value])
        submitted-number (rf/subscribe [:submitted-number])
        mnemonic (rf/subscribe [:mnemonic])]
    (fn [props]
      [safe-area-view {}
       [scroll-view {:style {:padding-top 50 :margin 10}
                     :scroll-enabled false}
        [welcome-modal]
        [:> View
         {:style {:display "flex"
                  :flexDirection "row"}}
         [:> TextInput
          {:style {:flex 7
                   :height 40
                   :borderWidth 1
                   :borderColor "grey"
                   :marginRight 5}
           :keyboardType "phone-pad"
           :placeholder " Enter a number"
           :on-change-text #(rf/dispatch [:number-input-changed %])
           :on-submit-editing #(rf/dispatch [:calculate-mnemonic])}]

         [:> Button
          {:title "mezmorize!"
           :style {:flex 5}
           :on-press #(rf/dispatch [:calculate-mnemonic])}]]
        (when-not (empty? @mnemonic)
          [:> View
           [:> Text
            "You can mezmorize the number " @submitted-number " with the simple phrase: "]
           [:> View
            [:> Text (s/join " " (map :mnemonic-chosen-word @mnemonic))]]
           [:> View
            [:> Text
             "Use the pickers below to change the words in the phrase"
             " to something that you find easy to remember."]]
           (display-native-pickers @input-value)])]])))

(def Home
  (let [comp (r/reactify-component -Home)]
    (doto comp
      (goog.object/set "navigationOptions" home-navigation-options))
    comp))



(defn -Settings [props]
  (let [state (rf/subscribe [:switch])
        navigation (:navigation props)]
    [:> View {:style {:justify-content "flex-start"
                      :flex-wrap "wrap"
                      :padding 10}}
     [:> Switch {:onValueChange #(rf/dispatch [:switch %])
                 :value @state}]
     [:> Text "Crazy toggle, that does nothing useful... just yet!"]
     [:> TouchableHighlight
      {:on-press #(rf/dispatch [:show-welcome true])}
      [:> Text
       {:style {:padding-top 20
                :font-weight "bold"}}
       "Show About intro"]]]))

(defn settings-navigation-options [props]
  (let [{navigation :navigation} props]
    (->js
     {:title "Settings"
      :headerStyle style/header
      :headerTintColor "black"
      :headerRight
      (r/as-element
       [touchable-highlight
        {:on-press #(navigate-> navigation "about")}
        [text {:style
               {:font-size 17
                :color "black"
                :padding-right 10}}
         "About"]])})))
(def Settings
  (let [comp (r/reactify-component -Settings)]
    (doto  -Settings
      (goog.object/set "navigationOptions" settings-navigation-options))
    comp))

(def app-navigator
  (create-stack-navigator
   (->js {:home Home
          :settings Settings})))

(def app-container
  (create-app-container app-navigator))

(defn render []
  (rn/AppRegistry.registerComponent "Ezmonic" (fn [] app-container)))

(defn ^:dev/after-load clear-cache-and-render! []
  (rf/clear-subscription-cache!)
  (render))

(defn ^:export main []
  (render))

