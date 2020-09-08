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
(def db (d/db conn-db))

;given the schema above:

; transact two different companies whitout any invoice


; transact two invoices whithout company



; transact a company with two invoices


; transact another company with two invoices, one of them wihtout amount


; transact another company with two invoices but whithout address




; find all the companies with invoices (in both ways: query and pull)




; find all the companies with their related invoices (in both ways: query and pull)



; count the invoices of each company




; find the 'median' invoice-amount of each company




; find the 'avg' invoice-amount of each company




;find the 'stddev' invoice amount of each company




;   find the companies with their name, invoice code and amount of their related invoices




;6  find the sum of the amount of all invoices of a company given the name




;  define a rule extract invoices by company name




; define a rule to extract all the companies with address




;  define a rule to extract company by invoice




; define a rule to extract only companies without the given invoice code




;  count all the companies




; find all adresses without duplicates




;  find the invoice with the highest amount



; find all schema names




;find the avg length of a schema name



; count all the attributes and datatypes in the schema



; define a function than use it in a query to sum all the amounts of a given company



; find all the companies with address, if a company is missing the address
; return the name of the company and "NO ADDRESS"



; find all the companies whitout address
;missing




; create and transact a database function that makes invoices,
; then retrieve the function and use it to transact a new invoice for an existing company.




; given the id of a company, 'touch' all the attributes of the entity




; define a function that returns a database instance given a point in time




; define a function that returns all the attributes af an entity given its id and a point in time




; define a function that returns a database instance since a given a point in time




; define a function that returns all the attributes af an entity given its id and a point in time using since




; define a function that changes the address of a company and returns its name and the new address




; define a function that shows all the addresses that a company had during its history





; change the address of a company then get the instance of the db after the transaction and return
; company name and address





; Using: tx-report-queue  ->  https://docs.datomic.com/on-prem/clojure/index.html#datomic.api/tx-report-queue
;                         ->  https://blog.datomic.com/2013/10/the-transaction-report-queue.html
; using a tx-report-queue,
;   define a watcher on the connection in a separate thread,
;   change the value of a company address in db,
;   take the tx-data from tx-report-queue and query on the db instances to show the values of the address property before and after

