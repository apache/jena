#!/bin/bash

## ---- LATERAL

N=0

N=$((N+1)) ; testGood $ARQ $(fname "syntax-lateral-" $N arq) <<EOF
SELECT * { LATERAL {} }
EOF

N=$((N+1)) ; testGood $ARQ $(fname "syntax-lateral-" $N arq) <<EOF
SELECT * { 
  ?s ?p ?o 
  LATERAL { SELECT ?x { ?s ?q ?x } LIMIT 2 }
}
EOF

N=$((N+1)) ; testGood $ARQ $(fname "syntax-lateral-" $N arq) <<EOF
SELECT * { LATERAL {} }
EOF

N=$((N+1)) ; testGood $ARQ $(fname "syntax-lateral-" $N arq) <<EOF
SELECT * { 
  ?s ?p ?o 
  LATERAL { OPTIONAL { ?s ?q ?x } }
}
EOF

## Scoping rules.
N=$((N+1)) ; testGood $ARQ $(fname "syntax-lateral-" $N arq) <<EOF
SELECT * { ?s ?p ?o LATERAL { SELECT ?s  { BIND(123 AS ?o) } } }
EOF

## Scoping rules.

N=0

N=$((N+1)) ; testBad $ARQ $(fname "syntax-lateral-bad-" $N arq) <<EOF
SELECT * { LATERAL }
EOF

N=$((N+1)) ; testBad $ARQ $(fname "syntax-lateral-bad-" $N arq) <<EOF
SELECT * { LATERAL OPTIONAL { ?s ?p ?o } }
EOF

N=$((N+1)) ; testBad $ARQ $(fname "syntax-lateral-bad-" $N arq) <<EOF
SELECT * { OPTIONAL LATERAL { ?s ?p ?o } }
EOF

N=$((N+1)) ; testBad $ARQ $(fname "syntax-lateral-bad-" $N arq) <<EOF
SELECT * {
   ?s ?p ?o
   LATERAL { BIND( 123 AS ?o) }
}
EOF

N=$((N+1)) ; testBad $ARQ $(fname "syntax-lateral-bad-" $N arq) <<EOF
SELECT * {
   ?s ?p ?o
   LATERAL { OPTIONAL { BIND( 123 AS ?o) } }
}
EOF

N=$((N+1)) ; testBad $ARQ $(fname "syntax-lateral-bad-" $N arq) <<EOF
SELECT * {
   ?s ?p ?o
   LATERAL { ?s ?p ?o VALUES ?o {123 456} }
}
EOF

N=$((N+1)) ; testBad $ARQ $(fname "syntax-lateral-bad-" $N arq) <<EOF
SELECT * {
   ?s ?p ?o
   LATERAL { SELECT (123 As ?o) {} }
}
EOF

N=$((N+1)) ; testBad $ARQ $(fname "syntax-lateral-bad-" $N arq) <<EOF
SELECT * { ?s ?p ?o LATERAL { SELECT * { BIND(123 AS ?o) } } }
EOF
