(ns ezmonic.db
  (:require [clojure.spec.alpha :as s]))

(s/def ::show-welcome? boolean?)
(s/def ::submitted-number string?)
(s/def ::number-to-mnemorize string?)
(s/def ::mnemonic-number string?)
(s/def ::mnemonic-word-choices (s/coll-of string?))
(s/def ::navigation string?)
(s/def ::mnemonic-chosen-word
  (s/or :word string?
        :empty nil?))
(s/def ::editable-mnemonic-story string?)
(s/def ::mnemonic-story string?)
(s/def ::calculating-mnemonic? boolean?)

(s/def ::mnemonic-subelement
  (s/keys :req [::mnemonic-number
                ::mnemonic-word-choices
                ::mnemonic-chosen-word]))
(s/def ::mnemonic (s/coll-of ::mnemonic-subelement))

(s/def ::saved-mnemonic
  (s/keys :req [::mnemonic
                ::mnemonic-story]))
(s/def ::saved-mnemonics (s/map-of string? ::saved-mnemonic))

(s/def ::e-app-db (s/keys :req-un [::show-welcome?
                                   ::submitted-number
                                   ::mnemonic
                                   ::number-to-mnemorize
                                   ::calculating-mnemonic?
                                   ::saved-mnemonics
                                   ::navigation]))

(defonce e-app-db {:show-welcome? false
                   :submitted-number ""
                   :mnemonic []
                   :number-to-mnemorize ""
                   :calculating-mnemonic? false
                   :navigation "home"
                   :editable-mnemonic-story ""
                   :saved-mnemonics {}})
