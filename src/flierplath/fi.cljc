(ns flierplath.fi)

(defn get-expenses [{:keys [loans consumables] :as b}]
  (reduce + (concat (map :payment loans) (map :payment consumables))))

(defn get-net-worth [{:keys [cash assets] :as g}]
  (reduce + (cons (:amount cash) (map :amount assets))))

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


(defn add-up-vals [k data]
  (make-monthly (reduce + (remove nil? (map k data)))))

(defn make-monthly [val]
  (/ val 12))

(defn get-months-costs [finstuff]
  (add-up-vals :cost finstuff))

(defn get-months-loan-payments [finstuff]
  (- (add-up-vals :paying-off finstuff)))

(defn get-months-income [finstuff]
  (add-up-vals :payment finstuff))

(defn get-surplus [finstuff]
  (let [expenses (+ (get-months-costs finstuff) (get-months-loan-payments finstuff))
        income (get-months-income finstuff)]
    (+ income expenses))) ;; have to add b/c expenses are negative.

;; http://benashford.github.io/blog/2014/12/27/group-by-and-transducers/
;; https://stackoverflow.com/a/25481057
#_(reduce (fn [aggr {:keys [name] :as row}]
          (update-in aggr
                     [name]
                     (fnil conj [])
                     (dissoc row :name)))
        {} #_exmpale-data)


