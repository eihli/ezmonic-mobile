(ns ezmonic.util
  (:require [clojure.math.combinatorics :as combo]
            [clojure.set]
            [clojure.string :as string]
            [ezmonic.data]))


(defn connections
  [length]
  (combo/selections [0 1] length))


(defn joiner [acc [digit join]]
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

(defn normalize-consonants [consonants]
  "Converts a sequence of consonants to a sequence of numbers."
  (map consonant-number-map consonants))

(def not-really-vowels
  #{"ER" "W"})

(def consonants
  "Every phone that we don't consider to be a vowel.
   These are the sounds that we need to map to numbers."
  (clojure.set/union
   not-really-vowels
   (into #{} (map first (filter #(and
                                  (not= (second %) "vowel")
                                  (not= (second %) "semivowel"))
                                phones)))))

(defn strip-vowels [phones]
  (filter #(consonants %1) phones))

(defn add-word-to-tree [tree word]
  (let [path (normalize-consonants (strip-vowels word))
        node (get-in tree path)]
    (assoc-in tree path
              (if (empty? node)
                {:terminals [word]}
                (merge node {:terminals (conj (:terminals node) word)})))))


(def words
  (let [nouns (set nouns)
        words (map prepare-word dictionary)]
    (filter #(nouns (string/trim (string/lower-case (first %)))) words)))


(def number-to-word-tree
  "This is a useful data structure we can use in our algorithm.

  {0: {:terminals ['Say' 'Sew', ...]
       0: {:terminals ['Says' 'Sis', ...]
           0: {terminals []}
           1: {terminals ['Assist']
               0: ...
               1: ...}
           2: ...}
       1: {:terminals ['Sat', 'Sit', ...]
           0: ...}
       2: ...}
   1: ...}

  So to get a word for the number 101 we can split
  the number into a sequence and then traverse through
  the data structure. If there are any terminals at 1 -> 0 -> 1,
  then we return that list of terminals.
  If there are no terminals at 1 -> 0 -> 1, then we
  can check 1 -> 0 and return that list, then check 1 and return that list.
  "
  (reduce add-word-to-tree {} words))




(defn normalize-consonants [consonants]
  "Converts a sequence of consonants to a sequence of numbers."
  (map consonant-number-map consonants))


(defn add-word-to-tree [tree word]
  (let [path (normalize-consonants (strip-vowels word))
        node (get-in tree path)]
    (assoc-in tree path
              (if (empty? node)
                {:terminals [word]}
                (merge node {:terminals (conj (:terminals node) word)})))))


(defn combo-to-phrase
  [combo]
  #_(map #(:terminals (get-in number-to-word-tree %)) combo)
  (map #(:terminals (get-in ezmonic.data/data %)) combo))


(defn split-number [number]
"Split a base 10 number into an list of digits."
(loop [number number
       result []]
  (if (= number 0)
    result
    (recur (quot number 10)
           (cons (rem number 10) result)))))

;; start with this
(defn all-phrases
  [number]
  (let [digits (split-number number)
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
