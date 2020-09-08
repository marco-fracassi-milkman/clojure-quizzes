(require '[clojure.core.async :as async :refer :all])

; define an unbuffered channel
(def unbuffered (chan))


; define a bufferred channel
(def buffered (chan 10))


; define an unbuffered channel, print the body you've put in it, then `close!` the channel.

(def unbuffered (chan))
; subscriber
(go (println (<! unbuffered)))
; writer
(>!! unbuffered "Message!")
(close! unbuffered)

; define a function which returns two unbuffered channels: input and output.
; The latter returns the input of the former.

(def input (chan))
(def output (chan))

; pipeline of subscribers
(go (>! output (<! input)))
(go (println (<! output)))
; issuer
(>!! input "My message!")
(close! input)
(close! output)



;   define a function that takes as input the total number of messages that can be processed,
;   and returns two unbuffered channels, input and output. Each message placed in the input channel
;   must be returned by the output channel, until the maximum message limit has been reached.

(defn pass-through [in, out]
  (go (>! out (<! in)))
  (go (println (<! out)))
)

(defn close-channels [in, out]
  (close! in)
  (close! out)
)

(defn channels [max]
      (let [ in (chan),  out (chan) ]
        ; define a behaviour on the channels
        (loop [m max]
           (if (> m 0)
               (pass-through in out )
               (close-channels in out )
           )
           (recur  (dec m))
        )
        ; return the channels in output
        [in, out]
      )
)


(defn channels [max]
  (let [ in (chan),  out (chan) ]
    ; define a behaviour on the channels
    (go
      (loop [m max]
        (if (> m 0)
          (pass-through in out )
          (close-channels in out )
        )
        (recur  (dec m))
      )
    )
    ; return the channels in output
    [in, out]
  )
)


(def chnls (channels 3) )
(def input-channel (first chnls))
(>!! input-channel "First message")
(>!! input-channel "Second message")
(>!! input-channel "Third message")
(>!! input-channel "Fourth message")


(defn get-message
  [message-count]
  (let [in (chan)  out (chan)]
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

;   reverse the string "ESACREWOL" and change it from uppercase to lowercase, using tree different channels:
;  the first: takes the input string, changes it from uppercase to lowercase, puts it on the second channel
;  the second: takes the input from the first channel, reverses it, puts it on the third channel
;  the third: takes the input from the second channel, prints it.

(defn lowercase-it [in, out]
  (go (>! out (clojure.string/lower-case (<! in)) ))
)

(defn reverse-it [in, out]
  (go (>! out (clojure.string/reverse (<! in)) ))
)

(defn print-it [out]
  (go (println (<! out)))
)



(defn lower-and-reverse [word]
  (let [c1 (chan), c2 (chan), c3 (chan)]
    (lowercase-it c1, c2)
    (reverse-it c2, c3)
    (print-it c3)
    (>!! c1 word)
  )
)

;; THE SAME
(defn lower-and-reverse [word]
  (let [c1 (chan), c2 (chan), c3 (chan)]
     (do
         (lowercase-it c1, c2)
         (reverse-it c2, c3)
         (print-it c3)
         (>!! c1 word)
     )
  )
)

; define a function that takes a company name and a channel as parameter,
; queries for the company invoices, and puts the result on the given channel

(def invoices [
                { :name "Milkman"   :invoices ["12322", "434332", "554545"] }
                { :name "Mitobit"   :invoices ["54432", "543422", "666754"] }
                { :name "Byte-Code" :invoices ["82726", "543366", "642111"] }
              ]
)


(defn invoice-query [name channel]
  (filter-key)
   (:invoices (= name (:name )) )
)


; define a function that takes three different company name as input,
; and for each of them uses the previous function to query the db, using a different channel for each company name.
; return the result of the first channel that finishes, then the second then the third, with a timeout of
; 1000 ms.


( -> (str "ciao" " a " "tutti ")
     thread
     (->> (def mythread) )
)

; channel
(def mychan1 (chan 3))
; subscriber
(def read-message (go (<!! mychan1)))                       ; non si blocca
; issuer
(>!! mychan1 "Questo e' un messaggio" )
; read-message ha come valore un go/thread quindi restituisce un canale che si chiude appena viene eseguito il body
; tale body viene eseguito al primo
(<!! read-message)
=> "Questo e' un messaggio"
(<!! read-message)
=> nil

(go (println (<! channy)))

(def messages (atom []))
(def channy (chan 5))
(go (swap! messages conj (<! channy)))
(>!! channy "first message")
(>!! channy "second message")
(>!! channy "third message")

@messages
=> ["first message"]

