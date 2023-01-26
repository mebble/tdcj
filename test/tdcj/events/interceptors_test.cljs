(ns tdcj.events.interceptors-test
  (:require [cljs.test :refer [deftest is testing]]
            [tdcj.ids :as i]
            [tdcj.events.interceptors :as in]))

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

(deftest test-effected-todo
  (testing "add todo"
    (let [ctx (set-event base-ctx [::i/add-todo 'any])
          expected {:id 2
                    :txt "two"
                    :done 'any
                    :editing 'any}]
      (is (= expected (in/effected-todo ctx)))))
  (testing "strike todo"
    (let [ctx (set-event base-ctx [::i/strike-todo 0])
          expected {:id 1
                    :txt "one new"
                    :done 'any
                    :editing 'any}]
      (is (= expected (in/effected-todo ctx)))))
  (testing "edit todo"
    (let [set-editing (fn [ctx val] (assoc-in ctx [:effects :db :todos 0 :editing] val))
          ctx (set-event base-ctx [::i/edit-todo 0])]
      (let [ctx (set-editing ctx false)
            expected {:id 1
                      :txt "one new"
                      :done 'any
                      :editing false}]
        (is (= expected (in/effected-todo ctx))))
      (let [ctx (set-editing ctx true)
            expected nil]
        (is (= expected (in/effected-todo ctx))))))
  (testing "remove todo"
    (let [ctx (set-event base-ctx [::i/remove-todo 0])
          expected {:id 0
                    :txt "zero"
                    :done 'any
                    :editing 'any}]
      (is (= expected (in/effected-todo ctx)))))
  (testing "unknown event"
    (let [ctx (set-event base-ctx [:unknown 'any])
          expected nil]
      (is (= expected (in/effected-todo ctx))))))
