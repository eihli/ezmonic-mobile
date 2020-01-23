(ns ezmonic.prod
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            ["react-native" :as rn]
            [ezmonic.views.core :as views]
            [ezmonic.subs]
            [shadow.cljs.devtools.client.react-native :as shadow-rn]
            [ezmonic.events]))

(rf/dispatch-sync [:initialize-db])

(defn start []
  (rn/AppRegistry.registerComponent
   "Ezmonic Free"
   (fn [] (r/reactify-component views/app-container))))

(defn ^:export init []
  (start))


