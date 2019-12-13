(ns ezmonic.app
  (:require [clojure.string :as s]
            [cljs.reader :refer [read-string]]
            [cljs-bean.core :refer [bean ->clj ->js]]
            ["react-native" :as rn :refer [AsyncStorage] :rename {AsyncStorage async-storage}]
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
  [data]
  (doall
   (for [words data]
     (doall
      (for [word words]
        ^{:key (random-uuid)}
        (doall
         ^{:key (random-uuid)}
         [picker-item {:label word
                       :value word}]))))))

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
    (for [mnemonics (u/number->mnemonics @number)]
      (let [random-key (random-uuid)
            chunk-number (first mnemonics)]
        ^{:key random-key}
        [view {:style {:flex-direction "row"}}
         [text {:style {:padding-top 15
                        :font-weight "bold"
                        :font-size 18}}
          chunk-number]
         ^{:key (random-uuid)}
         (doall
          [picker {:style {:width 150}
                   :item-style {:font-size 10}
                   :key (random-uuid)
                   :selectedValue (-> (rf/subscribe [:picker-data])
                                      deref
                                      #_(get (str chunk-number "-" random-key))
                                      (get chunk-number))
                   :onValueChange #(do (println "the new value is:" %)
                                       #_(rf/dispatch [:picker-data (hash-map (str chunk-number "-" random-key) %)])
                                       (rf/dispatch [:picker-data (hash-map chunk-number %)]))
                   :enabled true}
           (picker-options (rest mnemonics))])])))])


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
  [screen]
  ((.-navigate @(rf/subscribe [:navigation])) screen))


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
       "- S, Z and soft C translate to \"0\"
- T and D as in Tea or Add translate to \"1\"
- N as in Knee translates to \"2\"
- M as in Aim translates to \"3\"
- R as in Ray translates to \"4\"
- L as in Low translates to \"5\"
- J and soft G as in Joy or Gyro translates to \"6\"
- K and hard C as in Key and Cay translates to \"7\"
- etc..."]]]))

(defn root
  [props]
  (fn [{:keys [navigation] :as props}]
    (let [_ (rf/dispatch [:navigation navigation])
          input-value (rf/subscribe [:input-value])
          submitted-number (rf/subscribe [:submitted-number])]
      [safe-area-view {}
       [scroll-view {:style {:padding-top 50}
                     :scroll-enabled false}
        [welcome-modal]
        [view {:style {:flex-direction "row"
                       :padding 10}}
         [textinput {:style {:margin-right 10
                             :height 50
                             :width 230
                             :borderColor "gray"
                             :borderWidth 1
                             :font-size 20}
                     :keyboardType "phone-pad"
                     :placeholder " Enter a number"
                     :on-change-text #(rf/dispatch [:input-value %])
                     :on-submit-editing #(rf/dispatch [:submitted-number @input-value])}]

         [touchable-highlight {:on-press #(rf/dispatch [:submitted-number @input-value])
                               :style (.-inputButton style/styles)}
          [text
           {:style (.-inputButtonText style/styles)}
           "mezmorize!"]]]]
       #_[touchable-highlight
          {:style {:border-radius 25
                   :padding 150}
           :on-press #(navigate-to props "settings" {:foo "BAR!"})}
          [text {:style {:font-size 40
                         :font-weight "100"
                         :color "white"
                         :background-color "red"
                         :padding 20
                         :border-radius 50}}
           "Yey"]]
       (when-not (nil? @submitted-number)
         [view
          [text {:style {:padding 10
                         :font-size 20}}
           "You can memorize the number "
           @submitted-number
           " with the simple pharse:"]
          (if ios?
            (picker-select-menu submitted-number)
            (display-native-pickers submitted-number))])])))


(defn screen
  ([screen]
   (r/reactify-component screen))
  ([screen navigation-options]
   (doto (r/reactify-component screen)
     (aset "navigationOptions" (->js navigation-options)))))


(defn settings-screen
  [params]
  (let [state (rf/subscribe [:switch])]
    [view {:style {:justify-content "flex-start"
                   :flex-wrap "wrap"
                   :padding 10}}
     [rnswitch {:onValueChange #(rf/dispatch [:switch %])
                :value @state}]
     [text "Crazy toggle, that does nothing useful... just yet!"]
     [touchable-highlight {:on-press #(rf/dispatch [:show-welcome true])}
      [text {:style {:padding-top 20
                     :font-weight "bold"}}
       "Show About intro"]]]))


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
      (stack-navigator {:home (screen root {:title "ezmonic"
                                            :headerStyle header-style
                                            :headerRight
                                            (r/as-element
                                             [touchable-highlight
                                              {:on-press #(navigate-> "settings")}
                                              [text {:style
                                                     {:font-size 30
                                                      :color "black"
                                                      :padding-right 10}}
                                               "☰"]])})
                        :settings (screen settings-screen {:title "Settings"
                                                           :headerStyle header-style
                                                           :headerTintColor "black"})}
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
      (.then #(rf/dispatch-sync [arg (read-string %)]))))


(defn init []
  (rf/dispatch-sync [:initialize-db])
  (determine-state :show-welcome)
  (start))
