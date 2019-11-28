(ns ezmonic.app
  (:require ["react-native" :as rn]
            ["react" :as react]
            ["create-react-class" :as crc]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [ezmonic.events]
            [ezmonic.subs]
            [ezmonic.util :as u]))


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

(def text (r/adapt-react-class (.-Text rn)))
(def textinput (r/adapt-react-class (.-TextInput rn)))
(def scroll-view (r/adapt-react-class (.-ScrollView rn)))
(def picker (r/adapt-react-class (.-Picker rn)))
(def picker-item (r/adapt-react-class (.-Item (.-Picker rn))))


(defn root []
  (let [input-value (rf/subscribe [:input-value])]
    (fn []
      [:> rn/View {:style (.-container styles)}
       [textinput {:style {:height 40
                           :width 150
                           :borderColor "gray"
                           :borderWidth 1}
                   :keyboardType "numeric"
                   :placeholder "Enter a number"
                   :on-change-text (fn [number]
                                     (rf/dispatch [:input-value number]))}]
       [textinput {:style (.-title styles)} "Input: " @input-value]])))


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
