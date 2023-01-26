(ns tdcj.events.effects
  (:require [tdcj.db :as db]))

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
