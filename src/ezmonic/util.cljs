(ns ezmonic.util
  (:require [clojure.math.combinatorics :refer [selections]]
            [clojure.set]
            [clojure.string :as string]
            [ezmonic.data]))


(defn connections
  [length]
  (selections [0 1] length))


(defn joiner
  [acc [digit join]]
  (if (= join 1)
    (conj
     (if (empty? acc)
       acc
       (pop acc))
     (vec (conj (peek acc) digit)))
    (conj acc [digit])))


(defn ezminations
  [digits]
  (map
   (fn [connection]
     (reduce
      joiner
      [[(first digits)]]
      (map vector (rest digits) connection)))
   (connections (dec (count digits)))))


(defn combo-to-phrase
  [combo]
  (map #(:terminals (get-in ezmonic.data/data %)) combo))


(defn all-phrases
  [number]
  (let [digits (map int number)
        combos (ezminations digits)
        phrases (map combo-to-phrase combos)]
    phrases))


(defn shortest
  [coll]
  (reduce
   #(if (< (count %1) (count %2))
      %1
      %2)
   coll))


(defn all-mezmorizations
  [number]
  (shortest
   (filter
    (fn [phrase]
      ((complement some) nil? phrase))
    (all-phrases number))))


(def phones '(["AA" "vowel"]
              ["AE" "vowel"]
              ["AH" "vowel"]
              ["AO" "vowel"]
              ["AW" "vowel"]
              ["AY" "vowel"]
              ["B" "stop"]
              ["CH" "affricate"]
              ["D" "stop"]
              ["DH" "fricative"]
              ["EH" "vowel"]
              ["ER" "vowel"]
              ["EY" "vowel"]
              ["F" "fricative"]
              ["G" "stop"]
              ["HH" "aspirate"]
              ["IH" "vowel"]
              ["IY" "vowel"]
              ["JH" "affricate"]
              ["K" "stop"]
              ["L" "liquid"]
              ["M" "nasal"]
              ["N" "nasal"]
              ["NG" "nasal"]
              ["OW" "vowel"]
              ["OY" "vowel"]
              ["P" "stop"]
              ["R" "liquid"]
              ["S" "fricative"]
              ["SH" "fricative"]
              ["T" "stop"]
              ["TH" "fricative"]
              ["UH" "vowel"]
              ["UW" "vowel"]
              ["V" "fricative"]
              ["W" "semivowel"]
              ["Y" "semivowel"]
              ["Z" "fricative"]
              ["ZH" "fricative"]))


(def not-really-vowels #{"ER" "W"})


(def consonants
  "Every phone that we don't consider to be a vowel.
   These are the sounds that we need to map to numbers."
  (clojure.set/union
   not-really-vowels
   (into #{} (map first (filter #(and
                                  (not= (second %) "vowel")
                                  (not= (second %) "semivowel"))
                                phones)))))


(defn strip-vowels
  [phones]
  (filter #(consonants %1) phones))


(def consonant-number-map
  "Used to map words from the CMU pronouncing dictionary to numbers.
   For the entry `OVERWORK  OW2 V ER0 W ER1 K`:
   Ignore the `OW2` since it's not a consonant.
   V         -> 8
   ER0 -> ER -> 4  ;; The 0/1/2 at the end is the stress, we don't care about it.
   W         -> 3
   ER1 -> ER -> 4
   K         -> 7"
  {"T" 1
   "CH" 6
   "K" 7
   "HH" 2
   "L" 5
   "JH" 6
   "G" 7
   "M" 3
   "W" 3
   "S" 0
   "Z" 0
   "R" 4
   "ER" 4
   "F" 8
   "B" 9
   "SH" 6
   "P" 9
   "V" 8
   "TH" 1
   "N" 2
   "DH" 1
   "ZH" 6
   "NG" 7
   "D" 1})


(defn normalize-consonants
  [consonants]
  "Converts a sequence of consonants to a sequence of numbers."
  (map consonant-number-map consonants))


(defn number->mnemonics
  "Given a `number`, return all the mnemonics, grouped by the numbers as
  keys and mnemonics as values e.g. for 333 it returns:

  ({33 [MAMA MEMO MIME MOM]}
   {3 [AIM EMU WAY WHEY YAM]})"
  [number]
  (into {} (for [phrase-option (all-mezmorizations number)]
             (hash-map (->> phrase-option
                            first
                            strip-vowels
                            normalize-consonants
                            (clojure.string/join ""))
                       (mapv first phrase-option)))))



(defn -e-number->mnemonics
  "Given a `number`, return all the mnemonics, grouped by the numbers as
  keys and mnemonics as values e.g. for 333 it returns:

  ({33 [MAMA MEMO MIME MOM]}
   {3 [AIM EMU WAY WHEY YAM]})"
  [number]
  (into [] (for [phrase-option (all-mezmorizations number)]
             (list (->> phrase-option
                        first
                        strip-vowels
                        normalize-consonants
                        (clojure.string/join ""))
                   (mapv first phrase-option)))))


(defn e-number->mnemonics
  "Splits a long number into multiple smaller numbers
  so we don't time out while calculating a mnemonic"
  [number]
  (vec (apply concat (map -e-number->mnemonics
                          (map string/join (partition 12 12 nil number))))))


;; (defn mnemonic-number
;;   "Given a mnemonic, returns the number it's a mnemonic for.

;;   This helper is specific to how a mnemonic is saved in app-db.
;;   A mnemonic in app-db is just a collection of pieces of a mnemonic.
;;   To get the full original number, we need to map over the collection
;;   and join together all the individual numbers for each word of the
;;   mnemonic phrase."
;;   [mnemonic]
;;   (string/join "" []))
