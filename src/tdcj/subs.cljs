(ns tdcj.subs
  (:require
   [re-frame.core :as re-frame]))
 
(re-frame/reg-sub
  ::todo
 (fn [db [_ n]]
   (nth (:todos db) n)))

(re-frame/reg-sub
 ::todos
 (fn [db _]
   (:todos db)))

(re-frame/reg-sub
 ::num-todos
 (fn [db _]
   (doto (count (:todos db)) println)))
