(ns flierplath.util
  (require [clj-time.core :as time]))

(defn group-by-better [key-f val-f data]
  (reduce (fn [m d]
            (let [k (key-f d)
                  v (get m k [])]
              (assoc m k (conj v (val-f d)))))
          {}
          data))

(defn has-value [key value]
  "Returns a predicate that tests whether a map contains a specific value"
  (fn [m]
    (= value (m key))))

(defn select-map
  "given a vector of maps, a key, and a value, return a map"
  [vm k v]
  (let [by-key (group-by k vm)]
    (nth (get by-key v) 0)))

(defn select-maps
  "given a vector of maps, a key, and a value, return a list of maps"
  [vm k v]
  (let [by-key (group-by k vm)]
    (get by-key v)))

(defn vm->ms-from-pred [vm pred k]
  (let [by-key (group-by k vm)]
    (conj (map pred by-key))))


(defn rm-matching-maps
  "given a vector of maps, a key and a seq of values, remove matching maps"
  [vm k seq-of-values]
  (remove #(.contains seq-of-values (get % k)) vm)) ;; remove elements of the list that have both key and value

(defn keep-matching-maps
  "given a vector of maps, a key and a seq of values, remove matching maps"
  [vm k seq-of-values]
  (filter #(.contains seq-of-values (get % k)) vm)) ;; remove elements of the list that have both key and value
