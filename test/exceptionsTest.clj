(ns exceptionsTest
  (:require [clojure.test :refer :all])
  (:use [quiz.exceptions :refer :all]))

(deftest catchGenericExceptionReturnsOkTest
  (testing "catch a generic exception"
    (let [result (catchException #(throw (Exception. )))]
      (is (= result "OKfinally"))
      )
    )
  )

(deftest catchArithmeticExceptionReturnsFooTest
  (testing "catch a generic exception"
    (println "2")
    (let [result (catchException #(throw (ArithmeticException. )))]
      (is (= result "FOOfinally"))
      )
    )
  )

(deftest finallyAppendEsclamationTest
  (testing "finally append esclamation"
    (println "3")
    (let [result (catchException #(throw (ArithmeticException. )))]
      (is (= result "FOOfinally"))
      )
    )
  )