(ns flierplath.fi)



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

(defn calc-monthly-val [k data]
  (make-monthly (reduce + (remove nil? (map k data)))))

(defn make-monthly [val]
  (/ val 12))

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

