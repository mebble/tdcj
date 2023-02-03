(ns tdcj.views
  (:require
   [re-frame.core :as rf]
   [tdcj.ids :as i]))

(defn- get-val [ref]
  (when-let [elem @ref]
    (.-value elem)))

(defn- set-val [ref val]
  (when-let [elem @ref]
    (set! (.-value elem) val)))

(defn checkbox
  ([val on-click]
   [checkbox {} val on-click])
  ([attrs val on-click]
   [:input.shrink-0
    (merge {:type "checkbox" 
            :checked val
            :on-change on-click} 
           attrs)]))

(defn textbox
  ([val on-input]
   [textbox {} val on-input])
  ([attrs val on-input]
   [textbox attrs val on-input (atom nil)])
  ([attrs val on-input ref]
   [:input.border.border-neutral-400.rounded-sm.px-2.py-1
    (merge {:type "text"
            :default-value val
            :ref (fn [el] (reset! ref el) (set-val ref val))
            :on-input #(-> % .-target .-value on-input)}
           attrs)]))

(defn btn
  [& args]
  (apply vector :button.shrink-0.border.border-black.rounded.p-1.bg-gray-200.disabled:bg-gray-100.disabled:text-gray-400.disabled:border-gray-400 args))

(defn icon [icon-file]
  [:img.w-4.h-4 {:src (str "/icons/" icon-file)}])

(defn todo-item [i todo]
  (let [todo-ref (atom nil)]
    [:li.flex.justify-end.items-center.space-x-3.px-4.py-2
     {:data-id (:id todo)
      :data-is-done (:done todo)}
     [:div.txt.grow.overflow-x-auto {:class (when (:done todo) "line-through")}
      (if-not (:editing todo)
        [:span (:txt todo)]
        [:form
         {:on-submit (fn [e]
                       (.preventDefault e)
                       (rf/dispatch [::i/edit-todo i]))}
         [textbox
          {:class "w-full" :data-input (:id todo)}
          (:txt todo)
          #(rf/dispatch [::i/change-todo i (get-val todo-ref)])
          todo-ref]])]
     [checkbox {:data-edit (:id todo)} (:editing todo) #(rf/dispatch [::i/edit-todo i])]
     [btn {:data-delete (:id todo)
           :on-click #(rf/dispatch [::i/remove-todo i])}
      [icon "trash.svg"]]
     [checkbox {:data-done (:id todo)} (:done todo) #(rf/dispatch [::i/strike-todo i])]]))

(defn new-todo []
  (let [new-todo-txt @(rf/subscribe [::i/new-todo-txt])
        new-todo-ref (atom nil)]
    [:form.flex.justify-between.space-x-2.mb-4
     {:on-submit (fn [e]
                   (.preventDefault e) 
                   (rf/dispatch [::i/add-todo (get-val new-todo-ref)]))}
     [textbox
      {:id :new-todo-txt :class "grow border-black"} 
      new-todo-txt
      #(rf/dispatch [::i/edit-new-todo (get-val new-todo-ref)])
      new-todo-ref]
     [btn {:type "submit" :id :new-todo-btn} "Add Todo"]]))

(defn main-panel []
  [:div.w-fit.mx-auto.mt-20.px-6
   [:h1.text-4xl.text-center.mb-4
    [:span.font-black "Todo"]
    [:span.font-extralight "App"]]
   (if (zero? @(rf/subscribe [::i/num-todos]))
     [:p.text-center.h-16.mb-4.flex.flex-col.justify-center "Add a todo below"]
     [:ul#todo-list.border.border-black.rounded-sm.divide-y.divide-black.mb-4
      ;; We can subscribe to each todo:
      ;; https://github.com/reagent-project/reagent/issues/18#issuecomment-51316043
      (doall (for [i (range @(rf/subscribe [::i/num-todos]))]
               (let [t @(rf/subscribe [::i/todo i])]
                 ^{:key (:id t)} [todo-item i t])))
    ;; Alternatively we can subscribe to all todos: 
      #_(for [t @(rf/subscribe [::i/todos])]
          ^{:key (:id t)} [:li {:data-id (:id t)} (:txt t)])])
   [new-todo]
   [:div.flex.justify-between.items-center
    [btn {:id :undo-btn
          :disabled (not @(rf/subscribe [:undos?]))
          :on-click #(rf/dispatch [:undo])} [icon "undo.svg"]]
    [:a {:href "https://github.com/mebble/tdcj"} [icon "github.svg"]]
    [btn {:id :redo-btn
          :disabled (not @(rf/subscribe [:redos?]))
          :on-click #(rf/dispatch [:redo])} [icon "redo.svg"]]]])
