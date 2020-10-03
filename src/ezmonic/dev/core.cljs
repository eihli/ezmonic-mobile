(ns ezmonic.dev.core
  (:require ["react-native" :as rn]))

(defn clear-async-storage
  []
  (.clear rn/AsyncStorage))
                                      


