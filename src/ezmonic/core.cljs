(ns ezmonic.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            ["react-native" :as rn]
            [ezmonic.views.core :as views]
            [ezmonic.subs]
            [shadow.cljs.devtools.client.react-native :as shadow-rn]
            [ezmonic.events]))

(rf/dispatch-sync [:initialize-db])

(defn on-app-state-change
  "Fixes issue with shadow-cljs repl losing connection to websocket.

  Put this in some root component's `component-did-mount`.
  https://stackoverflow.com/questions/40561073/websocket-not-closed-on-reload-appreact-native
  "
  [state]
  (cond
    (= state "background")
    (.close @shadow-rn/socket-ref)

    (and (= state "active")
         (nil? @shadow-rn/socket-ref))
    (shadow-rn/ws-connect)))

(defn make-reloader
  [component]
  (let [component-ref (r/atom component)]
    (letfn [(render []
              (let [component @component-ref]
                (if (fn? component)
                  (component)
                  component)))]
      (let [wrapper (r/create-class
                     {:render render
                      :component-did-mount
                      (fn []
                        (.addEventListener rn/AppState "change" on-app-state-change))

                      :component-will-unmount
                      (fn []
                        (.removeEventListener rn/AppState "change" on-app-state-change))})]
        
        (rn/AppRegistry.registerComponent "Ezmonic" (fn [] wrapper))
        (fn [comp]
          (reset! component-ref comp))))))

(defonce reload (make-reloader views/app-container))

(defn ^:dev/after-load start []
  (reload views/app-container))

(defn ^:export init []
  (start))
