(ns ezmonic.app
  (:require [ezmonic.subs]
            [ezmonic.events]
            [ezmonic.views]
            ["react-native" :as rn]
            [re-frame.core :as rf]
            [reagent.core :as r]))


(rf/dispatch-sync [:initialize-db])

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
                                            (print this)
                                            (reset! root-component-ref this)))
                   :component-will-unmount (fn []
                                             (reset! root-component-ref nil))})]
        (rn/AppRegistry.registerComponent "Ezmonic" (fn [] Root))))))

(defn start ^:dev/after-load []
  (render-root (r/as-element [ezmonic.views/app-container])))

(defn ^:export init []
  (start))
