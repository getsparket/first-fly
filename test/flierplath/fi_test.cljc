(ns flierplath.fi-test
  (:require [clojure.test :refer :all]
            [flierplath.util :refer :all]
            [clj-time.core :as time]
            [clj-time.local :as local]
            [flierplath.db :as db]
            [flierplath.fi :refer :all]))

(def surp
  [{:name "car-loan" :amount -20000 :i-rate 0.07 :paying-off 6000 :delete-if-empty true :surplus nil} ;; counterintuitive: a loan is defined by
   {:name "school-loan" :amount -50000 :i-rate 0.04 :paying-off 12000 :delete-if-empty true :surplus nil} ;; a negative balance
   {:name "cash" :amount 5000 :i-rate 0.07 :payment 50000 :delete-if-empty false :surplus "cash"}
   {:name "rental-income" :amount 0 :i-rate 0.07 :payment 6000 :delete-if-empty false :surplus "cash"}
   {:name "vanguard" :amount 1000 :i-rate 0.07 :payment 6000 :delete-if-empty false :surplus "vanguard"}
   {:name "house" :amount 100000 :i-rate 0.07 :payment 0 :delete-if-empty false :surplus "house"}
   {:name "groceries" :amount 0 :i-rate 0 :cost -7200 :delete-if-empty false :surplus nil} ;; counterintuitive: should payment be pos or neg?
   {:name "clothing" :amount 0 :i-rate 0 :cost -2400 :delete-if-empty false :surplus nil}
   {:name "electric bil" :amount 0 :i-rate 0 :cost -1200 :delete-if-empty false :surplus nil}])

(def finstuff (:fin/stuff db/app-db))

(deftest stub-of-dates
  (is (not (nil? (time/plus (local/local-now) (time/months 1))))))

(deftest can-do-existing-shit
  (testing "monthly costs"
    (is (= -900 (months-costs finstuff))))
  (testing "monthly income"
    (is (= 14000/3 (months-income finstuff))))
  (testing "monthly loan payments"
    (is (= -1500 (months-loan-payments finstuff))))
  (testing "net worth"
    (is (= 35000 (net-worth finstuff))))
  (testing "surplus?"
    (is (= 8300/3 (surplus surp)))))

