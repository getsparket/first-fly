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
    (is (= -900 (f/get-months-costs finstuff))))
  (testing "monthly income"
    (is (= 14000/3 (f/get-months-income finstuff))))
  (testing "monthly loan payments"
    (is (= -1500 (f/get-months-loan-payments finstuff)))))
