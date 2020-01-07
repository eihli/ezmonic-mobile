(ns ezmonic.views.shared
  (:require [ezmonic.style :as style]
            ["react-native" :refer [View
                                    Text
                                    TouchableHighlight]]))

(defn edit-bar [text navigation nav-path]
  [:> View style/edit-bar
   [:> Text text]
   [:> TouchableHighlight
    {:on-press (fn []
                 (. navigation navigate "edit"))}
    [:> Text "Edit"]]])
