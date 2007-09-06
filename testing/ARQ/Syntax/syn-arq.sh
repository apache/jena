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
SELECT (:function(?x +?y)) ?z {}
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

