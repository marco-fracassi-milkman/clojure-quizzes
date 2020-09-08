(require '[clojure.core.async :as async :refer :all])

; define an unbuffered channel
(def c (chan))

; define a bufferred channel
(chan 10)

; define an unbuffered channel, print the body you've put in it, then `close!` the channel.
(let [c (chan)]
  (go (println (<! c)))
  (>!! c "val")
  (close! c))

; define a function which returns two unbuffered channels: input and output.
; The latter returns the input of the former.
(defn i-o
  []
  (let [in (chan)
        out (chan)]
    (go (let [item (<! in)]
          (>! out item)))
    [in out]))

(let [[in out] (i-o)]
  (>!! in "in-param")
  (<!! out))

;   define a function that takes as input the total number of messages that can be processed,
;   and returns two unbuffered channels, input and output. Each message placed in the input channel
;   must be returned by the output channel, until the maximum message limit has been reached.
(defn get-message
  [message-count]
  (let [in (chan)
        out (chan)]
    (go (loop [mc message-count]
          (if (> mc 0)
            (let [input (<! in)]
              (if (= "get" input)
                (do (>! out (str "msg: " mc))
                    (recur (dec mc)))
                (do (>! out "unknown command")
                    (recur mc))))
            (do (close! in)
                (close! out)))))
    [in out]))

(let [[in out] (get-message 3)]
  (>!! in "put")
  (println (<!! out))
  (>!! in "get")
  (println (<!! out))
  (>!! in "get")
  (println (<!! out))

  (>!! in "get")
  (println (<!! out))
  (>!! in "get")
  (println (<!! out))

  (>!! in "get")
  (println (<!! out)))

;   reverse the string "ESACREWOL" and change it from uppercase to lowercase, using tree different channels:
;  the first: takes the input string, changes it from uppercase to lowercase, puts it on the second channel
;  the second: takes the input from the first channel, reverses it, puts it on the third channel
;  the third: takes the input from the second channel, prints it.
(let [c1 (chan)
      c2 (chan)
      c3 (chan)]
  (go (>! c2 (clojure.string/lower-case (<! c1))))
  (go (>! c3 (clojure.string/reverse (<! c2))))
  (go (println (<! c3)))
  (>!! c1 "ESACREWOL"))

(defn upper-caser
  [in]
  (let [out (chan)]
    (go (while true (>! out (clojure.string/upper-case (<! in)))))
    out))

; define a function that takes a company name and a channel as parameter,
; queries for the company invoices, and puts the result on the given channel
(defn find-invoices-by-name
  [name channel]
  (go (>! channel (d/q '[:find ?company-name ?company-invoices
                         :in $ ?search-name
                         :where
                         [?company :company/name ?search-name]
                         [?company :company/name ?company-name]
                         [?company :company/invoices ?company-invoices]] db name))))



; define a function that takes three different company name as input,
; and for each of them uses the previous function to query the db, using a different channel for each company name.
; return the result of the first channel that finishes, then the second then the third, with a timeout of
; 1000 ms.

;1
(defn find-companies-invoices
  [[first second third]]
  (let [c1 (chan)
        c2 (chan)
        c3 (chan)]
    (find-invoices-by-name first c1)
    (find-invoices-by-name second c2)
    (find-invoices-by-name third c3)
    (println (concat (seq(let [[val _] (alts!! [c1 c2 c3 (timeout 1000)])] val))
                     (seq(let [[val _] (alts!! [c1 c2 c3 (timeout 1000)])] val))
                     (seq (let [[val _] (alts!! [c1 c2 c3 (timeout 1000)])] val))))))

(find-companies-invoices ["Microsoft" "Google" "Facebook"])

;2
(defn search [[first second third]]
  (let [c1 (chan 10)
        c2 (chan 10)
        c3 (chan 10)
        channels [c1 c2 c3 (timeout 1000)]]
    (go (>! c1 (<! (find-invoices-by-name first c1))))
    (go (>! c2 (<! (find-invoices-by-name second c2))))
    (go (>! c3 (<! (find-invoices-by-name third c3))))
    (go (loop [i 0 ret []]
          (if (= i 3)
            ret
            (recur (inc i) (conj ret (alt! [c1 c2 c3] ([v] v)))))))))

(<!! (search ["Microsoft" "Google" "Facebook"]))

;3
(let [c1 (chan(sliding-buffer 20))]
  (go(println (<!! c1)))
  (map (fn [[k v]] (find-invoices-by-name k v)) {"Microsoft" c1}))

;4
(let [c1 (chan(sliding-buffer 20))]
  (go(while true(println (<!! c1))))
  (find-invoices-by-name "Microsoft" c1))




