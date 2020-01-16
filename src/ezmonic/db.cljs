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
(s/def ::mnemonic
  (s/keys :req [::uuid
                ::name
                ::number
                ::story
                ::elements]))
(s/def ::screen-params ::mnemonic)
(s/def ::mnemonics
  (s/map-of ::uuid ::mnemonic))

(s/def ::e-app-db (s/keys :req [::mnemonic
                                ::calculating-mnemonic?
                                ::mnemonics
                                ::navigation]))

(defonce e-app-db {::calculating-mnemonic? false
                   ::navigation "home"
                   ::mnemonic {::name ""
                               ::number ""
                               ::story ""
                               ::elements []} ;; Transient
                   ::mnemonics {}}) ;; Saved

