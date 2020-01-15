(ns ezmonic.subs
  (:require [re-frame.core :refer [reg-sub]]
            [ezmonic.db :as db]))

(reg-sub
 :mnemonic
 (fn [db]
   (::db/mnemonic db)))

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
 :picker-data
 (fn [db _]
   (:picker-data db)))

(reg-sub
 :navigation
 (fn [db _]
   (:navigation db)))

(reg-sub
 :editable-mnemonic-story
 (fn [db _]
   (:editable-mnemonic-story db)))

(reg-sub
 :saved-mnemonics
 (fn [db _]
   (::db/mnemonics db)))

(reg-sub
 :screen-params
 (fn [db _]
   (:screen-params db)))

(reg-sub
 :mnemonic-to-edit
 (fn [db _]
   (:mnemonic-to-edit db)))

(reg-sub
 :saved-mnemonic
 (fn [db [_ number]]
   (get (::db/mnemonics db) number)))

