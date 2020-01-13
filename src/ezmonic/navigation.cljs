(ns ezmonic.navigation
  (:require ["react-navigation" :as react-navigation]))

(def navigator-ref (atom nil))

(def navigation-actions
  (.-NavigationActions react-navigation))

;; This is a bit unfortunate.
;; I want to keep all state in `app-db` in true re-frame style.
;; But it's not straight-forward to integrate re-frame with
;; ReactNavigation. ReactNavigation keeps state in component's
;; props. There's a library that attempts to integrate the two,
;; https://github.com/vikeri/re-navigate
;; but it's still out of my reach. So for now, the plan is
;; to let ReactNavigation handle just the route-name in
;; the state of a component. The route params will be handled
;; by re-frame. This divergence is the most unfortunate bit.
;;
;; Also, I'm not supporting the nested navigation that
;; is mentioned in the ReactNavigation docs where you
;; navigate to a parent route and provide an action
;; key with the value of a child route. I'm just using
;; unique keys across the board to navigate straight to
;; any view. This is just easier for me to get working
;; and it's currently sufficient.
(defn navigate
  [view-id]
  (.dispatch
   ^js/object @navigator-ref
   (.navigate navigation-actions #js {:routeName (name view-id)})))

(defn set-navigator-ref [nav]
  (reset! navigator-ref nav))
