(ns flierplath.db
  (:require #?(:clj [clojure.spec :as s]
               :cljs [cljs.spec.alpha :as s])))

;; spec of app-db
;; Fetched from: https://github.com/react-community/react-navigation/blob/c37ad8a0a924d13f3897bc72fbda52aac76904b6/src/TypeDefinition.js

(s/def :nav.route/key keyword?)
(s/def :nav.route/routeName keyword?)
(s/def :nav.route/path keyword?)
(s/def :nav.route/param (s/or :str string? :num number?))
(s/def :nav.route/params (s/map-of keyword? :nav.route/param))
(s/def :nav/route (s/keys :req [:nav.route/key :nav.route/routeName]
                          :opt [:nav.route/path :nav.route/params]))
(s/def :nav.state/routes (s/coll-of :nav/route :kind vector?))
(s/def :nav.state/index integer?)
(s/def :nav/tab-state (s/keys :req [:nav.state/index :nav.state/routes]))
(s/def :matt/matt string?)
(s/def :fin.stuff/amount number?)
(s/def :fin.stuff/i-rate number?)
(s/def :fin.stuff/cost number?)
(s/def :fin.stuff/paying-off number?)
(s/def :fin.stuff/payment integer?)
#_(s/def :fin.stuff/item (s/keys :req [ :fin.stuff/name :fin.stuff/i-rate :fin.stuff/payment]))
(s/def :fin/stuff (s/coll-of map?))

(s/def ::app-db
  (s/keys :req [:nav/tab-state]))

;; initial state of app-db
(def app-db {:nav/tab-state   #:nav.state{:index  0
                                          :routes [#:nav.route{:key :IndexKey :routeName :Index}
                                                   #:nav.route{:key :SettingsKey :routeName :Settings}]}
             :nav/stack-state #:nav.routeName {:Index #:nav.state {:index  0
                                                                   :routes [#:nav.route {:key :Home :routeName :Home}]}}
             :fin/stuff
             [{:name "car-loan" :amount -20000 :i-rate 0.07 :paying-off 6000 :delete-if-empty true :surplus nil} ;; counterintuitive: a loan is defined by
              {:name "school-loan" :amount -50000 :i-rate 0.04 :paying-off 12000 :delete-if-empty true :surplus nil} ;; a negative balance
              {:name "cash" :amount 5000 :i-rate 0.07 :payment 50000 :delete-if-empty false :surplus "cash"}
              {:name "rental-income" :amount 0 :i-rate 0.07 :payment 6000 :delete-if-empty false :surplus "cash"}
              {:name "house" :amount 100000 :i-rate 0.07 :payment 0 :delete-if-empty false :surplus "cash"}
              {:name "groceries" :amount 0 :i-rate 0 :cost -7200 :delete-if-empty false :surplus nil} ;; counterintuitive: should payment be pos or neg?
              {:name "clothing" :amount 0 :i-rate 0 :cost -2400 :delete-if-empty false :surplus nil}
              {:name "electric bil" :amount 0 :i-rate 0 :cost -1200 :delete-if-empty false :surplus nil}]})

