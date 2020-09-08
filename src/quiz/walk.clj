(require
  '[datomic.api :as d]
  '[datomic.samples.repl :as repl]
  '[clojure.walk :refer [walk postwalk postwalk-demo prewalk prewalk-demo]]
  '[clojure.core.async :as async :refer [>! <! >!! <!! go chan
                                         close! alts! alts!! thread timeout
                                         sliding-buffer]])

; walk the following collection so that the result
; is the first argument of each inner vector in reverse order
; [ [1 2] [3 4] [5 6] ]
(walk first reverse [[1 2] [3 4] [5 6]])



; walk the following collection so that the result
; is the sum of twice each element
; [1 2 3 4 5]
(walk #(* 2 %) #(reduce + %) [1 2 3 4 5])
(walk #(* 2 %) #(apply + %) [1 2 3 4 5])


; walk the following collection so that the result
; is the maximum of all of the last elements of the inner vectors
; [ [1 2] [3 4] [5 6] ]
(walk last #(apply max %) [[1 2] [3 4] [5 6]])



; walk the following map so that the result
; is the same map with its values multiplied by ten
; {:a 1 :b 2 :c 3}
 (walk (fn [[k, v]] [k, (* 10 v)])
       (fn [m] [m])
      {:a 1 :b 2 :c 3})

(walk (fn [[k, v]] [k, (* 10 v)])
      identity
      {:a 1 :b 2 :c 3})

; walk the following collection so that the result
; is a list of all the letters that appear as first element without duplicates
; [["ab" 1] ["b" 2] ["c" 3]]
(walk first
      (fn[l] (distinct (apply concat l)) )
      [["ab" 1] ["b" 2] ["c" 3]])


; 'postwalk' the following map. For each iteration of 'postwalk',
; print:
;   "current line number : element-walked-key -> element-walked-value"
; {:a 1 :b 2}




; using 'prewalk' increase all the values of the following map
; only if they are numbers
; [{:a 1 :b "2"} {:c 3 :d 4} {:e 5 :f 6}]
(prewalk   #(if (number? %) (inc %) %)
           [{:a 1 :b "2"} {:c 3 :d 4} {:e 5 :f 6}])

