; catch a generic exception
(try
  (/ 1 0)
  (catch Exception e (str "caught exception: " (.getMessage e))))

(try
  (/ 1 0)
  (catch Exception e (str "caught exception: " (. e getMessage))))

; catch multiple exceptions


(try
  (/ 1 0)
  (catch java.lang.ArithmeticException e1 (str "caught aritmetic exception: " (. e1 getMessage)))
  (catch Exception e2 (str "caught generic exception: " (. e2 getMessage)))
)

; catch multiple exception with a finally block
(try
  (/ 1 0)
  (catch java.lang.ArithmeticException e1 (str "caught aritmetic exception: " (. e1 getMessage)))
  (catch Exception e2 (str "caught generic exception: " (. e2 getMessage)))
  (finally (prn "finally block."))
)

