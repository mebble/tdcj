(ns tdcj.db
  (:require [cljs.reader :refer [read-string]]))

;; ------- Low level -------

(def todo-ids-key "meta:ids")

(defn set-local [key val]
  (.setItem js/localStorage key val))

(defn get-local [key]
  (.getItem js/localStorage key))

(defn remove-local [key]
  (.removeItem js/localStorage key))

;; ------- High level -------

(defn init-db [init-todos]
  {:todos init-todos
   :count (->> init-todos
               (apply max-key :id)
               (#(if (nil? %) 0 (:id %)))
               (inc))
   :new-todo-txt ""
   :prev-event nil})

(defn get-todo-ids [get-local]
  (let [res (get-local todo-ids-key)]
    (if (empty? res)
      []
      (read-string res))))

(defn- contains-keys [m ks]
  (when (every? #(contains? m %) ks)
    m))

(defn get-todo [get-local id]
  (some-> (get-local id)
          (read-string)
          (contains-keys [:txt :id :done])))
