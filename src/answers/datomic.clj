(require
  '[datomic.api :as d]
  '[datomic.samples.repl :as repl]
  '[clojure.set :as sets :refer :all]
  '[clojure.data :as datas :refer :all]
  '[clojure.core.async :as async :refer [>! <! >!! <!! go chan
                                         close! alts! alts!! thread timeout
                                         sliding-buffer go-loop dropping-buffer
                                         alt!]])

(doc repl/scratch-conn)

(def conn-db (repl/scratch-conn))

(def invoice-schema [{
                      :db/ident   	:invoice/code
                      :db/valueType   :db.type/string
                      :db/unique :db.unique/identity
                      :db/cardinality :db.cardinality/one
                      }
                     {
                      :db/ident   	:invoice/amount
                      :db/valueType   :db.type/double
                      :db/cardinality :db.cardinality/one
                      }])
(def company-schema [{
                      :db/ident   	:company/name
                      :db/valueType   :db.type/string
                      :db/unique :db.unique/identity
                      :db/fulltext true
                      :db/cardinality :db.cardinality/one
                      }
                     {
                      :db/ident   	:company/address
                      :db/valueType   :db.type/string
                      :db/cardinality :db.cardinality/one
                      }
                     {
                      :db/ident   	:company/bank_account
                      :db/valueType   :db.type/string
                      :db/cardinality :db.cardinality/one
                      }
                     {
                      :db/ident   	:company/aggregated_amount
                      :db/valueType   :db.type/double
                      :db/cardinality :db.cardinality/one
                      }
                     {
                      :db/ident   	:company/invoices
                      :db/valueType   :db.type/ref
                      :db/cardinality :db.cardinality/many
                      :db/isComponent true
                      }])

@(d/transact conn-db invoice-schema)
@(d/transact conn-db company-schema)

;given the schema above:

; transact two different companies whitout any invoice
@(d/transact conn-db [{:company/name "Milkman S.p.A." :company/address "Via Germania 12" :company/bank_account "IT0002"}])
@(d/transact conn-db [{:company/name "Accenture" :company/bank_account "IT000258987"}])

; transact two invoices whithout company
@(d/transact conn-db [{:invoice/code "I001" :invoice/amount (double 15000)}])


; transact a company with two invoices
@(d/transact conn-db [{:company/name  "Milkman-Services S.R.L." :company/address "Via Germania 12" :company/bank_account "IT0004"
                       :company/invoices [{:invoice/code "I002" :invoice/amount (double 16040)}{:invoice/code "I003" :invoice/amount (double 12354)}{:invoice/code "I103" :invoice/amount (double 87551)}]
                       }])

; transact another company with two invoices, one of them wihtout amount
@(d/transact conn-db [{:company/name  "Microsoft" :company/address "Via Germania 13" :company/bank_account "IT0003"
                       :company/invoices [{:invoice/code "I006" :invoice/amount (double 5556)} {:invoice/code "I007" }]
                       }])

; transact another company with two invoices but whithout address
@(d/transact conn-db [{:company/name  "Google" :company/address "Mountain View, California" :company/bank_account "IT0005"
                       :company/invoices [{:invoice/code "I099" :invoice/amount (double 2344)} {:invoice/code "I088" :invoice/amount (double 89706)}]
                       }])

@(d/transact conn-db [{:company/name  "Facebook" :company/address "Menlo Park, California" :company/bank_account "IT00567"
                       :company/invoices [{:invoice/code "I076" :invoice/amount (double 3457)} {:invoice/code "I034" :invoice/amount (double 76797)}]
                       }])

(def db (d/db conn-db))




; find all the companies with invoices (in both ways: query and pull)
(d/q '[:find ?company-name :where
       [?company :company/invoices]
       [?company :company/name ?company-name]] db)

(d/q '[:find (pull ?company pattern)
       :in $ pattern
       :where [?company :company/invoices]] db   [:company/name])



; find all the companies with their related invoices (in both ways: query and pull)
(d/q '[:find ?company-name ?company-invoice :where
       [?company :company/invoices ?company-invoice]
       [?company :company/name ?company-name]] db)

(d/q '[:find (pull ?company pattern)
       :in $ pattern
       :where [?company :company/invoices]] db   [:company/name :company/invoices])


; count the invoices of each company
(d/q '[:find ?company-name (count ?company-invoices)
       :where
       [?company :company/invoices ?company-invoices]
       [?company :company/name ?company-name]] db)



; find the 'median' invoice-amount of each company
(d/q '[:find ?company-name (median ?invoice-amount)
       :with ?company
       :where
       [?company :company/invoices ?invoice]
       [?invoice :invoice/amount ?invoice-amount]
       [?company :company/name ?company-name]] db)



; find the 'avg' invoice-amount of each company
(d/q '[:find ?company-name (avg ?invoice-amount)
       :with ?company
       :where
       [?company :company/invoices ?invoice]
       [?invoice :invoice/amount ?invoice-amount]
       [?company :company/name ?company-name]] db)



