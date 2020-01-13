(ns ezmonic.util
  (:require [reagent.core :as r]))

(defmacro defnav [name bindings body navigation-options]
  (let [fn-name (gensym 'fn-name)
        comp (gensym 'comp)]
    `(do
       (defn ~fn-name ~bindings ~body)
       (def ~name
           (let [~comp (r/reactify-component ~fn-name)]
             (doto ~comp
               (goog.object/set "navigationOptions" ~navigation-options))
             ~comp)))))