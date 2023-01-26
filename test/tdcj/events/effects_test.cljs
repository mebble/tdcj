(ns tdcj.events.effects-test
  (:require [cljs.test :refer [deftest is testing]]
            [spy.core :as spy]
            [tdcj.db :refer [todo-ids-key]]
            [tdcj.events.effects :as fx]))

(deftest fx-put-todo-store
  (let [set-local (spy/spy)
        get-todo-ids (spy/stub ["id1" "id2"])
        put-todo-store (partial fx/put-todo-store* set-local get-todo-ids)]

    (testing "todo in store"
      (put-todo-store ["id2" 'todo])
      (is (spy/called-once-with? set-local "id2" 'todo)))

    (testing "todo not in store"
      (spy/reset-spy! set-local)
      (put-todo-store ["id3" 'todo])
      (is (spy/called-n-times? set-local 2))
      (is (spy/called-with? set-local "id3" 'todo))
      (is (spy/called-with? set-local todo-ids-key ["id1" "id2" "id3"])))))

(deftest fx-delete-todo-store
  (let [set-local (spy/spy)
        remove-local (spy/spy)
        get-todo-ids (spy/stub ["id1" "id2" "id3"])
        delete-todo-store (partial fx/delete-todo-store* set-local remove-local get-todo-ids)]

    (testing "todo in store"
      (delete-todo-store "id1")
      (is (spy/called-once-with? remove-local "id1"))
      (is (spy/called-once-with? set-local todo-ids-key ["id2" "id3"])))

    (testing "todo not in store"
      (spy/reset-spy! set-local)
      (spy/reset-spy! remove-local)
      (delete-todo-store "idx")
      (is (spy/not-called? remove-local))
      (is (spy/not-called? set-local)))))
