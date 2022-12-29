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

(defn todo-item [i todo]
  [:li {:data-id (:id todo)}
   (if-not (:editing todo)
     (:txt todo)
     [textbox (:txt todo) #(rf/dispatch [::events/change-todo i %])])
   [checkbox (:editing todo) #(rf/dispatch [::events/edit-todo i])]
   [:button {:on-click #(rf/dispatch [::events/remove-todo i])} "🗑️"]
   [checkbox (:done todo) #(rf/dispatch [::events/strike-todo i])]
   (when (:done todo) "DONE")])

(defn new-todo []
  (let [new-todo-txt @(rf/subscribe [::subs/new-todo-txt])]
    [:form {:on-submit (fn [e]
                         (.preventDefault e)
                         (rf/dispatch [::events/add-todo new-todo-txt]))}
     [textbox new-todo-txt #(rf/dispatch [::events/edit-new-todo %])]
     [:button {:type "submit"} "Add Todo Item"]]))

(defn main-panel []
  [:div
   [:h1
    "Todo App"]
   [:ul

    ;; We can subscribe to each todo:
    ;; https://github.com/reagent-project/reagent/issues/18#issuecomment-51316043
    (doall (for [i (range @(rf/subscribe [::subs/num-todos]))]
             (let [t @(rf/subscribe [::subs/todo i])]
               ^{:key (:id t)} [todo-item i t])))

    ;; Alternatively we can subscribe to all todos:
    #_(for [t @(rf/subscribe [::subs/todos])]
        ^{:key (:id t)} [:li {:data-id (:id t)} (:txt t)])]
   [new-todo]])
