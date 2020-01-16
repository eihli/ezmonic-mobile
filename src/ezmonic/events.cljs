(ns ezmonic.events
  (:require
   [re-frame.core :refer [reg-event-db
                          after
                          ->interceptor
                          reg-event-fx
                          debug
                          reg-cofx
                          reg-fx
                          inject-cofx]]
   [cljs.reader]
   [day8.re-frame.async-flow-fx]
   [clojure.spec.alpha :as s]
   [ezmonic.db :as db :refer [e-app-db]]
   [ezmonic.util :as util]
   [ezmonic.navigation :as navigation]
   [re-frame.core :as rf]
   ["react-native" :refer [AsyncStorage]]))

;; -- Interceptors ------------------------------------------------------------
;;
;; See https://github.com/Day8/re-frame/blob/master/docs/Interceptors.md
;;
(defn check-and-throw
  "Throw an exception if db doesn't have a valid spec."
  [spec db]
  (when-not (s/valid? spec db)
    (let [explain-data (s/explain-data spec db)]
      (throw (ex-info (str "Spec check after failed: " explain-data) explain-data)))))

(def validate-spec
  (do
    (if goog.DEBUG
      (after (partial check-and-throw ::db/e-app-db))
      [])))

(def toggle-calculating-mnemonic
  (->interceptor
   :id :toggle-calculating-mnemonic
   :before (fn [context]
             (assoc-in context [:coeffects :db :calculating-mnemonic?] true))
   :after (fn [context]
            (assoc-in context [:effects :db :calculating-mnemonic?] false))))

(def get-number-input
  (->interceptor
   :id :get-number-input
   :before (fn [context]
             ())))

;; -- Async Flows

;; -- Effects --
(reg-fx
 :async-storage-get
 (fn [{:keys [key on-success on-failure]}]
   (->
    AsyncStorage
    (.getItem key)
    (.then #(rf/dispatch [on-success key % %2]))
    (.catch #(print "Error " % %2)))))  ;; Better handling

;; -- Handlers --------------------------------------------------------------
(reg-event-db
 :async-storage-get-success
 (fn [db [_ key value]]
   (assoc db ::db/mnemonics (or (cljs.reader/read-string value) {}))))

(reg-event-fx
 :initialize-db
 validate-spec
 (fn [_ _]
   {:db e-app-db
    :async-storage-get {:key "saved-mnemonics"
                        :on-success :async-storage-get-success
                        :on-failure :async-storage-get-failure}}))

(reg-event-db
 :select-value
 (fn [db [_ picker-position item-value item-position]]
   (assoc-in db
             [:mnemonic picker-position ::db/mnemonic-chosen-word]
             item-value)))

(reg-event-fx
 :mnemonic-submitted-for-calculation
 [debug
  validate-spec]
 (fn [cofx [_ number-to-mnemorize]]
   (let [db (:db cofx)]
     {:db (assoc db :calculating-mnemonic? true)
      :dispatch-later [{:ms 20
                        :dispatch ^:flush-dom [:calculate-mnemonic number-to-mnemorize]}]})))

(reg-event-fx
 :calculate-mnemonic
 [debug
  validate-spec]
 (fn [cofx [_ number-to-memorize]]
   (let [db (:db cofx)]
     {:db
      (-> (:db cofx)
          (assoc
           ::db/mnemonic
           {::db/name ""
            ::db/number number-to-memorize
            ::db/story ""
            ::db/elements (vec (map
                                (fn [mnemonic-subphrase]
                                  {::db/number (first mnemonic-subphrase)
                                   ::db/word-choices (second mnemonic-subphrase)
                                   ::db/chosen-word (first (second mnemonic-subphrase))})
                                (util/e-number->mnemonics number-to-memorize)))})
          (assoc :calculating-mnemonic? false))})))

(reg-event-db
 :input-value
 (fn [db [_ value]]
   (assoc db :input-value value)))

(reg-event-db
 :picker-data
 (fn [db [_ value]]
   (assoc db :picker-data
          (merge (:picker-data db) value))))

(reg-event-db
 :navigation
 (fn [db [_ value]]
   (assoc db :navigation value)))

(reg-event-db
 :switch
 (fn [db [_ value]]
   (assoc db :switch value)))

(reg-event-db
 :editable-mnemonic-story-changed
 validate-spec
 (fn [db [_ new-value]]
   (assoc db :editable-mnemonic-story new-value)))

;; Effects
(rf/reg-fx
 ::react-navigate
 (fn [view-id]
   (navigation/navigate (name view-id))))

;; For simplicity, chose to have only
;; one key in the database for every
;; screen's params. Since this is all handled by
;; events, it's possible for this to get wonky
;; and out of sync. If one screen expects a certain
;; type of params but it somehow gets rendered before
;; the value is updated in `app-db`, then it might
;; blow up. Pragmatically, I don't think this will
;; be a problem. But if it is, it should be simple enough
;; to figure out a robust alternative.
(rf/reg-event-fx
 :navigate
 (fn [{:keys [db]} [_ [view-id params]]]
   {:db (assoc-in db [:screen-params] params)
    ::react-navigate view-id}))

(reg-fx
 :persist-mnemonics
 (fn [mnemonics]
   (-> AsyncStorage
       (.setItem "saved-mnemonics" (pr-str mnemonics))
       (.then #(print "Success " mnemonics))
       (.catch #(print "Error saving: " % %2)))))

(reg-event-fx
 :save-mnemonic
 validate-spec
 (fn [{:keys [db]} [_ mnemonic]]
   (let [uuid (or (::db/uuid mnemonic) (random-uuid))
         mnemonic (merge mnemonic {::db/uuid uuid})
         db (assoc-in db [::db/mnemonics uuid] mnemonic)]
     {:db db
      :persist-mnemonics (::db/mnemonics db)})))

(reg-event-fx
 :delete-mnemonic
 validate-spec
 (fn [{:keys [db]} [_ uuid]]
   (let [db (update-in db [::db/mnemonics] dissoc uuid)]
     {:db db
      :persist-mnemonics (::db/mnemonics db)
      ::react-navigate :saved-home})))

(reg-event-fx
 :editable-mnemonic-story-submitted
 validate-spec
 (fn [{:keys [db]} [_ number mnemonic]]
   (let [saved-mnemonics (-> db
                             (:saved-mnemonics)
                             (assoc number mnemonic))]
     {:db (assoc db :saved-mnemonics saved-mnemonics)
      :persist-mnemonics saved-mnemonics})))

(reg-event-fx
 :update-users
 (fn [{:keys [db]} [_ number]]
   (let [users (-> db
                   (:users)
                   (assoc "Eric" {:phone-number number}))]
     {:db (assoc db :users users)})))
