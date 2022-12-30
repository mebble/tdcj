(ns tdcj.db)

(def init-db
  {:todos []
   :count 0
   :new-todo-txt ""})

(defn set-local [key val]
  (.setItem js/localStorage key val))

(defn get-local [key]
  (.getItem js/localStorage key val))
