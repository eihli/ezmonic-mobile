(ns ezmonic.navigation
  (:require ["react-navigation" :refer [NavigationActions]]))

(def navigator-ref (atom nil))

(defn set-navigator-ref [nav]
  (reset! navigator-ref nav))

(defn navigate-back []
  ;; What do we do about this type hint? Without it, we get a warning.
  (.dispatch ^js/object @navigator-ref (.back NavigationActions)))

(defn navigate-to [route & params]
  (.dispatch
   ^js/object @navigator-ref
   (.navigate NavigationActions #js {:routeName route
                                     :params params})))
