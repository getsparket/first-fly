(ns flierplath.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
  :nav/tab-state
  (fn [db _]
    (:nav/tab-state db)))

(reg-sub
  :nav/stack-state
  (fn [db [_ route-name]]
    (get-in db [:nav/stack-state (keyword "nav.routeName" route-name)])))


(reg-sub
 :fin.stuff/asset
 (fn [db _]
   (get-in db [:fin/stuff :fin.stuff/asset])))

(reg-sub
 :get-db-state
 (fn [db _]
   (str db #_(get-in db [:fin/stuff]))))

(reg-sub
 :list-assets
 (fn [db _]
   (str (:fin/stuff db))))

(reg-sub
 :get-greeting
 (fn [db _]
   "static android text"))
