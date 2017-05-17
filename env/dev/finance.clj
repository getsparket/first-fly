(ns flierplath.finance)

(def good {:incomes [{:name "rental-income" :income 6000}
                     {:name "salary" :income 50000}]
           :cash {:name "vanguard" :amount 5000 :interest 0.07}
           :assets [{:name "house" :amount 100000 :interest 0.07}
                    {:name "rental-house" :amount 50000 :interest 0.07}]})

(def bad {:loans [{:name "car-loan" :amount 20000 :interest 0.07 :payment 6000}
                  {:name "school-loan" :amount 50000 :interest 0.04 :payment 12000}]
          :consumables [{:name "groceries" :payment 7200}
                        {:name "clothing" :payment 2400}
                        {:name "electric bill" :payment 1200}]})

(defn calculate-surplus [{:keys [incomes cash] :as g} {:keys [loans consumables] :as b}]
  (let [monthly-income (/ (reduce + (map :income incomes)) 12)
        monthly-expenses (/ (reduce + (concat (map :payment loans) (map :payment consumables))) 12)] ;; TODO calculate cash income
    (- monthly-income monthly-expenses)))

(defn adv [{:keys [amount interest] :as m}]
  (assoc m :amount (* amount (+ 1 (/ interest 12)))))

(defn advance-good [{:keys [cash assets] :as good} surplus]
  (let [n-assets (map adv assets)
        n-cash (adv (assoc cash :amount (+ surplus (:amount cash))))]
    (assoc good :assets n-assets :cash n-cash)))

(defn apply-payments [{:keys [amount payment] :as loan}]
  (let [monthly-payment (/ payment 12)]
    (assoc loan :amount (- amount monthly-payment))))

(defn advance-bad [{:keys [loans] :as bad}]
  (let [with-payments (map apply-payments loans)
        n-loans (map adv with-payments)]
    (assoc bad :loans n-loans)))

(defn advance-a-month [g b]
  (let [surplus (calculate-surplus g b)
        n-good (advance-good g surplus)
        n-bad (advance-bad b)]
    (cons [g b] (lazy-seq (advance-a-month n-good n-bad)))))
