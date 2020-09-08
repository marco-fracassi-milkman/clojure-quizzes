(require
  '[datomic.api :as d]
  '[datomic.samples.repl :as repl])

; define a counter using 'atom'
(def counter (atom -1))



; increase the counter value
(defn next-value []
  (swap! counter inc))
(next-value)



; define a counter and init its value to 0 so that it will return 0
; at first call
(def atom-counter (atom 0))
(defn atom-next-value []
  (let [result @atom-counter]
    (swap! atom-counter inc)
    result))
(atom-next-value)



; define a watcher on an atom that executes a function every time the state of the atom changes
(def whatched (atom {}))
(add-watch whatched :watcher-alert
           (fn [key watched old-state new-state]
             (if (> (:val new-state) 5)
               (do (println "Dude, u r way 2 fast"))
               (do (println "Dude, u r way 2 slow")))))
(reset! whatched {:val 6})
(reset! whatched {:val 1})



; define a validator over a counter that returns error when the value of the counter
; is not between -1 and 7
(defn counter-validator
  [current-value]
  (and (>= current-value -1)
       (<= current-value 7)))
(def counter (atom 7 :validator counter-validator))
(swap! counter inc)



; given the following atom, define a function to increase
; its map values by a given amount
(def foo (atom {:valueA 1
                 :valueB 0}))
(defn increase-foo
  [foo-state increase-by]
  (merge-with + foo-state {:valueA increase-by :valueB increase-by}))
(swap! foo increase-foo 10)



; increase by 1 valueA key of foo atom
(swap! foo update-in [:valueA] + 1)



; reset the atom to 0 for all of its key
(reset! foo {:valueA 0
             :valueB 0})







