(ns tdcj.views
  (:require
   [re-frame.core :as re-frame]
   [tdcj.subs :as subs]
   [tdcj.events :as events]))

(defn main-panel []
  (let [name (re-frame/subscribe [::subs/name])
        count (re-frame/subscribe [::subs/count])]
    [:div
     [:h1
      "Hello frooo " @name " " @count]
     [:button {:on-click #(re-frame/dispatch [::events/update-count])} "Cycle count"]]))
