(ns flierplath.fi
  (:require [flierplath.util :refer [has-value select-map rm-matching-maps get-net-worth group-by-better]]
            #?(:clj [clj-time.core :as time]
               :cljs [cljs-time.core :as time])

            #?(:clj [clj-time.periodic :as pt]
               :cljs [cljs-time.periodic :as pt] )))

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


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; for daily calculation
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn surpluses->updated [vm l]
  (for [x l]
    (update (select-map vm :name (first (map key x))) :amount + (first (map val x)))))

(defn payments->newamounts [u]
  (let [w (map #(assoc % :newamount 0) u)
        nd (group-by :surplus w)]
    (filter #(not (= (:newamount %) 0)) (flatten (map vals (for [x u]
                                                             (let [p    (:payment x)
                                                                   dest (:surplus x)]
                                                               (update-in nd [dest 0 :newamount] + p))))))))

(defn ms-to-be-changed [vm date]
  (->> vm
       (group-by :cont-counter)
       (#(dissoc % :none))
       (#(dissoc % nil)) ;; hack.
       (filter #(time/before? (key %) date))
       vals
       flatten
       vec))

(defn update-days-surpluses [vm-orig vm]
  (->> vm
       payments->newamounts
       (group-by-better :surplus :newamount)
       (map (fn [[k v]] {k (reduce + v)}))
       (surpluses->updated vm-orig ,,,)))

(defn update-date-counters [vm]
  (map #(assoc % :cont-counter ((get % :cont-period) (get % :cont-counter))) vm))

(defn update-dates-if-before [vm date]
  (let [changed (update-date-counters (ms-to-be-changed vm date))
        unchanged (rm-matching-maps vm :name (map :name changed))]
    (vec (concat changed unchanged))))


(defn apply-interests-to-amounts [vm]
  (map (fn [m] (update m :amount #(* (+ 1 (/ (:i-rate m) 365)) %))) vm))

(defn advance-day [[vm date]]
  (let [changed (update-days-surpluses vm (ms-to-be-changed vm date))
        unchanged (rm-matching-maps vm :name (map :name changed))
        together (vec (concat changed unchanged))
        with-updated-dates (update-dates-if-before together date)
        with-interests (apply-interests-to-amounts with-updated-dates)]
    [with-interests (time/plus date (time/days 1))]))

(defn lazy-loop-of-days [[vm date]]
  (cons [vm date] (lazy-seq (lazy-loop-of-days (advance-day [vm date])))))

;; FIXME right now jan 31 + 2 months = march 28th. periodic-seq fixes this through .multipliedBy
(defn date-fixer [date date-func]
  (clojure.core/second (take 1 (pt/periodic-seq date date-func))))
