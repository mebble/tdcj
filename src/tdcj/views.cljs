(ns tdcj.views
  (:require
   [re-frame.core :as rf]
   [tdcj.subs :as subs]
   [tdcj.events :as events]))

(defn main-panel []
  [:div
   [:h1
    "Todo App"]
   [:ul (for [t @(rf/subscribe [::subs/todos])]
          ^{:key (:id t)} [:li {:data-id (:id t)} (:txt t)])]
    [:button {:on-click #(rf/dispatch [::events/add-todo (rand-nth ["one" "two" "three"])])} "Add Todo Item"]])
