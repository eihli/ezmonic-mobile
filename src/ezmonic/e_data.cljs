(ns ezmonic.e-data
  (:require [cljs.reader :as reader])
  (:require-macros [ezmonic.util :refer [slurp]]))

#_(defonce data
  (reader/read-string (slurp "number-to-word-tree.edn")))

#_(time (reader/read-string (slurp "number-to-word-tree.edn")))

#_(def json-data (js/require "../../assets/number-to-word-tree.json"))

#_(defn ow-get-in [obj keys]
  (if (empty? keys)
    obj
    (recur (goog.object/get obj (first keys)) (rest keys))))




