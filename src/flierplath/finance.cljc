(ns flierplath.finance)

(defn get-expenses [{:keys [loans consumables] :as b}]
  (reduce + (concat (map :payment loans) (map :payment consumables))))

(defn get-net-worth [{:keys [cash assets] :as g}]
  (reduce + (cons (:amount cash) (map :amount assets))))

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

(defn remove-paid-off [loans]
  (remove #(>= 0 (:amount %)) loans))

(defn advance-bad [{:keys [loans] :as bad}]
  (let [with-payments (map apply-payments loans)
        without-paid (remove-paid-off with-payments)
        n-loans (map adv without-paid)]
    (assoc bad :loans n-loans)))

(defn advance-a-month [[g b]]
  (let [surplus (calculate-surplus g b)
        n-good (advance-good g surplus)
        n-bad (advance-bad b)]
    [n-good n-bad]))

(defn apply-special-to-good [{:keys [cash] :as g} {:keys [amount] :or {amount 0} :as thing}]
  (let [n-cash (assoc cash :amount (+ (:amount cash) amount))]
    (assoc-in g [:cash] n-cash)))

(defn with-specials [s [g b]]
  (cons [g b] (lazy-seq (with-specials (rest s) (advance-a-month [(apply-special-to-good g (first s)) b])))))
