#!/bin/bash

## ==== Good
N=0

## ---- Expressions in SELECT

N=0

N=$((N+1)) ; testGood $(fname "syntax-select-expr-" $N arq) <<EOF
SELECT (?x +?y AS ?z) {}
EOF

N=$((N+1)) ; testGood $(fname "syntax-select-expr-" $N arq) <<EOF
SELECT ?x ?y (?x +?y AS ?z) {}
EOF

N=$((N+1)) ; testGood $(fname "syntax-select-expr-" $N arq) <<EOF
SELECT (datatype(?x +?y) AS ?z) {}
EOF

N=$((N+1)) ; testGood $(fname "syntax-select-expr-" $N arq) <<EOF
PREFIX : <http://example/>
SELECT (:function(?x +?y) AS ?F) ?z {}
EOF

N=$((N+1)) ; testGood $(fname "syntax-select-expr-" $N arq) <<EOF
PREFIX : <http://example/>
SELECT (COUNT(*) AS ?count) {}
EOF

## ---- Aggregates
N=0

N=$((N+1)) ; testGood $(fname "syntax-aggregate-" $N arq) <<EOF
SELECT (COUNT(*) AS ?count) {}
EOF

N=$((N+1)) ; testGood $(fname "syntax-aggregate-" $N arq) <<EOF
SELECT (COUNT(DISTINCT *) AS ?count) {}
EOF

N=$((N+1)) ; testGood $(fname "syntax-aggregate-" $N arq) <<EOF
SELECT (COUNT(?x) AS ?count) {}
EOF

N=$((N+1)) ; testGood $(fname "syntax-aggregate-" $N arq) <<EOF
SELECT (COUNT(DISTINCT ?x) AS ?count) {}
EOF

N=$((N+1)) ; testGood $(fname "syntax-aggregate-" $N arq) <<EOF
SELECT (SUM(?x) AS ?y) {}
EOF

N=$((N+1)) ; testGood $(fname "syntax-aggregate-" $N arq) <<EOF
SELECT (SUM(DISTINCT ?x) AS ?y) {}
EOF

N=$((N+1)) ; testGood $(fname "syntax-aggregate-" $N arq) <<EOF
SELECT (MIN(?x) AS ?y) {}
EOF

N=$((N+1)) ; testGood $(fname "syntax-aggregate-" $N arq) <<EOF
SELECT (MIN(DISTINCT ?x) AS ?y) {}
EOF

N=$((N+1)) ; testGood $(fname "syntax-aggregate-" $N arq) <<EOF
SELECT (MAX(?x) AS ?y) {}
EOF

N=$((N+1)) ; testGood $(fname "syntax-aggregate-" $N arq) <<EOF
SELECT (MAX(DISTINCT ?x) AS ?y) {}
EOF

N=$((N+1)) ; testGood $(fname "syntax-aggregate-" $N arq) <<EOF
SELECT (AVG(?x) AS ?y) {}
EOF

N=$((N+1)) ; testGood $(fname "syntax-aggregate-" $N arq) <<EOF
SELECT (AVG(DISTINCT ?x) AS ?y) {}
EOF

N=$((N+1)) ; testGood $(fname "syntax-aggregate-" $N arq) <<EOF
SELECT (GROUP_CONCAT(?x) AS ?y) {}
EOF

N=$((N+1)) ; testGood $(fname "syntax-aggregate-" $N arq) <<EOF
SELECT (GROUP_CONCAT(DISTINCT ?x) AS ?y) {}
EOF

N=$((N+1)) ; testGood $(fname "syntax-aggregate-" $N arq) <<EOF
SELECT (GROUP_CONCAT(?x; SEPARATOR=';') AS ?y) {}
EOF

## ---- Subquery

## ---- Negation: NOT EXISTS

## ---- Negation: MINUS

## Not in SPARQL 1.0
## ---- IN
## ---- UNION without left {}

## ---- SERVICE

## ---- BINDING

## == Bad
N=0

N=$((N+1)) ; testBad $(fname "syn-bad-" $N) <<EOF
SELECT (?x +?y) {}
EOF

N=$((N+1)) ; testBad $(fname "syn-bad-" $N) <<EOF
SELECT COUNT(*) {}
EOF

N=$((N+1)) ; testBad $(fname "syn-bad-" $N) <<EOF
SELECT (SUM(?x,?y) AS ?S) {}
EOF