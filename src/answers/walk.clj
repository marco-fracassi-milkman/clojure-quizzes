(require
  '[datomic.api :as d]
  ;'[datomic.samples.repl :as repl]
  '[clojure.walk :refer [ walk postwalk postwalk-demo prewalk prewalk-demo]]
  '[clojure.core.async :as async :refer [>! <! >!! <!! go chan
                                         close! alts! alts!! thread timeout
                                         sliding-buffer]])

;The semantics for walk are basically:
;make a collection of the same type where each item
;has been replaced by the value of the inner function for that item
;return the result of applying outer to the new collection:
(walk inc identity #{0 1 2})



; walk the following collection so that the result
; is the first argument of each inner vector in reverse order
; [ [1 2] [3 4] [5 6] ]
(walk first reverse [ [1 2] [3 4] [5 6] ])



; walk the following collection so that the result
; is the sum of twice each element
; [1 2 3 4 5]
(walk #(* 2 %) #(apply + %) [1 2 3 4 5])



; walk the following collection so that the result
; is the maximum of all of the last elements of the inner vectors
; [ [1 2] [3 4] [5 6] ]
(walk second #(apply max %) [ [1 2] [3 4] [5 6] ])



; walk the following map so that the result
; is the same map with its values multiplied by ten
; {:a 1 :b 2 :c 3}
(walk (fn [[k v]] [k (* 10 v)]) identity {:a 1 :b 2 :c 3})


; walk the following collection so that the result
; is a list of all the letters that appear as first element without duplicates
; [["ab" 1] ["b" 2] ["c" 3]]
(walk first #(apply concat %)  [["ab" 1] ["b" 2] ["c" 3]])
;(walk first (fn[l] (distinct (apply concat l)) )  [["ab" 1] ["b" 2] ["c" 3]])


; 'postwalk' the following map. For each iteration of 'postwalk',
; print:
;   "current line number : element-walked-key -> element-walked-value"
; {:a 1 :b 2}
(let [counter (atom -1)
      line-counter (atom 0)
      print-touch (fn [x]
                    (print (swap! line-counter inc) ":" (pr-str x) "-> "))
      change (fn [x]
               (let [new-x (swap! counter inc)]
                 (prn new-x)
                 [new-x x]))]
  (postwalk (fn [x]
              (print-touch x)
              (change x))
            {:a 1 :b 2}))

(postwalk
  (fn [x]
    (let [new (if (number? x) (* x 2) x)]
      (printf "%s -> %s\n" x new)
      new))
  '(1 (2 3)))


; using 'prewalk' increase all the values of the following map
; only if they are numbers
; [{:a 1 :b "2"} {:c 3 :d 4} {:e 5 :f 6}]
(prewalk #(if (number? %)
            (inc %) %)
         [{:a 1 :b "2"} {:c 3 :d 4} {:e 5 :f 6}])

