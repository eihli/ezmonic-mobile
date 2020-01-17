(ns ezmonic.e-data
  (:require [cljs.reader :as reader])
  (:require-macros [ezmonic.util :refer [slurp]]))

(defonce data
  (reader/read-string (slurp "number-to-word-tree.edn")))
