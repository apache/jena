(import s2j)

;; Notes:
;; Calls to static would work as (out <ResultSetFormatter> rs)
;; according to the documentation but I seem to need java-null
;; (out (java-null <ResultSetFormatter>) rs)

(java-class '|java.lang.String|)

(define-java-classes
  (<jstring>      |java.lang.String|)
  (<QueryFactory> |com.hp.hpl.jena.query.QueryFactory|)
  (<QueryExecutionFactory> |com.hp.hpl.jena.query.QueryExecutionFactory|)
  (<ResultSetFormatter> |com.hp.hpl.jena.query.ResultSetFormatter|)
  (<Query> |com.hp.hpl.jena.query.Query|)
  )

(define str (->jstring "SELECT * FROM <file:D.ttl> { ?s ?p ?o }"))

(define-generic-java-method jToString |toString|)
(define-generic-java-method jCreate |create|)
(define-generic-java-method execSelect |execSelect|)
(define-generic-java-method out |out|)

;; Query query = QueryFactory.create(queryString) ;
(define query (jCreate (java-null <QueryFactory>) str))

;; QueryExecution qexec = QueryExecutionFactory.create(query) ;
(define qExec (jCreate (java-null <QueryExecutionFactory>) query))

(define rs (execSelect qExec))

(define (next? iter)
  (->boolean 
   ((generic-java-method '|hasNext|) iter)))

(define (x iter)
  (cond ( (not (next? iter)) '() )
	(else
	 (let ((b ((generic-java-method '|nextBinding|) iter)))
	   (write (->string (jToString b)))
	   (newline)
	   (x iter)
	  ))))

(x rs)
(newline)

;; And again, then use the resultSetFormatter.
(set! qExec (jCreate (java-null <QueryExecutionFactory>) query))
(set! rs (execSelect qExec))
(out (java-null <ResultSetFormatter>) rs)
;;(out <ResultSetFormatter> rs)
