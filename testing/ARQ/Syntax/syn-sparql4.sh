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
EOF---- 

N=$((N+1)) ; testBad $(fname "syn-bad-" $N) <<EOF
SELECT COUNT(*) {}
EOF