;find the 'stddev' invoice amount of each company
(d/q '[:find ?company-name (stddev ?invoice-amount)
       :with ?company
       :where
       [?company :company/invoices ?invoice]
       [?invoice :invoice/amount ?invoice-amount]
       [?company :company/name ?company-name]] db)



;   find the companies with their name, invoice code and amount of their related invoices
(d/q '[:find ?company-name ?invoice-code ?invoice-amount
       :where
       [?company :company/invoices ?invoice]
       [?invoice :invoice/code ?invoice-code]
       [?invoice :invoice/amount ?invoice-amount]
       [?company :company/name ?company-name]] db)



;6  find the sum of the amount of all invoices of a company given the name
(def rule6 '[[(company-with-amount ?amount-gt ?company-name ?amount)
              [?company :company/invoices ?invoice]
              [?company :company/name ?company-name]
              [?invoice :invoice/amount ?amount]
              [(> ?amount ?amount-gt)]]])

(d/q '[
       :find ?company-name (sum ?amount)
       :in $ % ?amount-gt ?company-name
       :where (company-with-amount ?amount-gt ?company-name ?amount)
       ] db rule6 6000 "Microsoft")



;  define a rule extract invoices by company name
(def rule7 '[[(invoices-by-name ?name ?company-name ?invoice-code)
              [?company :company/name ?name]
              [?company :company/invoices ?invoice]
              [?invoice :invoice/code ?invoice-code]
              [?company :company/name ?company-name]]])

(d/q '[:find ?company-name ?company-invoices
       :in $ % ?name
       :where
       (invoices-by-name ?name ?company-name ?company-invoices)] db rule7 "Milkman-Services S.R.L.")



; define a rule to extract all the companies with address
(def rule8 '[[(company-with-address ?company-name ?company-address )
              [(get-else $ ?company :company/address "NO ADDRESS") ?company-address]
              [?company :company/name ?company-name]]])

(d/q '[:find ?company-name ?company-address
       :in $ %
       :where (company-with-address ?company-name ?company-address)] db rule8)



;  define a rule to extract company by invoice
(def rule9 '[[(company-by-invoice ?inv-code ?company-name ?invoice-amount)
              [?company :company/invoices ?invoice]
              [?invoice :invoice/code ?inv-code]
              [?invoice :invoice/amount ?invoice-amount]
              [?company :company/name ?company-name]]])

(d/q '[:find ?company-name ?invoice-amount
       :in $ % ?inv-code
       :where (company-by-invoice ?inv-code ?company-name ?invoice-amount)] db rule9 "I103")



; define a rule to extract only companies without the given invoice code
(def rule10 '[[(exclude-company-by-invoice-code ?inv-code ?company-name)
               [?company :company/name ?company-name]
               (not-join [?company]
                         [?company :company/invoices ?invoice]
                         [?invoice :invoice/code ?inv-code])]])

(d/q '[:find ?company-name
       :in $ % ?inv-code
       :where (exclude-company-by-invoice-code ?inv-code ?company-name)] db rule10 "I006")



;  count all the companies
(d/q '[:find (count ?company-name)
       :with ?company
       :where
       [?company :company/name ?company-name]] db)



; find all adresses without duplicates
(d/q '[:find (distinct ?company-address) .
       :where
       [?company :company/address ?company-address]] db)



;  find the invoice with the highest amount
(d/q '[:find  (max ?invoice-amount)
       :where
       [?company :company/invoices ?invoice]
       [?invoice :invoice/amount ?invoice-amount]] db)


; find all schema names
(d/q '[:find ?name
       :with ?e
       :where
       [?e :db/ident ?ident]
       [(name ?ident) ?name]]
     db)



;find the avg length of a schema name
(d/q '[:find (avg ?length) .
       :with ?e
       :where
       [?e :db/ident ?ident]
       [(name ?ident) ?name]
       [(count ?name) ?length]]
     db)


; count all the attributes and datatypes in the schema
(d/q '[:find  (count ?a) (count-distinct ?vt)
       :where
       [?a :db/ident ?ident]
       [?a :db/valueType ?vt]]
     db)


; define a function than use it in a query to sum all the amounts of a given company
(defn get-revenues
  [amounts]
  (reduce + amounts))

(d/q '[:find (user/get-revenues ?invoice-amount) .
       :with ?company
       :where [?company :company/invoices ?invoice]
       [?invoice :invoice/amount ?invoice-amount]] db)


; find all the companies with address, if a company is missing the address
; return the name of the company and "NO ADDRESS"
(d/q '[:find ?company-name ?company-town
       :where [?company :company/name ?company-name]
       [(get-else $ ?company :company/address "NO ADDRESS") ?company-town]] db)



; find all the companies whitout address
;missing
(d/q '[:find ?company-name
       :where
       [(missing? $ ?company :company/address)]
       [?company :company/name ?company-name]] db)



; create and transact a database function that makes invoices,
; then retrieve the function and use it to transact a new invoice for an existing company.
(def fixed_cost_invoice2
  #db/fn {:lang :clojure
          :params [code]
          :code {:invoice/code code :invoice/amount (double 45000)}})

