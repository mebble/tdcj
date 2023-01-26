(ns tdcj.events-test
  (:require [cljs.test :refer [deftest is testing]]
            [spy.core :as spy]
            [tdcj.events :as e]
            [tdcj.db :as db]))

(def ^:private db {:todos []
                   :count 0
                   :new-todo-txt ""})

(def ^:private base-ctx {:coeffects {:db {:todos [{:id 0
                                                   :txt "zero"
                                                   :done 'any
                                                   :editing 'any}
                                                  {:id 1
                                                   :txt "one old"
                                                   :done 'any
                                                   :editing 'any}]}}
                         :effects {:db {:todos [{:id 1
                                                 :txt "one new"
                                                 :done 'any
                                                 :editing 'any}
                                                {:id 2
                                                 :txt "two"
                                                 :done 'any
                                                 :editing 'any}]}}})

(defn- set-event [ctx e]
  (assoc-in ctx [:coeffects :event] e))

(deftest event-add-todo
  (testing "empty todo text: does not add a new todo"
    (is (= db
           (e/add-todo db ['any ""]))))

  (testing "some todo text: adds a new todo"
    (let [expected-db {:todos [{:txt "some todo txt"
                                :id 0
                                :done false
                                :editing false}]
                       :count 1
                       :new-todo-txt ""}]
      (is (= expected-db
             (e/add-todo db ['any "some todo txt"])))
      (is (= expected-db
             (e/add-todo (assoc db :new-todo-txt "non-empty")
                         ['any "some todo txt"]))))))

(deftest event-remove-todo
  (let [db (assoc db :todos ['todo1 'todo2 'todo3])
        expected-db (assoc db :todos ['todo1 'todo3])]
    (is (= expected-db (e/remove-todo db ['any 1])))))

(deftest test-effected-todo
  (testing "add todo"
    (let [ctx (set-event base-ctx [::e/add-todo 'any])
          expected {:id 2
                    :txt "two"
                    :done 'any
                    :editing 'any}]
      (is (= expected (e/effected-todo ctx)))))
  (testing "strike todo"
    (let [ctx (set-event base-ctx [::e/strike-todo 0])
          expected {:id 1
                    :txt "one new"
                    :done 'any
                    :editing 'any}]
      (is (= expected (e/effected-todo ctx)))))
  (testing "edit todo"
    (let [set-editing (fn [ctx val] (assoc-in ctx [:effects :db :todos 0 :editing] val))
          ctx (set-event base-ctx [::e/edit-todo 0])]
      (let [ctx (set-editing ctx false)
            expected {:id 1
                      :txt "one new"
                      :done 'any
                      :editing false}]
        (is (= expected (e/effected-todo ctx))))
      (let [ctx (set-editing ctx true)
            expected nil]
        (is (= expected (e/effected-todo ctx))))))
  (testing "remove todo"
    (let [ctx (set-event base-ctx [::e/remove-todo 0])
          expected {:id 0
                    :txt "zero"
                    :done 'any
                    :editing 'any}]
      (is (= expected (e/effected-todo ctx)))))
  (testing "unknown event"
    (let [ctx (set-event base-ctx [:unknown 'any])
          expected nil]
      (is (= expected (e/effected-todo ctx))))))

(deftest fx-put-todo-store
  (let [set-local (spy/spy)
        get-todo-ids (spy/stub ["id1" "id2"])
        put-todo-store (partial e/put-todo-store* set-local get-todo-ids)]

    (testing "todo in store"
      (put-todo-store ["id2" 'todo])
      (is (spy/called-once-with? set-local "id2" 'todo)))

    (testing "todo not in store"
      (spy/reset-spy! set-local)
      (put-todo-store ["id3" 'todo])
      (is (spy/called-n-times? set-local 2))
      (is (spy/called-with? set-local "id3" 'todo))
      (is (spy/called-with? set-local db/todo-ids-key ["id1" "id2" "id3"])))))

(deftest fx-delete-todo-store 
  (let [set-local (spy/spy)
        remove-local (spy/spy)
        get-todo-ids (spy/stub ["id1" "id2" "id3"])
        delete-todo-store (partial e/delete-todo-store* set-local remove-local get-todo-ids)]

    (testing "todo in store"
      (delete-todo-store "id1")
      (is (spy/called-once-with? remove-local "id1"))
      (is (spy/called-once-with? set-local db/todo-ids-key ["id2" "id3"])))

    (testing "todo not in store"
      (spy/reset-spy! set-local)
      (spy/reset-spy! remove-local)
      (delete-todo-store "idx")
      (is (spy/not-called? remove-local))
      (is (spy/not-called? set-local)))))

(deftest test-trim
  (is (= {:any 'any} (e/trim {:any 'any :editing 'any}))))

(deftest test-todo->id-str
  (is (= "todo:123" (e/todo->id-str {:any 'any :id 123}))))
