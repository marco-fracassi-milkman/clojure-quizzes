(ns atomTest
  (:require [clojure.test :refer :all])
  (:require [clojure.walk :refer :all])
  (:use [quiz.atom :refer :all]))

(defn validate [_ _ _ new-value]
  (if (or (< new-value -1) (> new-value 7))
    (throw (Exception. (str "uot of range " new-value)))
  )
)

(deftest incrementAndWatchAtom
  (testing "increment and watch atom"
    (let [counter (atom 0)]
      (add-watch counter nil validate)
      (is (= @counter 0))
      (reset! counter (+ 1 @counter))
      (is (= @counter 1))
      (is (thrown-with-msg? Exception #"uot of range 11" (reset! counter (+ 10 @counter))))
    )
  )
)

(deftest atomMap
  (testing "atom map"
    (let [counter (atom {:valueA 11 :valueB 10})]
      (reset! counter (assoc @counter :valueA (+ 1 (:valueA @counter))))
      (is (= (:valueA @counter) 12))

      (reset! counter (walk (fn [[k _]] [k 0]) identity @counter))
      (is (= @counter {:valueA 0 :valueB 0}))
    )
  )
)