(ns ezmonic.views.core
  (:require [reagent.core :as r]
            [ezmonic.views.saved-mnemonics :as saved-mnemonics]
            [ezmonic.navigation :as navigation]
            [ezmonic.views.help :as help]
            [ezmonic.views.home :as home]
            ["react-navigation" :as react-navigation]
            ["react-navigation-stack" :as react-navigation-stack]
            ["react-navigation-tabs" :as react-navigation-tabs]))


(def create-stack-navigator
  (.-createStackNavigator react-navigation-stack))
(def create-bottom-tab-navigator
  (.-createBottomTabNavigator react-navigation-tabs))
(def create-app-container
  (.-createAppContainer react-navigation))

(def app-bottom-tab-navigator
  (create-bottom-tab-navigator
   #js {:home home/home-stack
        :saved saved-mnemonics/saved-stack
        :help help/help-stack}))

;; Since ReactNavigation stores its state as component properties,
;; we'll use the functionality they give us for persisting
;; and loading navigation state.
;;
;; https://reactnavigation.org/docs/en/state-persistence.html
(defonce nav-state (atom nil))

(defn persist-navigation-state [state]
  (js/Promise. (fn [resolve]
                 (resolve (reset! nav-state state)))))

(defn load-navigation-state []
  (js/Promise. (fn [resolve]
                 (resolve @nav-state))))

(defn app-container []
  [(r/adapt-react-class (create-app-container app-bottom-tab-navigator))
   {:ref (fn [r] (reset! navigation/navigator-ref r))
    :persistNavigationState persist-navigation-state
    :loadNavigationState load-navigation-state}])

