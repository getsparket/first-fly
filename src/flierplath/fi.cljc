(ns flierplath.fi
  (:require [flierplath.util :refer :all]
            [clj-time.core :as time]))

(defn calculate-surplus [{:keys [incomes cash] :as g} {:keys [loans consumables] :as b}]
  (let [monthly-income (/ (reduce + (map :income incomes)) 12)
        monthly-expenses (/ (reduce + (concat (map :payment loans) (map :payment consumables))) 12)] ;; TODO calculate cash income
    (- monthly-income monthly-expenses)))

(defn apply-interest-rates [{:keys [:fin.stuff/amount :fin.stuff/i-rate]:as m}]
  (assoc m :fin.stuff/amount (* amount (+ 1 (/ i-rate 12)))))

(defn advance-good [{:keys [cash assets] :as good} surplus]
  (let [n-assets (map apply-interest-rates assets)
        n-cash (apply-interest-rates (assoc cash :amount (+ surplus (:amount cash))))]
    (assoc good :assets n-assets :cash n-cash)))

(defn apply-payments [{:keys [amount payment] :as loan}]
  (let [monthly-payment (/ payment 12)]
    (assoc loan :amount (- amount monthly-payment))))

(defn remove-paid-off [loans]
  (remove #(>= 0 (:amount %)) loans))

(defn advance-bad [{:keys [loans] :as bad}]
  (let [with-payments (map apply-payments loans)
        without-paid (remove-paid-off with-payments)
        n-loans (map apply-interest-rates without-paid)]
    (assoc bad :loans n-loans)))

;;; accessors

(defn make-monthly [val]
  (/ val 12))

(defn calc-monthly-val [k data]
  (make-monthly (reduce + (remove nil? (map k data)))))

(defn months-costs [finstuff]
  (calc-monthly-val :cost finstuff))

(defn months-loan-payments [finstuff]
  (- (calc-monthly-val :paying-off finstuff)))

(defn months-income [finstuff]
  (calc-monthly-val :payment finstuff))

(defn net-worth [finstuff]
  (reduce + (map :amount finstuff)))

(defn surplus [finstuff]
  (let [expenses (+ (months-costs finstuff) (months-loan-payments finstuff))
        income (months-income finstuff)]
    (+ income expenses))) ;; have to add b/c expenses are negative.

(defn surpluses->updated [vm l]
  (let [updated-ones (for [x l]
                       (update (select-map vm :name (first (map key x))) :amount + (first (map val x))))
        non-updated-ones (rm-matching-maps vm :name (keys (group-by :name updated-ones)))]
    (concat updated-ones non-updated-ones)))



(defn payments->newamounts [u]
  (let [nd (group-by :name u)
        with-new-amounts (filter #(contains? % :newamount) (flatten (map vals (for [x u]
                                                                                (let [p    (/ (:payment x) 12)
                                                                                      dest (:surplus x)]
                                                                                  (assoc-in nd [dest 0 :newamount] p))))))
        ]
    with-new-amounts))

(defn payments->surpluses [u]
  (let [with-new-amounts (payments->newamounts u)
        added-up (->> with-new-amounts
                      (group-by-better :name :newamount)
                      (map (fn [[k v]] {k (reduce + v)})))]))

(defn newamounts->surpluses [u]
  (let [list-of-surpluses (map #(->> u
                                     (group-by :name)
                                     ((fn [x] (get x %)))
                                     (map :newamount)
                                     (reduce +)) (vec (map :name u)))

        added (map #(update % :amount + (:newamount %)) u)
        dissocd (map #(dissoc % :newamount) added)]
    (interleave (map :name u) list-of-surpluses)))

(defn update-months-surpluses [vm]
  (->> vm
       payments->newamounts
       (group-by-better :name :newamount)
       (map (fn [[k v]] {k (reduce + v)}))
       (surpluses->updated vm)))

(defn update-date-counters [vm]
  (map #(assoc % :cont-counter ((get % :cont-period) (get % :cont-counter))) vm))

(defn ms-to-be-changed [vm date]
  (into [] (flatten (vals (into {} (filter #(time/before? (if (= :none (key %)) (time/date-time 9999) (key %)) date) (dissoc (group-by :cont-counter vm) nil))))))) 

(defn ms-to-stay-the-same [vm date]
  (into [] (flatten (vals (into {} (filter (complement #(time/before? (key %) date)) (clojure.set/rename-keys (group-by :cont-counter vm) {nil (time/date-time 2018)}))))))) ;; don't forget nils. 2018 is a magic number.

(defn advance-day [vm date]
  (let [changed (update-date-counters (update-months-surpluses (ms-to-be-changed vm date)))
        unchanged (rm-matching-maps vm :name (map :name (vec (update-months-surpluses (ms-to-be-changed vm date)))))]
    (concat changed unchanged)))


