(ns ezmonic.views
  (:require [clojure.string :as s]
            [goog.object]
            [cljs-bean.core :refer [->clj ->js]]
            ["react-native" :as rn :refer [AsyncStorage
                                           Button
                                           View
                                           TextInput
                                           SafeAreaView
                                           ScrollView
                                           Picker
                                           Switch
                                           TouchableHighlight
                                           Text]]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [ezmonic.helper :refer [ios?]]
            [ezmonic.style :as style]
            ["react-navigation" :as react-navigation]
            ["react-navigation-stack" :as react-navigation-stack]
            ["react-native-modal" :as react-native-modal :refer [default] :rename {default modal}]))


(def PickerItem (.. rn -Picker -Item))
(def create-stack-navigator (.-createStackNavigator react-navigation-stack))
(def create-app-container (.-createAppContainer react-navigation))


(defn picker-options
  "From given `data`, display picker options."
  [words]
  (map-indexed
   (fn [idx word]
     ^{:key idx}
     [:> PickerItem {:label word
                   :value word}])
   words))

(defn native-pickers
  "Display pickers full of mnemonics for a given `number`.

  Uses native picker, which looks fine in Android, but for this
  particular app is not the right fit."
  [mnemonic]
  [:> View {:style {:flex-direction "row"
                    :flex-wrap "wrap"
                    :justify-content "space-between"
                    :padding 10}}
   (map-indexed
    (fn [idx mnemonic-subelement]
      (let [selected-value (:mnemonic-chosen-word mnemonic-subelement)
            mnemonic-number (:mnemonic-number mnemonic-subelement)]
        ^{:key idx}
        [:> View {:style {:flex-direction "row"}}
         ^{:key "text"}
         [:> Text {:style {:padding-top 15
                           :font-weight "bold"
                           :font-size 18}}
          mnemonic-number]
         ^{:key "picker"}
         [:> Picker {:style {:width 140}
                     :item-style {:font-size 10}
                     :selectedValue selected-value
                     :onValueChange #(rf/dispatch [:select-value idx %1 %2])
                     :enabled true}
          (picker-options (:mnemonic-word-choices mnemonic-subelement))]]))
    mnemonic)])


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
               :on-swipe-complete #(do (.setItem AsyncStorage "show-welcome" "false")
                                       (rf/dispatch-sync [:show-welcome false]))
               :on-backdrop-press #(rf/dispatch [:show-welcome false])}
     [:> View {:background-color "white"
               :padding 20
               :border-radius 10}
      [:> Text {:style {:font-size 22
                        :font-weight "bold"}} "Welcome"]]]))

(defn home-navigation-options [props]
  (let [navigation (:navigation (->clj props))]
    (->js {:title "ezmonic"
           :headerStyle style/header
           :headerRight (r/as-element
                         [:> TouchableHighlight
                          {:on-press #(navigate-> navigation "settings")}
                          [:> Text {:style
                                    {:font-size 30
                                     :color "black"
                                     :padding-right 10}}
                           "☰"]])})))

(defn mnemonic-utils [submitted-number mnemonic]
  (let [editable-mnemonic-story (rf/subscribe [:editable-mnemonic-story])]
    (fn [submitted-number mnemonic]
      [:> View
       [:> Text
        "You can mezmorize the number " submitted-number " with the simple phrase: "]
       [:> View
        [:> Text
         {:style {:font-weight "bold"}}
         (s/join " " (map :mnemonic-chosen-word mnemonic))]]
       [:> View
        [:> Text
         "Use the pickers below to change the words in the phrase"
         " to something that you find easy to remember."]]
       [native-pickers mnemonic]
       [:> View
        [:> Text
         "Write a sentence or story that uses those words."
         " Save it for later reference."]
        [:> TextInput
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
        [:> View
         {:style {:display "flex"
                  :flex-direction "row"
                  :justify-content "space-between"}}
         [:> Button
          {:title "Clear"
           :style {:flex 1}}]
         [:> Button
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
    (fn [props]
      [:> SafeAreaView {}
       [:> ScrollView {:style {:padding-top 20 :margin 10}
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
           :placeholder "Enter a number"
           :on-change-text #(rf/dispatch [:number-input-changed %])
           :on-submit-editing #(rf/dispatch [:mnemonic-submitted-for-calculation @number-to-mnemorize])}]

         [:> Button
          {:title "mnemorize"
           :style {:flex 5}
           :on-press #(rf/dispatch [:mnemonic-submitted-for-calculation @number-to-mnemorize])}]]
        (cond
          (and (not (empty? @submitted-number)) (not @calculating-mnemonic?))
          [mnemonic-utils @submitted-number @mnemonic]
          @calculating-mnemonic?
          [:> View
           [:> Text
            "Calculating mnemonic for " @number-to-mnemorize ". Please wait..."]])]])))

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
       [:> TouchableHighlight
        {:on-press #(navigate-> navigation "about")}
        [:> Text {:style
                  {:font-size 17
                   :color "black"
                   :padding-right 10}}
         "About"]])})))
(def Settings
  (let [comp (r/reactify-component -Settings)]
    (doto comp
      (goog.object/set "navigationOptions" settings-navigation-options))
    comp))

(def app-navigator
  (create-stack-navigator
   (->js {:home Home
          :settings Settings})))

(def app-container
  (r/adapt-react-class (create-app-container app-navigator)))


(defn home []
  (fn []
    [:> Text "foosballss"]))

(defonce root-ref (atom nil))
(defonce root-component-ref (atom nil))

(defn render-root [component]
  (let [first-call? (nil? @root-ref)]
    (reset! root-ref component)
    (if-not first-call?
      (when-let [root @root-component-ref]
        (.forceUpdate ^js root))
      (let [Root (r/create-class
                  {:render (fn []
                             (let [body @root-ref]
                               (if (fn? body)
                                 (body)
                                 body)))
                   :component-did-mount (fn []
                                          (this-as this
                                            (reset! root-component-ref this)))
                   :component-will-unmount (fn []
                                             (reset! root-component-ref nil))})]
        (rn/AppRegistry.registerComponent "Ezmonic" (fn [] Root))))))

(defn start ^:dev/after-load []
  (render-root (r/as-element [app-container])))

(defn ^:export init []
  (start))
