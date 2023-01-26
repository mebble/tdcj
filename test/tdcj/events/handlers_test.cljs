(ns tdcj.events.handlers-test
  (:require [cljs.test :refer [deftest is testing]]
            [tdcj.events.handlers :as h]))

(def ^:private db {:todos []
                   :count 0
                   :new-todo-txt ""})

(deftest event-add-todo
  (testing "empty todo text: does not add a new todo"
    (is (= db
           (h/add-todo db ['any ""]))))

  (testing "some todo text: adds a new todo"
    (let [expected-db {:todos [{:txt "some todo txt"
                                :id 0
                                :done false
                                :editing false}]
                       :count 1
                       :new-todo-txt ""}]
      (is (= expected-db
             (h/add-todo db ['any "some todo txt"])))
      (is (= expected-db
             (h/add-todo (assoc db :new-todo-txt "non-empty")
                         ['any "some todo txt"]))))))

(deftest event-remove-todo
  (let [db (assoc db :todos ['todo1 'todo2 'todo3])
        expected-db (assoc db :todos ['todo1 'todo3])]
    (is (= expected-db (h/remove-todo db ['any 1])))))
