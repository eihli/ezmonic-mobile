(ns ezmonic.util
  (:require [reagent.core :as r]
            [clojure.java.io :as io]))

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

(defmacro slurp [file]
  (clojure.core/slurp (io/file (io/resource file))))

