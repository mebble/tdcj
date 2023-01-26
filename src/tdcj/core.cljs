(ns tdcj.core
  (:require
   [reagent.dom :as rdom]
   [re-frame.core :as rf]
   [tdcj.init] ;; init the handlers, effects and subscriptions
   [tdcj.ids :as i]
   [tdcj.views :as views]
   [tdcj.config :as config]))

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (rf/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/main-panel] root-el)))

(defn init []
  (rf/dispatch-sync [::i/initialize-db])
  (dev-setup)
  (mount-root))
