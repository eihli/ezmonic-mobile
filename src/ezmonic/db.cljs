(ns ezmonic.db
  (:require [clojure.spec.alpha :as s]))

;; spec of app-db
(s/def ::counter number?)
(s/def ::app-db
  (s/keys :req-un [::counter]))

(s/def ::show-welcome? boolean?)
(s/def ::submitted-number string?)
(s/def ::number-to-mnemorize string?)
(s/def ::mnemonic-number string?)
(s/def ::mnemonic-word-choices (s/coll-of string?))
(s/def ::navigation string?)
(s/def ::mnemonic-chosen-word
  (s/or :word string?
        :empty nil?))
(s/def ::calculating-mnemonic? boolean?)
(s/def ::mnemonic-subelement
  (s/keys :req-un [::mnemonic-number
                   ::mnemonic-word-choices
                   ::mnemonic-chosen-word]))
(s/def ::mnemonic (s/coll-of ::mnemonic-subelement))

(s/def ::e-app-db (s/keys :req-un [::show-welcome?
                                   ::submitted-number
                                   ::mnemonic
                                   ::number-to-mnemorize
                                   ::calculating-mnemonic?
                                   ::navigation]))

#_(def example-db
  {:show-welcome false
   :mnemonic [{:mnemonic-number "5"
               :mnemonic-word-choices ["Lee" "Lie"]
               :mnemonic-chosen-word "Lee"}]
   :number-to-mnemorize "5"})
#_(s/explain ::e-app-db example-db)

(defonce e-app-db {:show-welcome? false
                   :show-welcome false
                   :submitted-number ""
                   :mnemonic []
                   :number-to-mnemorize ""
                   :calculating-mnemonic? false
                   :navigation "home"})

;; initial state of app-db
(defonce app-db {:counter 0
                 :show-welcome false})
