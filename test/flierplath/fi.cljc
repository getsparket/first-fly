(ns flierplath.fi-test
  (:require [clojure.test :refer :all]
            [clj-time.core :as time]
            [clj-time.local :as local]
            [flierplath.db :as db]
            ;; [flierplath.finance :as f]
            [flierplath.fi :as f]))

(def finstuff (:fin/stuff db/app-db))

(deftest stub-of-dates
  (is (not (nil? (time/plus (local/local-now) (time/months 1))))))

(deftest can-do-existing-shit
  (testing "monthly costs"
    (is (= -900 (f/months-costs finstuff))))
  (testing "monthly income"
    (is (= 14000/3 (f/months-income finstuff))))
  (testing "monthly loan payments"
    (is (= -1500 (f/months-loan-payments finstuff))))
  (testing "net worth"
    (is (= 35000 (f/net-worth finstuff))))
  (testing "surplus?"
    (is (= 8300/3 (f/surplus surp)))))

(deftest can-move-payments-around
  (testing "no expenses, cash and rental move to cash."
    (let [f  [{:name "cash" :amount 5000 :payment 50000 :surplus "cash"}
                    {:name "rental-income" :amount 0 :payment 6000 :surplus "cash"}
                    {:name "vanguard" :amount 1000 :payment 6000 :surplus "vanguard"}
              {:name "house" :amount 100000 :payment 0 :surplus "house"}]
          done [{:name "cash", :amount 5000, :payment 50000, :surplus "cash", :newamount 12500/3} {:name "cash", :amount 5000, :payment 50000, :surplus "cash", :newamount 500} {:name "vanguard", :amount 1000, :payment 6000, :surplus "vanguard", :newamount 500} {:name "house", :amount 100000, :payment 0, :surplus "house", :newamount 0}]]
      (is (= done (payments->newamounts f)))))
  (testing ""))

(deftest can-test-surplus-application
  (testing "can redirect some money made"
    (let [no-expenses [{:name "cash" :amount 5000 :i-rate 0.07 :payment 50000 :delete-if-empty false :surplus "cash"}
                       {:name "rental-income" :amount 0 :i-rate 0.07 :payment 6000 :delete-if-empty false :surplus "cash"}
                       {:name "vanguard" :amount 1000 :i-rate 0.07 :payment 6000 :delete-if-empty false :surplus "vanguard"}
                       {:name "house" :amount 100000 :i-rate 0.07 :payment 0 :delete-if-empty false :surplus "house"}]
          after-a-month [{:name "cash" :amount 29000/3 :i-rate 0.07 :payment 50000 :delete-if-empty false :surplus "cash"} 
                         {:name "rental-income" :amount 0 :i-rate 0.07 :payment 6000 :delete-if-empty false :surplus "cash"}
                         {:name "vanguard" :amount 1500 :i-rate 0.07 :payment 6000 :delete-if-empty false :surplus "vanguard"}
                         {:name "house" :amount 100000 :i-rate 0.07 :payment 0 :delete-if-empty false :surplus "house"}]])
    #_(is (= #_(do-thing no-expenses) after-a-month))))

(defn payments->newamounts [u]
    (let [kd (group-by :surplus u)
          nd (group-by :name u)
          with-new-amounts (filter #(contains? % :newamount) (flatten (map vals (for [x u]
                                                                 (let [p    (/ (:payment x) 12)
                                                                       dest (:surplus x)]
                                                                   (assoc-in nd [dest 0 :newamount] p))))))
          ]
      with-new-amounts))

(defn update-map [{:keys [amount payment surplus-amount] :as bals} expenses]

  (merge bals {:amount (+ amount surplus-amount)
           :payment payment
           }))

(defn update-one-time [m expenses]
  [(update-map m expenses) (- expenses 1000)])

(defn default-map [f]
  (let [default (map #(assoc % :surplus-amount ((fnil / 0) (:payment %) 12)) f)]
    (reverse (sort-by :surplus-amount default))))
