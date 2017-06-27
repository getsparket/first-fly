(ns flierplath.subs
  (:require [re-frame.core :refer [reg-sub]]
            [flierplath.fi :as fi]
            [cljs-time.core :as time]
            [cljs-time.coerce :as coerce]))

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
 :get-stuff
 (fn [db _]
   (:fin/stuff db)))

#_(reg-sub
 :compute-fi
 (fn [_ _] ;; https://github.com/Day8/re-frame/blob/master/docs/SubscriptionInfographic.md
   (subscribe [:get-stuff]))
 (fn [stuff _]
   (f/fi stuff)))

(reg-sub
 :list-liabs
 (fn [db _]
   (str (remove #(> (:fin.stuff/amount %) 0) (:fin/stuff db)))))

(reg-sub
 :list-assets
 (fn [db _]
   (str (remove #(< (:fin.stuff/amount %) 0) (:fin/stuff db)))))

(reg-sub
 :get-db-state
 (fn [db _]
   (str db)))

(reg-sub
 :get-greeting
 (fn [db _]
   "static android text"))

(reg-sub
 :get-time-to-default-fi
 (fn [db _]
   (let [vm (:fin/stuff db)
         _ (.log js/console vm)]
     (str (->> (fi/lazy-loop-of-days [vm (time/date-time 2017 6 15)])
               (take-while #(< (flierplath.util/get-net-worth %) 1000000))
               last
               second)))))
