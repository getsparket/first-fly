 (ns ^:figwheel-no-load env.ios.main
  (:require [reagent.core :as r]
            [flierplath.ios.core :as core]
            [devtools.core :as devtools]
            [figwheel.client :as figwheel :include-macros true]
            ))

 (devtools/install!)
 (enable-console-print!)

(def cnt (r/atom 0))
(defn reloader [] @cnt [core/start])
(def root-el (r/as-element [reloader]))

(figwheel/watch-and-reload
 :websocket-url "ws://localhost:3449/figwheel-ws"
 :heads-up-display false
 :jsload-callback #(swap! cnt inc))

(core/init)
