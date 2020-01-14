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

;; -- Coeffects

(reg-cofx
 :number-to-mnemorize
 (fn [cofx _]
   (assoc cofx :number-to-mnemorize (get-in cofx [:db :number-to-mnemorize]))))

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
   (assoc db :saved-mnemonics (or (cljs.reader/read-string value) {}))))

(reg-event-fx
 :initialize-db
 validate-spec
 (fn [_ _]
   {:db e-app-db
    :async-storage-get {:key "saved-mnemonics"
                        :on-success :async-storage-get-success
                        :on-failure :async-storage-get-failure}}))

(reg-event-db
 :show-welcome?
 (fn [db [_ value]]
   (assoc db :show-welcome? value)))

(reg-event-db
 :select-value
 (fn [db [_ picker-position item-value item-position]]
   (assoc-in db
             [:mnemonic picker-position ::db/mnemonic-chosen-word]
             item-value)))

(reg-event-db
 :number-input-changed
 (fn [db [_ number]]
   (assoc db :number-to-mnemorize number)))

(reg-event-fx
 :mnemonic-submitted-for-calculation
 [debug
  validate-spec]
 (fn [cofx [_ number-to-mnemorize]]
   (let [db (:db cofx)]
     {:db (assoc db
                 :calculating-mnemonic? true
                 :submitted-number number-to-mnemorize)
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
           :mnemonic
           (vec (map
                 (fn [mnemonic-subphrase]
                   {::db/mnemonic-number (first mnemonic-subphrase)
                    ::db/mnemonic-word-choices (second mnemonic-subphrase)
                    ::db/mnemonic-chosen-word (first (second mnemonic-subphrase))})
                 (util/e-number->mnemonics number-to-memorize))))
          (assoc :calculating-mnemonic? false))})))

(reg-event-db
 :input-value
 (fn [db [_ value]]
   (assoc db :input-value value)))

(reg-event-db
 :submitted-number
 (fn [db [_ value]]
   (assoc db :submitted-number value)))

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
 :show-welcome
 (fn [db [_ value]]
   (assoc db :show-welcome value)))

(reg-event-db
 :switch
 (fn [db [_ value]]
   (assoc db :switch value)))

(reg-event-db
 :editable-mnemonic-story-changed
 validate-spec
 (fn [db [_ new-value]]
   (assoc db :editable-mnemonic-story new-value)))

(reg-fx
 :persist-mnemonics
 (fn [mnemonics]
   (-> AsyncStorage
       (.setItem "saved-mnemonics" (pr-str mnemonics))
       (.then #(print "Success " mnemonics))
       (.catch #(print "Error saving: " % %2)))))

(reg-event-fx
 :editable-mnemonic-story-submitted
 validate-spec
 (fn [{:keys [db]} [_ number mnemonic story]]
   (let [saved-mnemonics (-> db
                             (:saved-mnemonics)
                             (assoc number {::db/mnemonic mnemonic
                                            ::db/mnemonic-story story}))]
     {:db (assoc db :saved-mnemonics saved-mnemonics)
      :persist-mnemonics saved-mnemonics})))

(defn my-test []
  (-> AsyncStorage
      (.getItem "saved-mnemonics")
      (.then #(print % %2))))

(reg-event-fx
 :update-users
 (fn [{:keys [db]} [_ number]]
   (let [users (-> db
                   (:users)
                   (assoc "Eric" {:phone-number number}))]
     {:db (assoc db :users users)})))
