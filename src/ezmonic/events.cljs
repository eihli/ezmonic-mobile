(ns ezmonic.events
  (:require
   [re-frame.core :refer [reg-event-db after ->interceptor reg-event-fx debug reg-cofx inject-cofx]]
   [clojure.spec.alpha :as s]
   [ezmonic.db :as db :refer [app-db e-app-db]]
   [ezmonic.util :as util]
   [re-frame.core :as rf]))

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
  (if goog.DEBUG
    (after (partial check-and-throw ::db/app-db))
    []))

(def validate-e-spec
  (do
    (print "Validating spec")
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

;; -- Handlers --------------------------------------------------------------

(reg-event-db
 :initialize-e-db
 validate-e-spec
 (fn [_ _]
   e-app-db))

(reg-event-db
 :show-welcome?
 (fn [db [_ value]]
   (assoc db :show-welcome? value)))

(reg-event-db
 :select-value
 (fn [db [_ picker-position item-value item-position]]
   (assoc-in db [:mnemonic picker-position :mnemonic-chosen-word] item-value)))

(reg-event-db
 :number-input-changed
 (fn [db [_ number]]
   (do
     (print (str "Number changed: " number))
     (assoc db :number-to-mnemorize number))))

(reg-event-fx
 :calculate-mnemonic
 [debug
  validate-e-spec
  (inject-cofx :number-to-mnemorize)]
 (fn [cofx event]
   (let [val (:number-to-mnemorize cofx)
         db (:db cofx)]
     (do
       (print (str "received " val))
       (print (map
               (fn [mnemonic-subphrase]
                 {:mnemonic-number (first mnemonic-subphrase)
                  :mnemonic-word-choices (second mnemonic-subphrase)
                  :mnemonic-chosen-word (first (second mnemonic-subphrase))})
               (util/e-number->mnemonics val)))
       (print cofx)
       {:db (-> (:db cofx)
                (assoc :mnemonic (vec (map
                                 (fn [mnemonic-subphrase]
                                   {:mnemonic-number (first mnemonic-subphrase)
                                    :mnemonic-word-choices (second mnemonic-subphrase)
                                    :mnemonic-chosen-word (first (second mnemonic-subphrase))})
                                 (util/e-number->mnemonics val))))
                (assoc :submitted-number val))}))))


#_(assoc {} :mnemonic (map
                        (fn [mnemonic-subphrase]
                          {:mnemonic-number (first mnemonic-subphrase)
                           :mnemonic-word-choices (second mnemonic-subphrase)
                           :mnemonic-chosen-word (first (second mnemonic-subphrase))})
                        (util/e-number->mnemonics "52")))
#_(defn do-test []
  (do
    (rf/dispatch-sync [:initialize-e-db])
    (rf/dispatch [:calculate-mnemonic "52"])))

(reg-event-db
 :initialize-db
 validate-spec
 (fn [_ _]
   app-db))

(reg-event-db
 :inc-counter
 validate-spec
 (fn [db [_ _]]
   (update db :counter inc)))

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
