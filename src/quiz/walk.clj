(ns quiz.walk
  (:gen-class))

(require '[clojure.walk :refer :all])
(require '[clojure.string :as str])

; walk the following collection so that the result
; is the first argument of each inner vector in reverse order
; [ [1 2] [3 4] [5 6] ]

(defn walkEachInnerVectorInReverseOrder [vector]
  (map first (reverse vector))
)

; walk the following collection so that the result
; is the sum of twice each element
; [1 2 3 4 5]

(defn twice [number]
  (* number 2)
)

(defn sum [numbers]
  (reduce + numbers)
)

(defn sumOfTwiceEachElement [vector]
  (sum (map twice vector))
)


; walk the following collection so that the result
; is the maximum of all of the last elements of the inner vectors
; [ [1 2] [3 4] [5 6] ]

(defn maximumOfAllOfTheLastElementsOfTheInnerVectors [vector]
  (reduce max (map last vector))
)


; walk the following map so that the result
; is the same map with its values multiplied by ten
; {:a 1 :b 2 :c 3}

(defn multiplyValueByTen [pair]
  [(key pair) (* 10 (val pair))]
)

(defn sameMapWithItsValuesMultipliedByTen [hashmap]
  (apply hash-map (flatten (map multiplyValueByTen hashmap)))
)

; walk the following collection so that the result
; is a list of all the letters that appear as first element without duplicates
; [["ab" 1] ["b" 2] ["c" 3]]

(defn splittedFirst [vector]
  (str/split (first vector) #"")
)

(defn listOfAllTheLettersThatAppearAsFirstElementWithoutDuplicates [matrix]
  (distinct (flatten (map splittedFirst matrix)))
)

; 'postwalk' the following map. For each iteration of 'postwalk',
; print:
;   "current line number : element-walked-key -> element-walked-value"
; {:a 1 :b 2}




; using 'prewalk' increase all the values of the following map
; only if they are numbers
; [{:a 1 :b "2"} {:c 3 :d 4} {:e 5 :f 6}]


