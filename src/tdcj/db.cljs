(ns tdcj.db
  (:require [cljs.reader :refer [read-string]]))

(def init-db
  {:todos []
   :count 0
   :new-todo-txt ""})

(defn set-local [key val]
  (.setItem js/localStorage key val))

(defn get-local [key]
  (.getItem js/localStorage key val))

(def todo-ids-key "meta:ids")

(defn get-todo-ids []
  (let [res (get-local todo-ids-key)]
    (if res
      (read-string res)
      [])))
