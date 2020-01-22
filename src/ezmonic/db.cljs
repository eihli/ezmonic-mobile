(ns ezmonic.db
  (:require [clojure.spec.alpha :as s]))

(s/def ::navigation string?)
(s/def ::calculating-mnemonic? boolean?)

(s/def ::uuid uuid?)
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
(s/def ::all-possible-elements (s/coll-of ::elements))
(s/def ::mnemonic
  (s/keys :opt [::uuid
                ::name
                ::number
                ::story
                ::elements]))
(s/def ::all-possible-mnemonic
  (s/keys :opt [::uuid
                ::name
                ::number
                ::story
                ::all-possible-elements]))
(s/def ::screen-params ::mnemonic)
(s/def ::mnemonics
  (s/map-of ::uuid ::mnemonic))


(s/def ::e-app-db (s/keys :req [::mnemonic
                                ::all-possible-mnemonic
                                ::calculating-mnemonic?
                                ::mnemonics
                                ::navigation
                                ::all-possible-mnemonics]))

(defonce e-app-db {::calculating-mnemonic? false
                   ::navigation "home"
                   ::all-possible-mnemonic: {}
                   ::mnemonic {} ;; Transient
                   ::mnemonics {}}) ;; Saved

