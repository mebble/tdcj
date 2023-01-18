(ns tdcj.views
  (:require
   [re-frame.core :as rf]
   [tdcj.subs :as subs]
   [tdcj.events :as events]))

(defn checkbox
  ([val on-click]
   [checkbox {} val on-click])
  ([attrs val on-click]
   [:input.shrink-0 (merge {:type "checkbox"
                   :default-checked val
                   :on-click on-click}
                  attrs)]))

(defn textbox
  ([val on-input]
   [textbox {} val on-input])
  ([attrs val on-input]
   [:input.border.border-neutral-400.rounded-sm.px-2.py-1
    (merge {:type "text" 
            :value val 
            :on-input #(-> % .-target .-value on-input)} 
           attrs)]))

(defn btn
  [& args]
  (apply vector :button.shrink-0.border.border-black.rounded.p-1.bg-gray-200 args))

(defn icon [src]
  [:img.w-4.h-4 {:src src}])

(defn todo-item [i todo]
  [:li.flex.justify-end.items-center.space-x-3.px-4.py-2
   {:data-id (:id todo)
    :data-is-done (:done todo)}
   [:div.txt.grow.overflow-x-auto {:class (when (:done todo) "line-through")}
    (if-not (:editing todo)
      [:span (:txt todo)]
      [textbox {:class "w-full" :data-input (:id todo)}
       (:txt todo) #(rf/dispatch [::events/change-todo i %])])]
   [checkbox {:data-edit (:id todo)} (:editing todo) #(rf/dispatch [::events/edit-todo i])]
   [btn {:data-delete (:id todo)
         :on-click #(rf/dispatch [::events/remove-todo i])}
    [icon "/icons/trash.svg"]]
   [checkbox {:data-done (:id todo)} (:done todo) #(rf/dispatch [::events/strike-todo i])]])

(defn new-todo []
  (let [new-todo-txt @(rf/subscribe [::subs/new-todo-txt])]
    [:form.flex.justify-between.space-x-2
     {:on-submit (fn [e]
                   (.preventDefault e) 
                   (rf/dispatch [::events/add-todo new-todo-txt]))}
     [textbox {:id :new-todo-txt :class "grow border-black"} new-todo-txt #(rf/dispatch [::events/edit-new-todo %])]
     [btn {:type "submit" :id :new-todo-btn} "Add Todo Item"]]))

(defn main-panel []
  [:div.w-fit.mx-auto.mt-20.px-6
   [:h1.text-4xl.text-center.mb-4
    [:span.font-black "Todo"]
    [:span.font-extralight "App"]]
   (if (zero? @(rf/subscribe [::subs/num-todos]))
     [:p.text-center.h-16.mb-4.flex.flex-col.justify-center "Add a todo below"]
     [:ul#todo-list.border.border-black.rounded-sm.divide-y.divide-black.mb-4
      ;; We can subscribe to each todo:
      ;; https://github.com/reagent-project/reagent/issues/18#issuecomment-51316043
      (doall (for [i (range @(rf/subscribe [::subs/num-todos]))]
               (let [t @(rf/subscribe [::subs/todo i])]
                 ^{:key (:id t)} [todo-item i t])))
    ;; Alternatively we can subscribe to all todos: 
      #_(for [t @(rf/subscribe [::subs/todos])]
          ^{:key (:id t)} [:li {:data-id (:id t)} (:txt t)])])
   [new-todo]])
