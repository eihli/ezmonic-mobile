(ns ezmonic.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
 :get-counter
 (fn [db _]
   (:counter db)))

(reg-sub
 :input-value
 (fn [db _]
   (:input-value db)))
