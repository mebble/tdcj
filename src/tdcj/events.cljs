(ns tdcj.events
  (:require
   [re-frame.core :as rf]
   [tdcj.db :as db]))

(rf/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/init-db))

(rf/reg-event-db
  ::add-todo
 (fn [db [_ todo]]
   (-> db
       (update :todos (fn [todos] (conj todos {:txt todo
                                               :id (:count db)})))
       (update :count inc))))

(rf/reg-event-db
 ::inc
 [rf/debug]
 (fn [db _]
   (update db :count inc)))
