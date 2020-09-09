(ns walkTest
  (:require [clojure.test :refer :all])
  (:use [quiz.walk :refer :all]))

(deftest walkEachInnerVectorInReverseOrderTest
  (testing "walk each inner vector in reverse order"
    (let [result (walkEachInnerVectorInReverseOrder [ [1 2] [3 4] [5 6] ])]
      (is (= result [5 3 1]))
      )
    )
  )

(deftest sumOfTwiceEachElementTest
  (testing "sum of twice each element"
    (let [result (sumOfTwiceEachElement [1 2 3 4 5])]
      (is (= result 30))
      )
    )
  )

(deftest maximumOfAllOfTheLastElementsOfTheInnerVectorsTest
  (testing "maximum of all of the last elements of the inner vectors"
    (let [result (maximumOfAllOfTheLastElementsOfTheInnerVectors [ [1 2] [3 4] [5 6] ])]
      (is (= result 6))
      )
    )
  )

(deftest sameMapWithItsValuesMultipliedByTenTest
  (testing "same map with its values multiplied by ten"
    (let [result (sameMapWithItsValuesMultipliedByTen {:a 1 :b 2 :c 3})]
      (is (= result {:a 10 :b 20 :c 30}))
      )
    )
  )
