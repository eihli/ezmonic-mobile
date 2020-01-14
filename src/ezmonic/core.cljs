(ns ezmonic.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            ["react-native" :as rn]
            [ezmonic.views.core :as views]
            [ezmonic.subs]
            [ezmonic.events]))

(rf/dispatch-sync [:initialize-db])

(defn make-reloader
  [component]
  (let [component-ref (r/atom component)
        wrapper (r/create-class 
                 {:render (fn []
                            (let [component @component-ref]
                              (if (fn? component)
                                (component)
                                component)))})]
    (rn/AppRegistry.registerComponent "Ezmonic" (fn [] wrapper))
    (fn [comp]
      (reset! component-ref comp))))

(defonce reload (make-reloader views/app-container))

(defn ^:dev/after-load start []
  (reload views/app-container))

(defn ^:export init []
  (start))
