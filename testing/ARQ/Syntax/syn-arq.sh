#!/bin/bash

## Expressions in SELECT
N=0

N=$((N+1)) ; testGood $(fname "syntax-select-expr-" $N arq) <<EOF
SELECT (?x +?y) AS ?z {}
EOF

N=$((N+1)) ; testGood $(fname "syntax-select-expr-" $N arq) <<EOF
SELECT (?x +?y) {}
EOF

N=$((N+1)) ; testGood $(fname "syntax-select-expr-" $N arq) <<EOF
SELECT ?x ?y (?x +?y) AS ?z {}
EOF

N=$((N+1)) ; testGood $(fname "syntax-select-expr-" $N arq) <<EOF
SELECT datatype(?x +?y) AS ?z {}
EOF

N=$((N+1)) ; testGood $(fname "syntax-select-expr-" $N arq) <<EOF
PREFIX : <http://example/>
SELECT :function(?x +?y) {}
EOF
