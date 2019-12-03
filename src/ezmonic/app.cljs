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
            ["react-native-picker-select" :as react-native-picker-select]))


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
  Update the choices picked in `ratom`.

  Uses native picker, which looks fine in Android, but for this
  particular app is not the right fit."
  [ratom number]
  (doall
   (for [mnemonics (u/number->mnemonics @number)]
     ^{:key (random-uuid)}
     [view {:style {:flex-direction "row"}}
      [text "Number: "]
      [text {:style {:width 50
                     :padding-top 35
                     :font-weight "bold"
                     :font-size 14}}
       (first mnemonics)]
      ^{:key (random-uuid)}
      (doall
       [picker {:style {:width 150}
                :item-style {:font-size 10}
                :key (random-uuid)
                :selectedValue (get @ratom (first mnemonics))
                :onValueChange #(do (println "the new value is:" %)
                                    (reset! ratom (assoc @ratom (first mnemonics) %)))
                :enabled true}
        (picker-options (rest mnemonics))])])))


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


(defn root []
  (let [input-value (rf/subscribe [:input-value])
        submitted-number (rf/subscribe [:submitted-number])
        ratom (r/atom nil)]
    (fn []
      [safe-area-view {}
       [scroll-view {:style {:padding-top 50}
                     :scroll-enabled false}
        [textinput {:style {:padding-top 10
                            :height 40
                            :width 150
                            :borderColor "gray"
                            :borderWidth 1}
                    :keyboardType "numeric"
                    :placeholder "Enter a number"
                    :on-change-text
                    (fn [number]
                      (rf/dispatch [:input-value number])
                      (println (u/all-mezmorizations @input-value)))
                    :on-submit-editing #(rf/dispatch [:submitted-number @input-value])}]
        [touchable-highlight {:on-press #(rf/dispatch [:submitted-number @input-value])
                              :style (.-inputButton style/styles)}
         [text {:style (.-inputButtonText style/styles)} "mezmorize!"]]]
       [text {:style (.-title style/styles)} "Input!!: " @submitted-number]
       (when-not (nil? @submitted-number)
         (if ios?
           (picker-select-menu submitted-number)
           (display-native-pickers ratom submitted-number)))])))


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
  (render-root (r/as-element [root])))

(defn init []
  (rf/dispatch-sync [:initialize-db])
  (start))
