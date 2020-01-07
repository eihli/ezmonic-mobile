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
                                           TouchableHighlight
                                           Text]]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [ezmonic.helper :refer [ios?]]
            [ezmonic.db :as db]
            [ezmonic.style :as style]
            [ezmonic.views.saved-mnemonics :as saved-mnemonics]
            [ezmonic.navigation :as navigation]
            [ezmonic.views.help :as help]
            ["react-navigation" :as react-navigation]
            ["react-navigation-stack" :as react-navigation-stack]
            ["react-navigation-tabs" :as react-navigation-tabs]))


(def PickerItem (.. rn -Picker -Item))
(def create-stack-navigator (.-createStackNavigator react-navigation-stack))
(def create-bottom-tab-navigator
  (.-createBottomTabNavigator react-navigation-tabs))
(def create-app-container (.-createAppContainer react-navigation))
(def text (r/adapt-react-class Text))


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


(defn home-navigation-options [props]
  (let [navigation (:navigation (->clj props))]
    (->js {:title "ezmonic"
           :headerStyle style/header})))

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

(def home-stack
  (. react-navigation-stack createStackNavigator
     (clj->js {:home Home})))

(def app-bottom-tab-navigator
  (create-bottom-tab-navigator
   (->js {::home home-stack
          ::saved saved-mnemonics/saved-stack
          ::help help/help-stack})))

(def app-container
  (fn []
    [(r/adapt-react-class (create-app-container app-bottom-tab-navigator))
     {:ref (fn [r] (reset! navigation/navigator-ref r))}]))

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
