(ns tdcj.subs
  (:require
   [re-frame.core :as rf]))
 
(rf/reg-sub
  ::todo
 (fn [db [_ n]]
   (nth (:todos db) n)))

(rf/reg-sub
 ::todos
 (fn [db _]
   (:todos db)))

(rf/reg-sub
 ::num-todos
 (fn [db _]
   (count (:todos db))))

(rf/reg-sub
 ::new-todo-txt
 (fn [db _]
   (:new-todo-txt db)))
