(ns flierplath.util)

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

(defn vec-map->map [b k v]
  (let [by-key (group-by k b)]
    (nth (get by-key v) 0)))

(defn rm-matching-maps
  "given a vector of maps, a key and a seq of values, remove matching maps"
  [vm k seq-of-values]
  (remove #(.contains seq-of-values (get % k) ) vm)) ;; remove elements of the list that have both key and value
