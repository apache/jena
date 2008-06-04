#!/bin/bash

## ---- Expressions in SELECT

N=0

N=$((N+1)) ; testGood $(fname "syntax-select-expr-" $N arq) <<EOF
SELECT (?x +?y AS ?z) {}
EOF

N=$((N+1)) ; testGood $(fname "syntax-select-expr-" $N arq) <<EOF
SELECT (?x +?y) {}
EOF

N=$((N+1)) ; testGood $(fname "syntax-select-expr-" $N arq) <<EOF
SELECT ?x ?y (?x +?y AS ?z) {}
EOF

N=$((N+1)) ; testGood $(fname "syntax-select-expr-" $N arq) <<EOF
SELECT (datatype(?x +?y) AS ?z) {}
EOF

N=$((N+1)) ; testGood $(fname "syntax-select-expr-" $N arq) <<EOF
PREFIX : <http://example/>
SELECT :function(?x +?y) ?z {}
EOF

N=$((N+1)) ; testGood $(fname "syntax-select-expr-" $N arq) <<EOF
PREFIX : <http://example/>
SELECT (:function(?x +?y)) ?z {}
EOF

N=$((N+1)) ; testGood $(fname "syntax-select-expr-" $N arq) <<EOF
PREFIX : <http://example/>
SELECT str(?z) ?z {}
EOF

## ---- SERVICE

N=0

N=$((N+1)) ; testGood $(fname "syntax-service-" $N arq) <<EOF
PREFIX : <http://example/>
SELECT *
{ SERVICE <http://host/service> {} }
EOF

## ---- GROUP BY

N=0

N=$((N+1)) ; testGood $(fname "syntax-group-" $N arq) <<EOF
PREFIX : <http://example/>
SELECT *
{ ?x :p ?p .}
GROUP BY ?p
EOF

N=$((N+1)) ; testGood $(fname "syntax-group-" $N arq) <<EOF
PREFIX : <http://example/>

SELECT * { ?x :p ?p .}
GROUP BY ?p ?q (?p*?q)
HAVING (?p*?q > 1)
EOF

N=$((N+1)) ; testGood $(fname "syntax-group-" $N arq) <<EOF
PREFIX : <http://example/>

SELECT * { ?x :p ?p .}
GROUP BY ?p ?q (?p*?q AS ?z)
EOF

N=$((N+1)) ; testGood $(fname "syntax-group-" $N arq) <<EOF
PREFIX : <http://example/>

SELECT * { ?x :p ?p .}
GROUP BY ?p ?q str(?p)
EOF


## ---- COUNT

N=0

N=$((N+1)) ; testGood $(fname "syntax-count-" $N arq) <<EOF
PREFIX : <http://example/>

SELECT count(*) { ?x :p ?p .}
EOF

N=$((N+1)) ; testGood $(fname "syntax-count-" $N arq) <<EOF
PREFIX : <http://example/>

SELECT (count(distinct *) AS ?count) { ?x :p ?p .}
EOF

N=$((N+1)) ; testGood $(fname "syntax-count-" $N arq) <<EOF
PREFIX : <http://example/>

SELECT (count(?x) AS ?count) { ?x :p ?p .}
EOF

N=$((N+1)) ; testGood $(fname "syntax-count-" $N arq) <<EOF
PREFIX : <http://example/>

SELECT (COUNT(DISTINCT ?x) AS ?count) { ?x :p ?p .}
EOF


## ---- { SELECT }
N=0
N=$((N+1)) ; testGood $(fname "syntax-subquery-" $N arq) <<EOF
PREFIX : <http://example/>

SELECT *
{ SELECT * { ?x ?y ?z } }
EOF

N=$((N+1)) ; testGood $(fname "syntax-subquery-" $N arq) <<EOF
PREFIX : <http://example/>

SELECT *
{ ?s ?p ?o
  { SELECT * { ?x ?y ?z } }
}
EOF

N=$((N+1)) ; testGood $(fname "syntax-subquery-" $N arq) <<EOF
PREFIX : <http://example/>

SELECT *
{ ?s ?p ?o
  { SELECT count( distinct * ) { ?x :p ?z } ORDER BY ?z LIMIT 5 OFFSET 6 }
}
EOF

N=$((N+1)) ; testGood $(fname "syntax-subquery-" $N arq) <<EOF
PREFIX : <http://example/>

SELECT *
{ ?s ?p ?o
  OPTIONAL { SELECT * { ?a ?b ?c } }
}
EOF

N=0
N=$((N+1)) ; testBad $(fname "syntax-subquery-bad-" $N arq) <<EOF
PREFIX : <http://example/>

SELECT *
  SELECT * { ?x ?y ?z }
EOF

N=$((N+1)) ; testBad $(fname "syntax-subquery-bad-" $N arq) <<EOF
PREFIX : <http://example/>

SELECT *
{
  ASK { ?x ?y ?z } 
}
EOF

## ---- LET

N=0
N=$((N+1)) ; testGood $(fname "syntax-let-" $N arq) <<EOF
PREFIX : <http://example/>

SELECT * { LET ( ?x := 3 ) }
EOF

N=$((N+1)) ; testGood $(fname "syntax-let-" $N arq) <<EOF
PREFIX : <http://example/>

SELECT * { ?s ?p ?o . OPTIONAL { ?o :p ?q LET ( ?q := true ) } }
EOF

N=0
N=$((N+1)) ; testBad $(fname "syntax-let-bad-" $N arq) <<EOF
PREFIX : <http://example/>

SELECT * 
{
  LET ?x := (4+5)
}
EOF