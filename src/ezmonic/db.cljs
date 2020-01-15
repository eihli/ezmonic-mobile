(ns ezmonic.db
  (:require [clojure.spec.alpha :as s]))

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
(s/def ::screen-params ::mnemonic)
(s/def ::e-app-db (s/keys :req-un [::mnemonic
                                   ::calculating-mnemonic?
                                   ::saved-mnemonics
                                   ::navigation]))

;; Refactor to a more reasonable data model
(s/def ::name string?)
(s/def ::number string?)
(s/def ::story string?)
(s/def ::chosen-word string?)
(s/def ::word-choices (s/coll-of string?))
(s/def ::element
  (s/keys :req [::number
                ::word-choices
                ::chosen-word]))
(s/def ::elements (s/coll-of ::element))
(s/def ::new-mnemonic
  (s/keys :req [::name
                ::number
                ::story
                ::elements]))
(s/def ::new-mnemonics
  (s/map-of ::name ::new-mnemonic))

(defonce e-app-db {:mnemonic []
                   :calculating-mnemonic? false
                   :navigation "home"
                   :editable-mnemonic-story ""
                   :saved-mnemonics {}
                   ::new-mnemonic {::name ""
                                   ::number ""
                                   ::story ""
                                   ::elements []} ;; Transient
                   ::new-mnemonics {}}) ;; Saved
