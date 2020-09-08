(require '[clojure.core.async :as async :refer :all])

; define an unbuffered channel


; define a bufferred channel


; define an unbuffered channel, print the body you've put in it, then `close!` the channel.


; define a function which returns two unbuffered channels: input and output.
; The latter returns the input of the former.


;   define a function that takes as input the total number of messages that can be processed,
;   and returns two unbuffered channels, input and output. Each message placed in the input channel
;   must be returned by the output channel, until the maximum message limit has been reached.



;   reverse the string "ESACREWOL" and change it from uppercase to lowercase, using tree different channels:
;  the first: takes the input string, changes it from uppercase to lowercase, puts it on the second channel
;  the second: takes the input from the first channel, reverses it, puts it on the third channel
;  the third: takes the input from the second channel, prints it.



; define a function that takes a company name and a channel as parameter,
; queries for the company invoices, and puts the result on the given channel


; define a function that takes three different company name as input,
; and for each of them uses the previous function to query the db, using a different channel for each company name.
; return the result of the first channel that finishes, then the second then the third, with a timeout of
; 1000 ms.

