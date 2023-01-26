(ns tdcj.misc
  (:require [tdcj.ids :as i]))

(defn is-event [event-id event]
  (-> event
      (first)
      (= event-id)))

(defn is-undo-redo [event]
  (some #(is-event % event) [:undo :redo]))

(defn is-undoable [event]
  (some #(is-event % event) [::i/add-todo ::i/remove-todo ::i/strike-todo]))

(defn trim [todo]
  (dissoc todo :editing))

(defn todo->id-str [todo]
  (->> todo :id (str "todo:")))
