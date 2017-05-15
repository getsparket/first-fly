(ns flierplath.finance)

;; can have either amount and interest XOR income
(def assets [{:name "cash" :amount 5000 :interest 0.07}
             {:name "house" :amount 100000 :interest 0.03}
             {:name "salary" :income 60000}
             ])

;; if we want "undo" and historical data, here's an obvious case:
(def liabs [{:name "car-loan" :amount 20000 :interest 0.07 :payment 500}
            {:name "school-loan" :amount 50000 :interest 0.04 :payment 1000}
            {:name "groceries" :payment 600}
            {:name "clothing" :payment 200}
            {:name "electric bill" :payment 100}
            ])

(defn calc-an-assets-income [{:keys [:name :amount :interest :income]}]
  (if (nil? income)
    (- (* (+ 1 (/ interest 12)) amount) amount)
    (/ income 12)))

(defn calc-months-income [a]
  (reduce + (map calc-an-assets-income a)))

(calc-months-income assets)

                                        ; when big is bigger than small
(take-while #(< 0 (:vla %)) [{:vla 1} {:vla 2} {:vla -1}])

(defn lazy-ass [{:keys [:asset :debt]}]
  (cons {:asset asset :debt debt} (lazy-seq (lazy-ass {:asset (+ asset asset) :debt debt}))))
(take-while #(> 500 (:asset %)) (lazy-ass {:asset 100 :debt 100}))
(lazy-ass {:asset 2 :debt 0})

(defn decrement-liabs [liabs]
  (let [ (map decrement-a-liab (filter (nil? ) liabs))])
  (keep-if-not-nil liabs-without-zeros))

;; don't *do* anything with consumable expenses. just add them up later.
(filter #(nil? (:amount %) ) liabs)

(defn decrement-a-liab [{:keys [:amount :payment] :as liab}]
  (assoc liab :amount (- amount payment)))

(decrement-a-liab (first liabs))

(defn advance-a-month [assets liabs]
  (let [recalculated-assets (compute assets)
        decremented-liabs (decrement-liabs liabs)
        ]
    (cons [assets liabs] (lazy-seq (advance-a-month recalculated-assets decremented-liabs)))
    ))
