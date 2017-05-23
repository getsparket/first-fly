(ns flierplath.finance-test
  (:require [clojure.test :refer :all]
            [flierplath.finance :refer :all]))
;; show me what bad variable names i have
;; show other developers what this is supposed to do
;; permit me to refactor this namespace
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

(def all [{:name "foo" :payment 500 :amount 20000 :interest 0.07 :direction :bar :delete-on-empty false}])

(def special-months [{:name "scion XB" :amount -3000} 0 0 0 0 {:name "bank error in your favor" :amount 75}])

(deftest removes-paid-off-loans
  (testing "should blah"
    (let [loans (:loans bad)]
      (is (= 1 1 )))))

(deftest having-fun
  (testing "testing the test framework"
    (let [two 2]
      (is (= two 2)))))

(deftest when-retire?
  (testing "all the math is roughly right"
    (let [months-to-retiring (count (take-while #(< (get-net-worth (first %)) (* 25 (get-expenses (second %)))) (fi special-months [good bad])))]
      (is (= months-to-retiring 55)))))
