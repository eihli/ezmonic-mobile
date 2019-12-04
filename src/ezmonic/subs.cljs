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

(reg-sub
 :submitted-number
 (fn [db _]
   (:submitted-number db)))

(reg-sub
 :picker-data
 (fn [db _]
   (:picker-data db)))
