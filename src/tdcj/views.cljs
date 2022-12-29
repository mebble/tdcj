(ns tdcj.views
  (:require
   [re-frame.core :as rf]
   [tdcj.subs :as subs]
   [tdcj.events :as events]))

(defn checkbox [val on-click]
  [:input {:type "checkbox"
           :value val
           :on-click on-click}])

(defn textbox [val on-input]
  [:input {:type "text"
           :value val
           :on-input #(-> % .-target .-value on-input)}])

(defn main-panel []
  [:div
   [:h1
    "Todo App"]
   [:ul

    ;; We can subscribe to each todo:
    ;; https://github.com/reagent-project/reagent/issues/18#issuecomment-51316043
    (doall (for [i (range @(rf/subscribe [::subs/num-todos]))]
             (let [t @(rf/subscribe [::subs/todo i])]
               ^{:key (:id t)} [:li {:data-id (:id t)}
                                (if-not (:editing t)
                                  (:txt t)
                                  [textbox (:txt t) #(rf/dispatch [::events/change-todo i %])])
                                [checkbox (:editing t) #(rf/dispatch [::events/edit-todo i])] 
                                [:button {:on-click #(rf/dispatch [::events/remove-todo i])} "🗑️"]
                                [checkbox (:done t) #(rf/dispatch [::events/strike-todo i])]
                                (when (:done t) "DONE")])))

    ;; Alternatively we can subscribe to all todos:
    #_(for [t @(rf/subscribe [::subs/todos])]
        ^{:key (:id t)} [:li {:data-id (:id t)} (:txt t)])]
   (let [new-todo-txt @(rf/subscribe [::subs/new-todo-txt])]
     [:form {:on-submit (fn [e]
                          (.preventDefault e)
                          (rf/dispatch [::events/add-todo new-todo-txt]))}
      [textbox new-todo-txt #(rf/dispatch [::events/edit-new-todo %])]
      [:button {:type "submit"} "Add Todo Item"]])])
