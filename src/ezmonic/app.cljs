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
                     :font-weight "bold"}} "Welcome"]
      [text paragraph
       "Here's a quick overview for the first-time users."]
      [text paragraph
       "Type in a number you want to remember and click \"mezmorize\"."]
      [text paragraph
       "We'll generate an easy to memorize phrase that can be converted back to the number."]
      [text paragraph
       "Every consonant sound in the phrase translates to a particular number. Vowels are ignored."]
      [text paragraph
       "- S, Z and soft C translate to \"0\" - T and D as in Tea or
Add translate to \"1\" - N as in Knee translates to \"2\" - M as in
Aim translates to \"3\" - R as in Ray translates to \"4\" - L as in
Low translates to \"5\" - J and soft G as in Joy or Gyro translates to
\"6\" - K and hard C as in Key and Cay translates to \"7\" -
etc..."]]]))

(defn root
  [props]
  (let [navigation (:navigation props)
        input-value (rf/subscribe [:input-value])
        submitted-number (rf/subscribe [:submitted-number])
        mnemonic (rf/subscribe [:mnemonic])]
    (fn []
      [safe-area-view {}
       [scroll-view {:style {:padding-top 50}
                     :scroll-enabled false}
        [welcome-modal]
        [:> View
         {:style {:display "flex"
                  :flexDirection "row"
                  :padding 10}}
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
           :on-press #(rf/dispatch [:calculate-mnemonic])}]]]
       (when-not (empty? @mnemonic)
         [:> View
          [text {:style {:padding 10
                         :font-size 20}}
           "You can mezmorize the number "
           @submitted-number
           " with the simple phrase:"]
          (if ios?
            (picker-select-menu @input-value)
            (display-native-pickers @input-value))])])))


(defn screen
  ([screen]
   (r/reactify-component screen))
  ([screen navigation-options]
   (doto (r/reactify-component screen)
     (goog.object/set "navigationOptions" navigation-options))))


(defn settings-screen
  [props]
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


(defn about-screen
  ""
  [props]
  (let [paragraph {:style (.-paragraph style/styles)}
        navigation (:navigation props)]
    [view {:style {:flex-direction "row"
                   :padding 10}}
     [view
      [text {:style {:padding-bottom 10
                     :font-size 20
                     :font-weight "bold"}}
       "How to use ezmonic"]
      [text paragraph
       "Our brains are better at remembering relationships between concrete things than abstract things like numbers."]
      [text paragraph
       "Stories and songs were passed down orally for centuries. Epics as long as Homer's Illiad and Odyssey were memorized by their original storytellers and memory champions around the world use these techniques to break world records every year."]
      [text paragraph
       "By using a simple technique to convert numbers to nouns that can be stringed together to form a story, you'll be amazed at how quickly you can memorize large numbers."]
      [text paragraph
       "All you need to do is learn how to convert phonetic sounds to numbers."]
      [text paragraph
       "Here is a quick example."]
      [text paragraph
       "Every time you hear the \"T\" or \"D\" sound, such as in the word Tea, or Aid, convert that sound to an \"1\". Every time you hear the \"M\" sound, as in May, or Mow, convert that sound to a \"3\"."]
      [text paragraph
       "Now see if you can use that technique to tell me what number is represented by the following pharse."]
      [text paragraph
       "\"Timmy met Tom today\" (13311311)."]]]))


(defn stack-navigator
  [routes options]
  (create-stack-navigator (->js routes) (->js options)))

(defn app-root
  []
  (let [header-style {:backgroundColor "#01BCD4"
                      :borderBottomColor "#ffffff"
                      :borderBottomWidth 3}]
    [:>
     (create-app-container
      (stack-navigator {:home
                        (screen root (fn [obj]
                                       (let [navigation (:navigation (->clj obj))]
                                         (->js {:title "ezmonic"
                                                :headerStyle header-style
                                                :headerRight
                                                (r/as-element
                                                 [touchable-highlight
                                                  {:on-press #(navigate-> navigation "settings")}
                                                  [text {:style
                                                         {:font-size 30
                                                          :color "black"
                                                          :padding-right 10}}
                                                   "☰"]])}))))
                        :settings
                        (screen settings-screen (fn [obj]
                                                  (let [navigation (:navigation (->clj obj))]
                                                    (->js
                                                     {:title "Settings"
                                                      :headerStyle header-style
                                                      :headerTintColor "black"
                                                      :headerRight
                                                      (r/as-element
                                                       [touchable-highlight
                                                        {:on-press #(navigate-> navigation "about")}
                                                        [text {:style
                                                               {:font-size 17
                                                                :color "black"
                                                                :padding-right 10}}
                                                         "About"]])}))))
                        :about
                        (screen about-screen (fn [obj]
                                               (let [navigation (:navigation (->clj obj))]
                                                 (->js
                                                  {:title "About"
                                                   :headerStyle header-style
                                                   :headerTintColor "black"}))))}
                       {:initialRouteName "home"}))]))

(defonce root-ref (atom nil))
(defonce root-component-ref (atom nil))

(defn render-root [root]
  (let [first-call? (nil? @root-ref)]
    (reset! root-ref root)

    (if-not first-call?
      (when-let [root @root-component-ref]
        (.forceUpdate ^js root))
      (let [Root (crc
                  #js {:componentDidMount
                       (fn []
                         (this-as this
                           (reset! root-component-ref this)))
                       :componentWillUnmount
                       (fn []
                         (reset! root-component-ref nil))
                       :render
                       (fn []
                         (let [body @root-ref]
                           (if (fn? body)
                             (body)
                             body)))})]
        (rn/AppRegistry.registerComponent "Ezmonic" (fn [] Root))))))


(defn start
  {:dev/after-load true}
  []
  (render-root (r/as-element [app-root])))


(defn determine-state
  "Determine if the `arg` should be shown to the end user."
  [arg]
   (-> (.getItem async-storage (name arg))
      (.then #(let [stored-value (read-string %)]
                (if (nil? stored-value)
                  (rf/dispatch-sync [arg true])
                  (rf/dispatch-sync [arg false]))))))

(defn init []
  (rf/dispatch-sync [:initialize-e-db])
  (determine-state :show-welcome)
  (start))
