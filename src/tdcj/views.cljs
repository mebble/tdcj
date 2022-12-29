(ns tdcj.views
  (:require
   [re-frame.core :as rf]
   [tdcj.subs :as subs]
   [tdcj.events :as events]))

(defn main-panel []
  [:div
   [:h1
    "Todo App"]
   [:ul

    ;; We can subscribe to each todo:
    (doall (for [i (range @(rf/subscribe [::subs/num-todos]))] 
             (let [t @(rf/subscribe [::subs/todo i])] 
               ^{:key (:id t)} [:li {:data-id (:id t)} 
                                (:txt t) 
                                [:button {:on-click #(rf/dispatch [::events/remove-todo i])} "🗑️"]
                                [:input {:type "checkbox"
                                         :value (:done t)
                                         :on-click #(rf/dispatch [::events/strike-todo i])}]
                                (when (:done t) "DONE")])))
    
    ;; Alternatively we can subscribe to all todos:
    #_(for [t @(rf/subscribe [::subs/todos])] 
        ^{:key (:id t)} [:li {:data-id (:id t)} (:txt t)])]

    [:button {:on-click #(rf/dispatch [::events/add-todo (rand-nth ["one" "two" "three"])])} "Add Todo Item"]])
