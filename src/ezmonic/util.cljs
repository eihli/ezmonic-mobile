(ns ezmonic.util
  (:require [clojure.math.combinatorics :as combo]
            [clojure.set]
            [clojure.string :as string]
            [ezmonic.data]))


(defn connections
  [length]
  (combo/selections [0 1] length))


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


(defn split-number
  [number]
  "Split a base 10 number into an list of digits."
  (loop [number number
         result []]
    (if (= number 0)
      result
      (recur (quot number 10)
             (cons (rem number 10) result)))))


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
