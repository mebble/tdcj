(ns tdcj.events
  (:require
   [re-frame.core :as rf]
   [tdcj.db :as db]))

(defn vec-remove
  "remove elem in coll (https://stackoverflow.com/a/18319708/5811761)"
  [pos coll]
  (into (subvec coll 0 pos) (subvec coll (inc pos))))

(rf/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/init-db))

(rf/reg-event-db
  ::add-todo
 (fn [db [_ todo]]
   (-> db
       (update :todos (fn [todos] (conj todos {:txt todo
                                               :id (:count db)
                                               :done false})))
       (update :count inc))))

(rf/reg-event-db
 ::remove-todo
 (fn [db [_ i]]
   (update db :todos (fn [todos] (vec-remove i todos)))))

(rf/reg-event-db
 ::strike-todo
 (fn [db [_ i]]
   (update-in db
           [:todos i :done]
           (fn [done] (not done)))))

(rf/reg-event-db
 ::inc
 [rf/debug]
 (fn [db _]
   (update db :count inc)))
