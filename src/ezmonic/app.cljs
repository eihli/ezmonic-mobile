(ns ezmonic.app
  (:require [clojure.string :as s]
            ["react-native" :as rn]
            ["react" :as react]
            ["create-react-class" :as crc]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [ezmonic.events]
            [ezmonic.subs]
            [ezmonic.util :as u]
            ["react-native-dropdown-menu" :as react-native-dropdown-menu]
            ["react-native-picker-select" :as react-native-picker-select]))


(def styles
  ^js (-> {:container
           {:flex 1
            :backgroundColor "#fff"
            :alignItems "center"
            :justifyContent "center"}
           :title
           {:fontWeight "bold"
            :fontSize 24
            :color "blue"}
           :button
           {:fontWeight "bold"
            :fontSize 18
            :padding 6
            :backgroundColor "blue"
            :borderRadius 10}
           :buttonText
           {:paddingLeft 12
            :paddingRight 12
            :fontWeight "bold"
            :fontSize 18
            :color "white"}
           :label
           {:fontWeight "normal"
            :fontSize 15
            :color "blue"}}
          (clj->js)
          (rn/StyleSheet.create)))

(def safe-area-view (r/adapt-react-class (.-SafeAreaView rn)))
(def view (r/adapt-react-class (.-View rn)))
(def text (r/adapt-react-class (.-Text rn)))
(def textinput (r/adapt-react-class (.-TextInput rn)))
(def scroll-view (r/adapt-react-class (.-ScrollView rn)))
(def picker (r/adapt-react-class (.-Picker rn)))
(def picker-item (r/adapt-react-class (.-Item (.-Picker rn))))
(def touchable-highlight (r/adapt-react-class (.-TouchableHighlight rn)))
(def dropdown-menu (r/adapt-react-class (.-default react-native-dropdown-menu)))
(def picker-select (r/adapt-react-class (.-default react-native-picker-select)))

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

(defn display-pickers
  "Display pickers full of mnemonics for a given `number`."
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
        [picker-item {:key (random-uuid)
                      :label "pick a value!!"
                      :value ""}]
        (picker-options (rest mnemonics))])])))


(defn drop-down-menu
  [data]
  (let [_ (println "drop-down-menu ->" data)]
    ^{:key (random-uuid)}
    [view {:style {:flex-direction "row"}}
     [text {:style {:width 50}} (key data)]
     [dropdown-menu {:data [(val data)]
                     :style {:width 100}
                     :bg-color "white"
                     :handler (fn [column row]
                                (println "----> column, row:" column row))}]]))


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
                      (println (u/all-mezmorizations @input-value)))}]
        [touchable-highlight {:on-press #(rf/dispatch [:submitted-number @input-value])
                              :style {:padding 20
                                      :background-color "green"}}
         [text "press me"]]]
       [text {:style (.-title styles)} "Input!!: " @submitted-number]
       (when-not (nil? @submitted-number)
         (doall
          (for [mnemonics (u/number->mnemonics @submitted-number)]
            ^{:key (random-uuid)}
            (drop-down-menu mnemonics))))])))


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