@(d/transact
   conn-db
   [{:db/id (d/tempid :db.part/user)
     :db/doc "Funzione per generazione fattura a costo fisso - Spesa ricorrente affitto annuo"
     :db/ident :create_fixed_cost_invoice2
     :db/fn fixed_cost_invoice2}])

(def db (d/db conn-db))
(def fn_create_fixed_cost_invoice2 (d/entity db :create_fixed_cost_invoice2))

@(d/transact conn-db [{:company/name  "Reply SpA" :company/address "Via Milano 77" :company/bank_account "IT00000098"
                       :company/invoices ((:db/fn fn_create_fixed_cost_invoice2) "IST77777777")}])

;----
(def insert-invoice
  #db/fn {:lang :clojure
          :params [db id code amount]
          :code (when-not (seq (d/q '[:find ?e
                                      :in $ ?code
                                      :where [?e :invoice/code ?code]]
                                    db code))
                  {:db/id id
                   :invoice/code code
                   :invoice/amount amount})})

(insert-invoice db
                (d/tempid :db.part/user)
                "I999"
                (double 5500))

;
(def db-with-invoice
  (:db-after (d/with db [(insert-invoice db
                                         (d/tempid :db.part/user)
                                         "I998"
                                         (double 5500))])))

(d/q '[:find ?invoice-code
       :where
       [?invoice :invoice/code ?invoice-code]]
     db-with-invoice)



; given the id of a company, 'touch' all the attributes of the entity
(d/touch (d/entity db 17592186045432))



; define a function that returns a database instance given a point in time
(def db_yesterday (d/as-of (d/db conn-db) #inst "2019-09-06T09:30:00"))



; define a function that returns all the attributes af an entity given its id and a point in time
(d/touch (d/entity (d/as-of (d/db conn-db) #inst "2019-09-06T09:30:00") 17592186045432))



; define a function that returns a database instance since a given a point in time
(def sinceOf (d/since (d/db conn-db) #inst "2019-09-06T09:30:00"))



; define a function that returns all the attributes af an entity given its id and a point in time using since
(d/touch (d/entity (d/since (d/db conn-db) #inst "2019-09-06T09:30:00") 17592186045432))



; define a function that changes the address of a company and returns its name and the new address
(let [db (d/db conn-db)
      company-name [:company/name "Google"]
      tx-result (deref
                  (d/transact
                    conn-db
                    [[:db/add company-name :company/address "1600 Amphitheatre Parkway\nMountain View, CA 94043"]
                     [:db/add "datomic.tx" :db/doc "correct data entry error"]]))
      db-after (:db-after tx-result)
      address-after (-> (d/pull db-after '[:company-name :company/address] company-name)
                       :company/address)]
  [company-name address-after])



; define a function that shows all the addresses that a company had during its history
(d/q '[:find ?company-name ?company-address
       :in $ ?name
       :where
       [?company :company/name ?name]
       [?company :company/name ?company-name]
       [?company :company/address ?company-address]]
     (d/history db) "Google")




; change the address of a company then get the instance of the db after the transaction and return
; company name and address
(let [db (d/db conn-db)
      company-name [:company/name "Google"]
      tx-result (deref
                  (d/transact
                    conn-db
                    [[:db/add company-name :company/address "1600 Amphitheatre Parkway\nMountain View, CA 94043"]
                     [:db/add "datomic.tx" :db/doc "correct data entry error"]]))
      db-after (:db-after tx-result)
      address-after (-> (d/pull db-after '[:company-name :company/address] company-name)
                       :company/address)]
  [company-name address-after])




; Using: tx-report-queue  ->  https://docs.datomic.com/on-prem/clojure/index.html#datomic.api/tx-report-queue
;                         ->  https://blog.datomic.com/2013/10/the-transaction-report-queue.html
; using a tx-report-queue,
;   define a watcher on the connection in a separate thread,
;   change the value of a company address in db,
;   take the tx-data from tx-report-queue and query on the db instances to show the values of the address property before and after
(let [db (d/db conn-db)
      q (fn [name db]  (d/q '[:find ?company-address :in $ ?name :where [?company :company/name ?name]
                              [?company :company/address ?company-address]] db name))
      print-changes (fn []
                      (let [queue (d/tx-report-queue conn-db)]
                        (loop []
                          (let [tx-result (.take queue)
                                db-before (:db-before tx-result)
                                db-after (:db-after tx-result)]
                            (println (str "db-before result: " (q "Google" db-before)))
                            (println (str "db-after result: " (q "Google" db-after)))))))
      _run-watcher (let [t (Thread. print-changes)]
                     (.start t) t)
      company-name [:company/name "Google"]
      tx-result (deref
                  (d/transact
                    conn-db
                    [[:db/add company-name :company/address "1600 Amphitheatre Parkway\nMountain View, CA 94043"]
                     [:db/add "datomic.tx" :db/doc "correct data entry error"]]))
      db-after (:db-after tx-result)
      address-after (-> (d/pull db-after '[:company-name :company/address] company-name)
                       :company/address)]
  [company-name address-after])
