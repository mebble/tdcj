(ns tdcj.events
  (:require
   [re-frame.core :as rf]
   [re-frame.db :refer [app-db]]
   [day8.re-frame.undo :refer [undoable]]
   [tdcj.db :as db]))

(defn- vec-remove
  "remove elem in coll (https://stackoverflow.com/a/18319708/5811761)"
  [pos coll]
  (into (subvec coll 0 pos) (subvec coll (inc pos))))

(defn- is-event [event-id event]
  (-> event
      (first)
      (= event-id)))

(defn- is-undo-redo [event]
  (some #(is-event % event) [:undo :redo]))

(defn- is-undoable [event]
  (some #(is-event % event) [::add-todo ::remove-todo ::strike-todo]))

(defn trim [todo]
  (dissoc todo :editing))

(defn todo->id-str [todo]
  (->> todo :id (str "todo:")))

(defn effected-undo-todo [old-db new-db event prev-event]
  (let [[event-id _] event
        [prev-event-id payload] prev-event]
    (case [prev-event-id event-id]
      [::strike-todo :undo] (-> new-db :todos (nth payload))
      [::strike-todo :redo] (-> new-db :todos (nth payload))
      [::add-todo    :undo] (-> old-db :todos last)
      [::add-todo    :redo] (-> new-db :todos last)
      [::remove-todo :undo] (-> new-db :todos (nth payload))
      [::remove-todo :redo] (-> old-db :todos (nth payload))
      nil)))

(defn- set-undo-effects [ctx old-db new-db event prev-event]
  (if-let [todo (effected-undo-todo old-db new-db event prev-event)]
    (let [event-id (first event)
          prev-event-id (first prev-event)
          id-str (todo->id-str todo)]
      (case [prev-event-id event-id]
        [::add-todo    :undo] (rf/assoc-effect ctx ::delete-todo-store id-str)
        [::remove-todo :redo] (rf/assoc-effect ctx ::delete-todo-store id-str)
        (rf/assoc-effect ctx ::put-todo-store [id-str (trim todo)])))
    ctx))

(defn undo-redo-effect* [app-db ctx]
  (let [event (-> ctx :coeffects :event)]
    (cond
      (is-undo-redo event) (let [old-db (-> ctx :coeffects :db)
                                 new-db @app-db    ;; the re-frame.undo lib accesses app-db directly to undo and redo the app state. Hence we have to do the same to get new-db, not through [:effects :db]
                                 prev-event (if (is-event :undo event)
                                              (:prev-event old-db)
                                              (:prev-event new-db))]
                             (set-undo-effects ctx old-db new-db event prev-event))
      (is-undoable event)  (assoc-in ctx [:effects :db :prev-event] event)
      :else                ctx)))

(defn effected-todo [ctx]
  (let [[event-id payload] (-> ctx :coeffects :event)
        old-db (rf/get-coeffect ctx :db)
        new-db (rf/get-effect ctx :db)]
    (case event-id
      ::add-todo    (-> new-db :todos last)
      ::strike-todo (-> new-db :todos (nth payload))
      ::edit-todo   (-> new-db :todos (nth payload) (#(when-not (:editing %) %)))
      ::remove-todo (-> old-db :todos (nth payload))
      nil)))

(defn todo-store-effect [ctx]
  (if-let [todo (effected-todo ctx)]
    (let [event-id (-> ctx :coeffects :event first)
          id-str (todo->id-str todo)]
      (if (= event-id ::remove-todo)
        (rf/assoc-effect ctx ::delete-todo-store id-str)
        (rf/assoc-effect ctx ::put-todo-store [id-str (trim todo)])))
    ctx))

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

(defn put-todo-store* [set-local get-todo-ids [id-str todo]]
  (set-local id-str todo)
  (let [existing-ids (get-todo-ids)]
    (when-not (some #{id-str} existing-ids)
      (set-local db/todo-ids-key (conj existing-ids id-str)))))

(defn delete-todo-store* [set-local remove-local get-todo-ids id-str]
  (let [existing-ids (get-todo-ids)]
    (when (some #{id-str} existing-ids)
      (->> existing-ids
           (remove #{id-str})
           (vec)
           (set-local db/todo-ids-key))
      (remove-local id-str))))

(def get-todo-ids (partial db/get-todo-ids db/get-local))
(def get-todo (partial db/get-todo db/get-local))
(def put-todo-store (partial put-todo-store* db/set-local get-todo-ids))
(def delete-todo-store (partial delete-todo-store* db/set-local db/remove-local get-todo-ids))

;; Notes
;; - harvest-fn is run during the following events: every "undoable" event, undo event, redo event 
;; - reinstate-fn is run during the following events: undo event, redo event
;; - Inside the reinstate-fn,
;;   - app-db (a ratom) is the old state (ie before the undo/redo events)
;;   - db (a would-be value inside app-db) is the new state (ie after the undo/redo events)
;; (undo-config! {:harvest-fn (fn [app-db] @app-db)
;;                :reinstate-fn (fn [app-db db]
;;                                (let [undo-latest-event (:prev-event @app-db)
;;                                      redo-latest-event (:prev-event db)])
;;                                (println "app-db" @app-db)
;;                                (println "db" db)
;;                                (reset! app-db db))})

(rf/reg-global-interceptor
 (rf/->interceptor
  :after (partial undo-redo-effect* app-db)))

(def todo->local-store
  (rf/->interceptor
   :after todo-store-effect))

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
 [(undoable "Undo add item") todo->local-store]
 add-todo)

(rf/reg-event-db
 ::remove-todo
 [(undoable "Undo remove item") todo->local-store]
 remove-todo)

(rf/reg-event-db
 ::strike-todo
 [(undoable "Undo strike todo") todo->local-store]
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
 put-todo-store)

(rf/reg-fx
 ::delete-todo-store
 delete-todo-store)
