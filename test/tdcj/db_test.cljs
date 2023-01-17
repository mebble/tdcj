(ns tdcj.db-test
  (:require [cljs.test :refer [deftest is]]
            [spy.core :as spy]
            [tdcj.db :as db]))

(deftest test-get-todo-ids
  (let [get-local (spy/stub nil)]
    (is (= [] (db/get-todo-ids get-local))))
  (let [get-local (spy/stub "")]
    (is (= [] (db/get-todo-ids get-local))))
  (let [get-local (spy/stub "[:id1 :id2]")]
    (is (= [:id1 :id2] (db/get-todo-ids get-local)))))

(deftest test-get-todo
  (let [get-local (spy/stub nil)]
    (is (= nil (db/get-todo get-local "id"))))
  (let [get-local (spy/stub "")]
    (is (= nil (db/get-todo get-local "id"))))
  (let [get-local (spy/stub "{:key1 :val1 :key2 :val2}")]
    (is (= nil (db/get-todo get-local "id"))))
  (let [get-local (spy/stub "{:txt :val :id :val :done :val}")]
    (is (= {:txt :val :id :val :done :val} (db/get-todo get-local "id")))))

(deftest test-init-db
  (is (= {:new-todo-txt "" :count 1 :todos []}
         (db/init-db [])))
  (is (= {:new-todo-txt "" :count 6 :todos [{:id 2} {:id 5}]}
         (db/init-db [{:id 2} {:id 5}]))))
