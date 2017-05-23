(ns my-project.tests
  (:require [cljs.test :refer-macros [deftest is testing run-tests]]
            [flierplath.db :as d]
            ;; [flierplath.finance :as f]
            [flierplath.twofinance :as f]))

(enable-console-print!)

(deftest having-fun
  (testing "testing the test framework"
    (let [two 2]
      #_(print d/app-db)
      (is (= two 2)))))

(deftest wrong-fun
  (testing "testing the test framework"
    (let [two 2]
      #_(print d/app-db)
      (is (= two 2)))))

(deftest can-advance-interest-rates
  (let [amounts '(-20116.666666666668 -50166.66666666667 5029.166666666667 0 100583.33333333333 0 0 0)]
    (is  (= amounts (map :fin.stuff/amount (map f/apply-interest-rates (:fin/stuff d/app-db)))))))

(cljs.test/run-tests)
;; get surplus
;; apply payments
;; put surplus in cash
;;
;; apply interest rates first because it's easy
;; get surplus by subtracting incomes from expenses
;; apply surplus to 

;; (deftest ) (map :fin.stuff/amount (:fin/stuff d/app-db))


