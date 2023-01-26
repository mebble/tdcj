(ns tdcj.misc-test
  (:require [cljs.test :refer [deftest is]]
            [tdcj.misc :as m]))

(deftest test-trim
  (is (= {:any 'any} (m/trim {:any 'any :editing 'any}))))

(deftest test-todo->id-str
  (is (= "todo:123" (m/todo->id-str {:any 'any :id 123}))))
