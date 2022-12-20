(ns tdcj.events
  (:require
   [re-frame.core :as re-frame]
   [tdcj.db :as db]))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/reg-event-db
 ::update-count
 (fn [db _]
   (assoc db :count (-> db :count rest))))
