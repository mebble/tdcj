(ns tdcj.events
  (:require
   [re-frame.core :as rf]
   [tdcj.db :as db]))

(defn- vec-remove
  "remove elem in coll (https://stackoverflow.com/a/18319708/5811761)"
  [pos coll]
  (into (subvec coll 0 pos) (subvec coll (inc pos))))

(def todo->local-store
  (rf/->interceptor
   :after (fn [ctx]
            (let [[event-name payload] (-> ctx :coeffects :event) 
                  old-db (rf/get-coeffect ctx :db)
                  new-db (rf/get-effect ctx :db)
                  todo (case event-name
                         ::add-todo    (-> new-db :todos last)
                         ::strike-todo (-> new-db :todos (nth payload))
                         ::edit-todo   (-> new-db :todos (nth payload) (#(when-not (:editing %) %)))
                         ::remove-todo (-> old-db :todos (nth payload)))]
              (if todo
                (let [id-str (->> todo :id (str "todo:"))]
                  (case event-name
                    ::remove-todo   (rf/assoc-effect ctx ::delete-todo-store id-str)
                    (rf/assoc-effect ctx ::put-todo-store [id-str todo])))
                ctx)))))

(rf/reg-event-db
 ::initialize-db
 (fn [_ _]
   (->> (db/get-todo-ids)
        (map (partial db/get-todo))
        (remove nil?)
        (vec)
        (db/init-db))))

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
 [todo->local-store]
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
 [todo->local-store]
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

(rf/reg-fx
 ::put-todo-store
 (fn [[id-str todo]]
   (db/set-local id-str todo)
   (let [existing-ids (db/get-todo-ids)]
     (when-not (some #{id-str} existing-ids)
       (db/set-local db/todo-ids-key (conj existing-ids id-str))))))

(rf/reg-fx
 ::delete-todo-store
 (fn [id-str]
   (let [existing-ids (db/get-todo-ids)]
     (when (some #{id-str} existing-ids)
       (->> existing-ids
            (remove #{id-str})
            (vec)
            (db/set-local db/todo-ids-key))))
   (db/remove-local id-str)))
