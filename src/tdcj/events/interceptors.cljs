(ns tdcj.events.interceptors
  (:require [re-frame.core :as rf]
            [tdcj.ids :as i]
            [tdcj.misc :as m]))

(defn effected-undo-todo [old-db new-db event prev-event]
  (let [[event-id _] event
        [prev-event-id payload] prev-event]
    (case [prev-event-id event-id]
      [::i/strike-todo :undo] (-> new-db :todos (nth payload))
      [::i/strike-todo :redo] (-> new-db :todos (nth payload))
      [::i/add-todo    :undo] (-> old-db :todos last)
      [::i/add-todo    :redo] (-> new-db :todos last)
      [::i/remove-todo :undo] (-> new-db :todos (nth payload))
      [::i/remove-todo :redo] (-> old-db :todos (nth payload))
      nil)))

(defn effected-todo [ctx]
  (let [[event-id payload] (-> ctx :coeffects :event)
        old-db (rf/get-coeffect ctx :db)
        new-db (rf/get-effect ctx :db)]
    (case event-id
      ::i/add-todo    (-> new-db :todos last)
      ::i/strike-todo (-> new-db :todos (nth payload))
      ::i/edit-todo   (-> new-db :todos (nth payload) (#(when-not (:editing %) %)))
      ::i/remove-todo (-> old-db :todos (nth payload))
      nil)))

(defn todo-store-effect [ctx]
  (if-let [todo (effected-todo ctx)]
    (let [event-id (-> ctx :coeffects :event first)
          id-str (m/todo->id-str todo)]
      (if (= event-id ::i/remove-todo)
        (rf/assoc-effect ctx ::i/delete-todo-store id-str)
        (rf/assoc-effect ctx ::i/put-todo-store [id-str (m/trim todo)])))
    ctx))

(defn- set-undo-effects [ctx old-db new-db event prev-event]
  (if-let [todo (effected-undo-todo old-db new-db event prev-event)]
    (let [event-id (first event)
          prev-event-id (first prev-event)
          id-str (m/todo->id-str todo)]
      (case [prev-event-id event-id]
        [::i/add-todo    :undo] (rf/assoc-effect ctx ::i/delete-todo-store id-str)
        [::i/remove-todo :redo] (rf/assoc-effect ctx ::i/delete-todo-store id-str)
        (rf/assoc-effect ctx ::i/put-todo-store [id-str (m/trim todo)])))
    ctx))

(defn undo-redo-effect* [app-db ctx]
  (let [event (-> ctx :coeffects :event)]
    (cond
      (m/is-undo-redo event) (let [old-db (-> ctx :coeffects :db)
                                   new-db @app-db    ;; the re-frame.undo lib accesses app-db directly to undo and redo the app state. Hence we have to do the same to get new-db, not through [:effects :db]
                                   prev-event (if (m/is-event :undo event)
                                                (:prev-event old-db)
                                                (:prev-event new-db))]
                               (set-undo-effects ctx old-db new-db event prev-event))
      (m/is-undoable event)  (assoc-in ctx [:effects :db :prev-event] event)
      :else                ctx)))
