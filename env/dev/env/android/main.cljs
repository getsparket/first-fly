 (ns ^:figwheel-no-load env.android.main
  (:require [reagent.core :as r]
            [flierplath.android.core :as core]
            [figwheel.client :as figwheel :include-macros true]))

 (enable-console-print!)

(def cnt (r/atom 0))
(defn reloader [] @cnt [core/app-root])
(def root-el (r/as-element [reloader]))

(figwheel/watch-and-reload
 ;; this url changes depending on the dev's use of avd, genymotion, real device etc.
 :websocket-url "ws://10.0.2.2:3449/figwheel-ws" ;; don't commit changes to this just because you're doing git add -u.
 :heads-up-display false
 :jsload-callback #(swap! cnt inc))

(core/init)
