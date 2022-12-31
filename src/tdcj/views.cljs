(ns tdcj.views
  (:require
   [re-frame.core :as rf]
   [tdcj.subs :as subs]
   [tdcj.events :as events]))

(defn checkbox [val on-click]
  [:input {:type "checkbox"
           :default-checked val
           :on-click on-click}])

(defn textbox [val on-input]
  [:input.border {:type "text"
           :value val
           :on-input #(-> % .-target .-value on-input)}])

(defn btn
  [& args]
  (apply vector :button.p-1.border-2.bg-gray-300 args))

(defn icon [src]
  [:img.w-6.h-6 {:src src}])

(defn todo-item [i todo]
  [:li.flex.justify-center.items-center.space-x-3.mb-3 {:data-id (:id todo)}
   (if-not (:editing todo)
     [:span (:txt todo)]
     [textbox (:txt todo) #(rf/dispatch [::events/change-todo i %])])
   [checkbox (:editing todo) #(rf/dispatch [::events/edit-todo i])]
   [btn {:on-click #(rf/dispatch [::events/remove-todo i])}
    [icon "/icons/trash.svg"]]
   [checkbox (:done todo) #(rf/dispatch [::events/strike-todo i])]
   (when (:done todo) "DONE")])

(defn new-todo []
  (let [new-todo-txt @(rf/subscribe [::subs/new-todo-txt])]
    [:form.space-x-2 {:on-submit (fn [e]
                         (.preventDefault e)
                         (rf/dispatch [::events/add-todo new-todo-txt]))}
     [textbox new-todo-txt #(rf/dispatch [::events/edit-new-todo %])]
     [btn {:type "submit"} "Add Todo Item"]]))

(defn main-panel []
  [:div.w-fit.m-auto.mt-20
   [:h1.text-3xl.text-center.mb-4 "Todo App"]
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
