(ns ezmonic.util
  (:require [clojure.math.combinatorics :refer [selections]]
            [clojure.set]
            [clojure.string :as string]
            [cognitect.transit :as transit]
            [ezmonic.config :as config]
            [ezmonic.number-to-word-tree :as mnemonic-data]))

(defn pro-version? [] (= :pro (:flavor @config/config)))
(defn free-version? [] (not (pro-version?)))
(defn max-phrase-options [] (:max-phrase-options @config/config))
(defn max-saved-mnemonics [] (:max-saved-mnemonics @config/config))

(defn get-number-to-word-tree
  "Data structure looks like this:
  {0 {0 {0 {:terminals [\"ASSESSES\"]}
         1 {:terminals [\"ASSIST\" \"ASSESSED\"]
            1 {:terminals [\"ASSISTED\"]}
            ,,,}
         ,,,}
      ,,,}
   1 {0 {:terminals [\"TASE\"]
         0 {,,,}
         ,,,}
       ,,,}
   ,,,}

  In hindsight, this was not the best structure to meet the needs
  of the algorithm.

  The original idea was... find the longest number you can
  find words for, i.e. traverse the tree as far as you can
  go, then work your way back making smaller words at each
  step. Or, given 001, show me a list of all the longer words
  that could be made from that prefix.

  It's kind of silly though for what this app needs. You could just have a flat
  hash-map and things would be a lot simpler.

  {\"000\" [\"ASSESSES\"]
   \"001\" [\"ASSIST\" \"ASSESSED\"]
   \"0011\" [\"ASSISTED\"]}
  "
  []
  (let [reader (transit/reader :json)
        data mnemonic-data/number-to-word-tree]
    (transit/read reader data))) 

(defonce number-to-word-tree (get-number-to-word-tree))

(defn connections
  "I think there's a built-in Clojure function that gives us
  what we are trying to do with this. See `partitions`."
  [length]
  (selections [0 1] length))

(defn singles [coll] (filter #(= (count %) 1) coll))
(defn single? [coll])

(defn num-to-min
  "TODO: I think this might be a performance bug. The idea (I think)
  is to ignore 1-digit words for numbers that are over a certain
  number of digits. See how this interacts with ezminations and
  filter-to-threshold."
  [num]
  (cond
    (<= num 6) 3
    (<= num 8) 2
    (<= num 10) 1
    :else 0))

(defn filter-to-threshold
  ;; If there's more than 8 digits, filter out singles.
  ;; Also filter out phrases that have more than half
  ;; their words as singles.
  [ezminations min]
  (filter (fn [ezmination]
            (<= (count
                 (filter (fn [combo]
                           (= 1 (count combo)))
                         ezmination))
                min))
          ezminations))

(defn joiner
  ;; There are much better ways to do this. I think the builtin `partitions`
  ;; would be equivalent but better.
  ;; "12345"
  ;; [0 0 0 1]
  ;; [1, 2, 3, 45]
  ;; [0 0 1 1]
  ;; [1, 2, 345]
  ;; 1's are where we join words
  [acc [digit join]]
  (if (= join 1)
    (conj
     (if (empty? acc)
       acc
       (pop acc))
     (vec (conj (peek acc) digit)))
    (conj acc [digit])))

(defn ezminations
  "An `ezmination` is like a combination, but one that makes
  up a mnemonic. It's the selection of: [AFLAME VOICEMAIL] i.e.
  the grouping of 8538035 into the pairs of numbers [853 8035]
  to form the mnemonic `aflame voicemail`."
  [digits]
  (map
   (fn [connection]
     (reduce
      joiner
      [[(first digits)]]
      (map vector (rest digits) connection)))
   (connections (dec (count digits)))))

(defn -terminals [combo-el]
  (get-in number-to-word-tree (conj combo-el :terminals)))

(defn combo-to-phrase
  [combo]
  (map -terminals combo))

(defn all-phrases
  ;; Returns them in order longest to shortest.
  ;; For the number 4567,
  ;; Returns [<4-word mnemonics>, <3-words>, <2-words>, <1-words>]
  ;; Note these are lists of lists of lists.
  ;; 2-words would be [[[4..][567...]] [[45...][67...]]]
  ;; Will be nil if can't form phrase.
  ;; [[nil] [[45...][67...]]] means there are no [[4..][567]] phrases
  [number]
  (let [digits (map int number)
        combos (filter-to-threshold (ezminations digits) (num-to-min (count digits)))
        phrases (map combo-to-phrase combos)]
    phrases))

(defn shortest
  "Return thing with lowest `count`. Same as `min-by`, but with a +2 obfuscation bonus."
  [coll]
  (reduce
   #(if (< (count %1) (count %2))
      %1
      %2)
   coll))

(defn available-phrases
  ;; This also orders them from shortest to longest
  ;; since that's a more natural order. The ones we probably care
  ;; about are the fewest words to make up a mnemonic.
  [number]
  (reverse
   (filter
    (fn [phrase]
      ((complement some) nil? phrase))
    (all-phrases number))))

(defn all-mezmorizations
  ;; Shortest list of options to complete a mnemonic.
  ;; Given 3141592153, returns
  ;; a list of 31415 words and a list of 92153 words.
  ;; Technically, not "words" but a list of a word.
  ;; [word, phone, phone, phone, phone]
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


(def not-really-vowels #{"ER"})


(def consonants
  "Every phone that we don't consider to be a vowel.
   These are the sounds that we need to map to numbers."
  (clojure.set/union
   not-really-vowels
   (into #{} (map first (filter #(and
                                  (not= (second %) "vowel")
                                  (not= (second %) "semivowel")
                                  (not= (second %) "aspirate"))
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
   Ignore the W
   ER1 -> ER -> 4
   K         -> 7"
  {"T" 1
   "CH" 6
   "K" 7
   "L" 5
   "JH" 6
   "G" 7
   "M" 3
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
  "Converts a sequence of consonants to a sequence of numbers."
  [consonants]
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


(defn all-mnemonic-options
  [number]
  (let [all-mnemonic-possibilities (available-phrases number)]
    (map
     (fn [mnemonic-possibilities]
       (map
        (fn [mnemonic-possibility]
          (list (->> mnemonic-possibility
                     first
                     strip-vowels
                     normalize-consonants
                     (clojure.string/join ""))
                (mapv first mnemonic-possibility)))
        mnemonic-possibilities))
     all-mnemonic-possibilities)))

(defn e-all-mnemonic-options
  "Splits a long number into multiple smaller numbers
  so we don't time out while calculating a mnemonic"
  [number]
  (all-mnemonic-options number))

(defn e-number->mnemonics
  "Splits a long number into multiple smaller numbers
  so we don't time out while calculating a mnemonic
  DEPRECATED"
  [number]
  (vec (apply concat (map -e-number->mnemonics
                          (map string/join (partition 12 12 nil number))))))


