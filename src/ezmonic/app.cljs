(ns ezmonic.app
  (:require [clojure.string :as s]
            ["react-native" :as rn]
            ["react" :as react]
            ["create-react-class" :as crc]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [ezmonic.events]
            [ezmonic.subs]
            [ezmonic.style :as style]
            [ezmonic.util :as u]
            ["react-native-picker-select" :as react-native-picker-select]
            ["react-navigation" :as react-navigation]
            ["react-navigation-stack" :as react-navigation-stack]))


(def safe-area-view (r/adapt-react-class (.-SafeAreaView rn)))
(def view (r/adapt-react-class (.-View rn)))
(def text (r/adapt-react-class (.-Text rn)))
(def textinput (r/adapt-react-class (.-TextInput rn)))
(def scroll-view (r/adapt-react-class (.-ScrollView rn)))
(def picker (r/adapt-react-class (.-Picker rn)))
(def picker-item (r/adapt-react-class (.-Item (.-Picker rn))))
(def touchable-highlight (r/adapt-react-class (.-TouchableHighlight rn)))
(def picker-select (r/adapt-react-class (.-default react-native-picker-select)))
(def platform (.-Platform rn))


(def create-stack-navigator (.-createStackNavigator react-navigation-stack))
(def create-app-container (.-createAppContainer react-navigation))


(def ios? (= "ios" (.-OS platform)))


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
  (doall
   (for [mnemonics (u/number->mnemonics @number)]
     (let [random-key (random-uuid)
           chunk-number (first mnemonics)]
       ^{:key random-key}
       [view {:style {:flex-direction "row"}}
        [text "Number: "]
        [text {:style {:width 50
                       :padding-top 35
                       :font-weight "bold"
                       :font-size 14}}
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
          (picker-options (rest mnemonics))])]))))


(defn picker-select-menu
  [data]
  (doall
   (for [mnemonics (u/number->mnemonics @data)]
     ^{:key (random-uuid)}
     [view {:style {:flex-direction "row"}}
      [text {:style {:padding-right 10}} (key mnemonics)]

      [picker-select {:items (->> (val mnemonics)
                                  (mapv #(hash-map "label" % "value" %)))
                      :style {:fontSize 16,
                              :paddingHorizontal 10
                              :paddingVertical 8
                              :borderWidth 0.5
                              :borderColor "purple"
                              :borderRadius 8
                              :color "black"
                              :paddingRight 30}
                      :on-value-change #(println "picker-select" %)}]])))


(defn navigate-to
  ([props screen]
   (-> props clj->js .-navigation (.navigate screen)))
  ([props screen params]
   (-> props clj->js .-navigation (.navigate screen params))))


(defn root
  [props]
  (fn [props]
    (let [input-value (rf/subscribe [:input-value])
          submitted-number (rf/subscribe [:submitted-number])]
      [safe-area-view {}
       [scroll-view {:style {:padding-top 50}
                     :scroll-enabled false}
        [view {:style {:flex-direction "row"
                       :padding 10}}
         [textinput {:style {:margin-right 10
                             :height 50
                             :width 230
                             :borderColor "gray"
                             :borderWidth 1
                             :font-size 20}
                     :keyboardType "numeric"
                     :placeholder "Enter a number"
                     :on-change-text
                     (fn [number]
                       (rf/dispatch [:input-value number])
                       (println (u/all-mezmorizations @input-value)))
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
         (if ios?
           (picker-select-menu submitted-number)
           (display-native-pickers submitted-number)))])))


(defn screen
  ([screen]
   (r/reactify-component screen))
  ([screen navigation-options]
   (doto (r/reactify-component screen)
     (aset "navigationOptions" (clj->js navigation-options)))))


(defn settings-screen
  [params]
  [view {:style {:flex 1
                 :justify-content "center"
                 :align-tems "center"}}
   [text "Settings Screen!"]
   [text (-> params
             :navigation
             .-state
             .-params
             :foo)]])

(defn stack-navigator
  [routes options]
  (create-stack-navigator (clj->js routes) (clj->js options)))

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
                                              {:on-press
                                               (println "Take me to 'Settings'")}
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

(defn init []
  (rf/dispatch-sync [:initialize-db])
  (start))
