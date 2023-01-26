(ns tdcj.init
  (:require
   [re-frame.core :as rf]
   [re-frame.db :refer [app-db]]
   [day8.re-frame.undo :refer [undoable]]
   [tdcj.db :as db]
   [tdcj.ids :as i]
   [tdcj.events.handlers :as h]
   [tdcj.events.interceptors :refer [undo-redo-effect* todo-store-effect]]
   [tdcj.events.effects :refer [put-todo-store* delete-todo-store*]]))

(def get-todo-ids (partial db/get-todo-ids db/get-local))
(def get-todo (partial db/get-todo db/get-local))

(rf/reg-global-interceptor
 (rf/->interceptor
  :after (partial undo-redo-effect* app-db)))

(def todo->local-store
  (rf/->interceptor
   :after todo-store-effect))

(rf/reg-event-db
 ::i/initialize-db
 (fn [_ _]
   (->> (get-todo-ids)
        (map (partial get-todo))
        (remove nil?)
        (vec)
        (db/init-db))))

(rf/reg-event-db
 ::i/add-todo
 [(undoable "Undo add item") todo->local-store]
 h/add-todo)

(rf/reg-event-db
 ::i/remove-todo
 [(undoable "Undo remove item") todo->local-store]
 h/remove-todo)

(rf/reg-event-db
 ::i/strike-todo
 [(undoable "Undo strike todo") todo->local-store]
 h/strike-todo)

(rf/reg-event-db
 ::i/edit-todo
 [todo->local-store]
 h/edit-todo)

(rf/reg-event-db
 ::i/change-todo
 h/change-todo)

(rf/reg-event-db
 ::i/edit-new-todo
 h/edit-new-todo)

(def put-todo-store (partial put-todo-store* db/set-local get-todo-ids))
(def delete-todo-store (partial delete-todo-store* db/set-local db/remove-local get-todo-ids))

(rf/reg-fx
 ::i/put-todo-store
 put-todo-store)

(rf/reg-fx
 ::i/delete-todo-store
 delete-todo-store)

(rf/reg-sub
 ::i/todo
 (fn [db [_ n]]
   (nth (:todos db) n)))

(rf/reg-sub
 ::i/todos
 (fn [db _]
   (:todos db)))

(rf/reg-sub
 ::i/num-todos
 (fn [db _]
   (count (:todos db))))

(rf/reg-sub
 ::i/new-todo-txt
 (fn [db _]
   (:new-todo-txt db)))

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