(deftest can-test-surplus-applying-no-expenses
  (testing "can redirect some money made"
    (let [no-expenses [{:name "cash" :amount 5000 :i-rate 0.07 :payment 50000 :delete-if-empty false :surplus "cash"}
                       {:name "rental-income" :amount 0 :i-rate 0.07 :payment 6000 :delete-if-empty false :surplus "cash"}
                       {:name "vanguard" :amount 1000 :i-rate 0.07 :payment 6000 :delete-if-empty false :surplus "vanguard"}
                       {:name "house" :amount 100000 :i-rate 0.07 :payment 0 :delete-if-empty false :surplus "house"}]
          just-added (->> no-expenses
                          payments->newamounts
                          (group-by-better :name :newamount)
                          (map (fn [[k v]] {k (reduce + v)})))
          works? (surpluses->updated no-expenses just-added)
          after-a-month [{:name "cash" :amount 29000/3 :i-rate 0.07 :payment 50000 :delete-if-empty false :surplus "cash"}
                         {:name "rental-income" :amount 0 :i-rate 0.07 :payment 6000 :delete-if-empty false :surplus "cash"}
                         {:name "vanguard" :amount 1500 :i-rate 0.07 :payment 6000 :delete-if-empty false :surplus "vanguard"}
                         {:name "house" :amount 100000 :i-rate 0.07 :payment 0 :delete-if-empty false :surplus "house"}]]
      (is (= (into #{} works?) (into #{} after-a-month)))
      (is (= (into #{} (update-months-surpluses no-expenses)) (into #{} after-a-month))))
    (testing "utility function can do what it says"
      (let [all [{:name "cash" :amount 5000 :i-rate 0.07 :payment 50000 :delete-if-empty false :surplus "cash"}
                 {:name "rental-income" :amount 0 :i-rate 0.07 :payment 6000 :delete-if-empty false :surplus "cash"}
                 {:name "vanguard" :amount 1000 :i-rate 0.07 :payment 6000 :delete-if-empty false :surplus "vanguard"}
                 {:name "house" :amount 100000 :i-rate 0.07 :payment 0 :delete-if-empty false :surplus "house"}]
            pass [{:name "cash" :amount 5000 :i-rate 0.07 :payment 50000 :delete-if-empty false :surplus "cash"}
                  {:name "house" :amount 100000 :i-rate 0.07 :payment 0 :delete-if-empty false :surplus "house"}]]
        (is (= (rm-matching-maps all :name ["rental-income" "vanguard"]) pass))))))

(deftest can-test-surpluses-with-dates
  (testing "the day is now 6/13. what should happen?"
    (let [monthly  (fn [date] (time/plus date (time/months 1)))

          with-times  [{:name "cash" :amount 5000 :i-rate 0.07 :payment 5000 :delete-if-empty false :surplus "cash"
                        :cont-period monthly :start (time/date-time 2017 6 1) :cont-counter (time/date-time 2017 6 13)}
                       {:name "rental-income" :amount 0 :i-rate 0.07 :payment 500 :delete-if-empty false :surplus "cash"
                        :cont-period monthly :start (time/date-time 2017 6 1) :cont-counter (time/date-time 2017 6 15)}
                       {:name "vanguard" :amount 1000 :i-rate 0.07 :payment 500 :delete-if-empty false :surplus "vanguard"
                        :cont-period monthly :start (time/date-time 2017 6 1) :cont-counter (time/date-time 2017 6 13)}
                       {:name "house" :amount 100000 :i-rate 0.07 :payment 0 :delete-if-empty false :surplus "house"
                        :cont-period :none  :start (time/date-time 2017 6 1) :cont-counter :none}]
          after-a-day [{:name "cash" :amount 16250/3 :i-rate 0.07 :payment 5000 :delete-if-empty false :surplus "cash"
                        :cont-period monthly :start (time/date-time 2017 6 1) :cont-counter (time/plus (time/date-time 2017 6 13) (time/months 1))}
                       {:name "rental-income" :amount 0 :i-rate 0.07 :payment 500 :delete-if-empty false :surplus "cash"
                        :cont-period monthly :start (time/date-time 2017 6 1) :cont-counter (time/date-time 2017 6 15)}
                       {:name "vanguard" :amount 1000 :i-rate 0.07 :payment 500 :delete-if-empty false :surplus "vanguard"
                        :cont-period monthly :start (time/date-time 2017 6 1) :cont-counter (time/plus (time/date-time 2017 6 13) (time/days 1))}
                       {:name "house" :amount 100000 :i-rate 0.07 :payment 0 :delete-if-empty false :surplus "house"
                        :cont-period :none :start (time/date-time 2017 6 1) :cont-counter :none}]]
      (is (= (into #{} after-a-day) (into #{} (advance-day with-times (time/date-time 2017 6 14))))))))
;; :cont-period :once for one-time events. cont-counter: magic value. start: the start date.

(deftest npe
  (testing "throws an NPE. shouldn't."
    (let [data [{:name "cash" :amount 5000 :i-rate 0.07 :payment 5000 :delete-if-empty false :surplus "cash"
                 :cont-period (fn [date] (time/plus date (time/months 1))) :start (time/date-time 2017 6 1) :cont-counter (time/date-time 2017 6 13)}
                {:name "rental-income" :amount 0 :i-rate 0.07 :payment 500 :delete-if-empty false :surplus "cash"
                 :cont-period (fn [date] (time/plus date (time/months 1))):start (time/date-time 2017 6 1) :cont-counter (time/date-time 2017 6 15)}]]
      (is (= "cheese" (try (take 3 (flierplath.fi/lazy-loop-of-days [data (time/date-time 2017 06 15)]))
                            (catch NullPointerException e (str "fuck"))))))))
