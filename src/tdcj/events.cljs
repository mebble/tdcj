(ns tdcj.events
  (:require
   [re-frame.core :as rf]
   [tdcj.db :as db]))

(defn- vec-remove
  "remove elem in coll (https://stackoverflow.com/a/18319708/5811761)"
  [pos coll]
  (into (subvec coll 0 pos) (subvec coll (inc pos))))

(defn todo-store-effect [ctx]
  (let [[event-name payload] (-> ctx :coeffects :event)
        old-db (rf/get-coeffect ctx :db)
        new-db (rf/get-effect ctx :db)
        todo (case event-name
               ::add-todo    (-> new-db :todos last)
               ::strike-todo (-> new-db :todos (nth payload))
               ::edit-todo   (-> new-db :todos (nth payload) (#(when-not (:editing %) %)))
               ::remove-todo (-> old-db :todos (nth payload))
               nil)]
    (if todo
      (let [todo-trimmed (dissoc todo :editing)
            id-str (->> todo-trimmed :id (str "todo:"))]
        (if (= event-name ::remove-todo)
          (rf/assoc-effect ctx ::delete-todo-store id-str)
          (rf/assoc-effect ctx ::put-todo-store [id-str todo-trimmed])))
      ctx)))

(defn add-todo [db [_ todo]]
  (if (empty? todo)
    db
    (-> db
        (update :todos (fn [todos] (conj todos {:txt todo
                                                :id (:count db)
                                                :done false
                                                :editing false})))
        (update :count inc)
        (assoc :new-todo-txt ""))))

(defn remove-todo [db [_ i]]
  (update db :todos (fn [todos] (vec-remove i todos))))

(defn put-todo-store [set-local get-todo-ids [id-str todo]]
  (set-local id-str todo)
  (let [existing-ids (get-todo-ids)]
    (when-not (some #{id-str} existing-ids)
      (set-local db/todo-ids-key (conj existing-ids id-str)))))

(defn delete-todo-store [set-local remove-local get-todo-ids id-str]
  (let [existing-ids (get-todo-ids)]
    (when (some #{id-str} existing-ids)
      (->> existing-ids
           (remove #{id-str})
           (vec)
           (set-local db/todo-ids-key))
      (remove-local id-str))))

(def todo->local-store
  (rf/->interceptor
   :after todo-store-effect))

(def get-todo-ids (partial db/get-todo-ids db/get-local))
(def get-todo (partial db/get-todo db/get-local))

(rf/reg-event-db
 ::initialize-db
 (fn [_ _]
   (->> (get-todo-ids)
        (map (partial get-todo))
        (remove nil?)
        (vec)
        (db/init-db))))

(rf/reg-event-db
  ::add-todo
 [todo->local-store]
 add-todo)

(rf/reg-event-db
 ::remove-todo
 [todo->local-store]
 remove-todo)

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
 (partial put-todo-store db/set-local get-todo-ids))

(rf/reg-fx
 ::delete-todo-store
 (partial delete-todo-store db/set-local db/remove-local get-todo-ids))
