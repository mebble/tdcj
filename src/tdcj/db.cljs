(ns tdcj.db
  (:require [cljs.reader :refer [read-string]]))

(defn init-db [init-todos]
  {:todos init-todos
   :count (->> init-todos 
               (apply max-key :id)
               (#(if (nil? %) 0 (:id %)))
               (inc))
   :new-todo-txt ""})

(defn set-local [key val]
  (.setItem js/localStorage key val))

(defn get-local [key]
  (.getItem js/localStorage key))

(defn remove-local [key]
  (.removeItem js/localStorage key))

(def todo-ids-key "meta:ids")

(defn get-todo-ids []
  (let [res (get-local todo-ids-key)]
    (if res
      (read-string res)
      [])))

(defn get-todo [id]
  (some-> (get-local id) read-string))
