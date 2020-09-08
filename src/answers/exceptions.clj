; catch a generic exception
(defn Example []
  (try
    (def string1 (slurp "Example.txt"))
    (println string1)
    (catch Exception e (println (str "ex: " (.getMessage e))))))
(Example)

; catch multiple exceptions
(defn Example []
  (try
    (def string1 (slurp "Example.txt"))
    (println string1)

    (catch java.io.FileNotFoundException e (println (str "caught ex: " (.getMessage e))))

    (catch Exception e (println (str "caught ex: " (.getMessage e)))))
  (println "over"))
(Example)

; catch multiple exception with a finally block
(defn Example []
  (try
    (def string1 (slurp "Example.txt"))
    (println string1)

    (catch java.io.FileNotFoundException e (println (str "caught ex: " (.getMessage e))))

    (catch Exception e (println (str "caught ex: " (.getMessage e))))
    (finally (println "final block")))
  (println "over"))
(Example)