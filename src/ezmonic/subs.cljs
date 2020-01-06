(ns ezmonic.subs
  (:require [re-frame.core :refer [reg-sub]]))


(reg-sub
 :show-welcome?
 (fn [db]
   (:show-welcome? db)))

(reg-sub
 :number-to-mnemorize
 (fn [db]
   (:number-to-mnemorize db)))

(reg-sub
 :mnemonic
 (fn [db]
   (:mnemonic db)))

(reg-sub
 :calculating-mnemonic?
 (fn [db]
   (:calculating-mnemonic? db)))

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

(reg-sub
 :navigation
 (fn [db _]
   (:navigation db)))

(reg-sub
 :show-welcome
 (fn [db _]
   (:show-welcome db)))

(reg-sub
 :switch
 (fn [db _]
   (:switch db)))

(reg-sub
 :editable-mnemonic-story
 (fn [db _]
   (:editable-mnemonic-story db)))

(reg-sub
 :saved-mnemonics
 (fn [db _]
   (:saved-mnemonics db)))
