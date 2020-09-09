(ns quiz.exceptions
  (:gen-class))

(defn catchException [exception]
  (let [result (atom "")]
    (try
      (exception)
      (catch ArithmeticException e (reset! result "FOO"))
      (catch Exception e (reset! result "OK"))
      (finally (reset! result (str @result "finally")))
    )
    (println @result)
    @result
  )
)

; catch multiple exceptions

; catch multiple exception with a finally block

