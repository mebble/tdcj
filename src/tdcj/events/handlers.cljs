(ns tdcj.events.handlers)

(defn- vec-remove
  "remove elem in coll (https://stackoverflow.com/a/18319708/5811761)"
  [pos coll]
  (into (subvec coll 0 pos) (subvec coll (inc pos))))

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

(defn strike-todo [db [_ i]]
  (update-in db
             [:todos i :done]
             (fn [done] (not done))))

(defn edit-todo [db [_ i]]
  (update-in db
             [:todos i :editing]
             (fn [editing] (not editing))))

(defn change-todo [db [_ i val]]
  (assoc-in db [:todos i :txt] val))

(defn edit-new-todo [db [_ val]]
  (assoc db :new-todo-txt val))
