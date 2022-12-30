(ns tdcj.events
  (:require
   [re-frame.core :as rf]
   [tdcj.db :as db]))

(defn vec-remove
  "remove elem in coll (https://stackoverflow.com/a/18319708/5811761)"
  [pos coll]
  (into (subvec coll 0 pos) (subvec coll (inc pos))))

(def todo->local-store (rf/after (fn [db [event-name payload]]
                                (let [todo (case event-name 
                                             ::add-todo    (-> db :todos last) 
                                             ::strike-todo (-> db :todos (nth payload)))]
                                  (when todo
                                    (db/set-local (->> todo :id (str "todo:")) todo))))))

(rf/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/init-db))

(rf/reg-event-db
  ::add-todo
 [todo->local-store]
 (fn [db [_ todo]]
   (-> db
       (update :todos (fn [todos] (conj todos {:txt todo
                                               :id (:count db)
                                               :done false
                                               :editing false})))
       (update :count inc)
       (assoc :new-todo-txt ""))))

(rf/reg-event-db
 ::remove-todo
 (fn [db [_ i]]
   (update db :todos (fn [todos] (vec-remove i todos)))))

(rf/reg-event-db
 ::strike-todo
 [todo->local-store]
 (fn [db [_ i]]
   (update-in db
           [:todos i :done]
           (fn [done] (not done)))))

(rf/reg-event-db
 ::edit-todo
 (fn [db [_ i]]
   (update-in db
              [:todos i :editing]
              (fn [editing] (not editing)))))

(rf/reg-event-db
 ::change-todo
 (fn [db [_ i val]]
   (assoc-in db [:todos i :txt] val)))

(rf/reg-event-db
 ::edit-new-todo
 (fn [db [_ val]]
   (assoc db :new-todo-txt val)))
