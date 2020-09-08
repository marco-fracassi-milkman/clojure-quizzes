(require
  '[datomic.api :as d]
  '[datomic.samples.repl :as repl])

; define a counter using 'atom'

(def counter (atom -1))



; increase the counter value

(swap! counter inc)


; define a counter and init its value to 0 so that it will return 0
; at first call

(def counter (atom 0))
@counter


; define a watcher on an atom that executes a function every time the state of the atom changes

;(add-watch reference key fn)
;Adds a watch function to an agent/atom/var/ref reference. The watch
;fn must be a fn of 4 args: a key, the reference, its old-state, its
;new-state.

(def a (atom 0))
(defn watch-fn [key, ref, old, new]
      (println "State changed!")
      (println (str "Key: " key) )
      (println (str "Reference: " ref))
      (println (str "Old state: " old))
      (println (str "New state: " new))
)
(add-watch a "mykey" watch-fn)
@a
(swap! a inc)
@a

; define a validator over a counter that returns error when the value of the counter
; is not between -1 and 7

(defn counter-validator? [counter]
      (and (>= counter -1) (<= counter 7)) )

(def counter (atom -1 :validator counter-validator?))
@atom
(swap! counter #(+ 7 % ))
@atom
(swap! counter inc)

; given the following atom, define a function to increase
; its values by a given amount e.g
; (def foo (atom {:valueA 11
;                :valueB 10}))
(def foo (atom {:valueA 1
                :valueB 0}))

(defn increase-foo [x]
      { :valueA  (+ 10 (:valueA x) )
        :valueB  (+ 10 (:valueB x) )
      }
)

(swap! foo increase-foo)


(defn increase-foo [amap]
  (merge
    (for [pair amap]
      {(first pair) (+ 10 (last pair)) }
    )
  )
)

(defn increase-foo2 [amap]
   (for [pair amap]
     {(first pair) (+ 10 (last pair))}
   )
)

(def foo {:valueA 1 :valueB 0})
(increase-foo foo)
(increase-foo2 foo)

; increase by 1 valueA key of foo atom

(defn increase-value-a [amap]
      {:valueA  (inc (:valueA amap)),
       :valueB  (:valueB amap)}
)
(swap! foo increase-value-a)


; reset the atom to 0 for all of its key
(reset! foo {:valueA 0 :valueB 0})







