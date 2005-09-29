#!/bin/bash

## ==== Good
N=0

N=$((N+1)) ; testGood $(fname "syn-" $N) <<EOF
# Dot after triple
SELECT * WHERE
{ ?s ?p ?o . }
EOF

N=$((N+1)) ; testGood $(fname "syn-" $N) <<EOF
# No dot after triple
SELECT * WHERE
{ ?s ?p ?o }
EOF


N=$((N+1)) ; testGood $(fname "syn-" $N) <<EOF
SELECT * WHERE
{ ?s ?p ?o . ?s ?p ?o . }
EOF

N=$((N+1)) ; testGood $(fname "syn-" $N) <<EOF
# No dot
SELECT * WHERE
{ ?s ?p ?o . ?s ?p ?o }
EOF

N=$((N+1)) ; testGood $(fname "syn-" $N) <<EOF
# DOT after non-triples
SELECT * WHERE
{ FILTER (?o>5) . }
EOF

N=$((N+1)) ; testGood $(fname "syn-" $N) <<EOF
# DOT after non-triples
SELECT * WHERE
{ FILTER (?o>5) . ?s ?p ?o }
EOF

N=$((N+1)) ; testGood $(fname "syn-" $N) <<EOF
# Trailing ;
PREFIX :   <http://example/ns#>
SELECT * WHERE
{ :s :p :o ; FILTER(?x) }
EOF


N=$((N+1)) ; testGood $(fname "syn-" $N) <<EOF
# Broken ;
PREFIX :   <http://example/ns#>
SELECT * WHERE
{ :s :p :o ; . }
EOF


## ==== Bad
N=0
N=$((N+1)) ; testBad $(fname "syn-bad-" $N) <<EOF
# More a test that bad syntax tests work!
PREFIX ex:   <http://example/ns#>
SELECT *
EOF

N=$((N+1)) ; testBad $(fname "syn-bad-" $N) <<EOF
# Missing DOT between triples
PREFIX :   <http://example/ns#>
SELECT *
{ :s1 :p1 :o1 :s2 :p2 :o2 . }
EOF

N=$((N+1)) ; testBad $(fname "syn-bad-" $N) <<EOF
# DOT, no triples
SELECT * WHERE
{ . }
EOF

N=$((N+1)) ; testBad $(fname "syn-bad-" $N) <<EOF
# DOT, no triples
SELECT * WHERE
{ . FILTER(?x) }
EOF

N=$((N+1)) ; testBad $(fname "syn-bad-" $N) <<EOF
# Broken ;
SELECT * WHERE
{ ; FILTER(?x) }
EOF

N=$((N+1)) ; testBad $(fname "syn-bad-" $N) <<EOF
# Broken ;
PREFIX :   <http://example/ns#>
SELECT * WHERE
{ :s ; :p :o }
EOF

N=$((N+1)) ; testBad $(fname "syn-bad-" $N) <<EOF
# Broken ;
PREFIX :   <http://example/ns#>
SELECT * WHERE
{ :s :p ; }
EOF

N=$((N+1)) ; testBad $(fname "syn-bad-" $N) <<EOF
# Broken ;
PREFIX :   <http://example/ns#>
SELECT * WHERE
{ :s :p ; FILTER(?x) }
EOF

N=$((N+1)) ; testBad $(fname "syn-bad-" $N) <<EOF
# Broken ;
PREFIX :   <http://example/ns#>
SELECT * WHERE
{ :s :p :o . ;  }
EOF

N=$((N+1)) ; testBad $(fname "syn-bad-" $N) <<EOF
CONSTRUCT 
EOF


## ==== Other bad : from NegativeSyntax/
N=0
testBad "bnode-dot.rq" <<EOF
# NegativeSyntax/bnode-dot.rq
SELECT * WHERE {[] . }
EOF

##--
N=0
N=$((N+1)) ; testBad $(fname "bnodes-missing-pvalues-" $N) <<EOF
# NegativeSyntax/bnodes-missing-pvalues.rq
PREFIX :   <http://example/ns#>
SELECT * WHERE { [,] :p [;] . }
EOF

N=$((N+1)) ; testBad $(fname "bnodes-missing-pvalues-" $N) <<EOF
# NegativeSyntax/bnodes-missing-pvalues-02.rq
SELECT * WHERE {() . [,] . [,;] }
EOF

##--
N=0
N=$((N+1)) ; testBad $(fname "empty-optional-" $N) <<EOF
# NegativeSyntax/empty-optional.rq
SELECT * { OPTIONAL FILTER (?x) }
EOF

N=$((N+1)) ; testBad $(fname "empty-optional-" $N) <<EOF
# NegativeSyntax/empty-optional-02.rq
SELECT * { OPTIONAL GRAPH ?v OPTIONAL FILTER (?x) }
EOF

testBad "filter-missing-parens.rq" <<EOF
# NegativeSyntax/filter-missing-parens.rq
SELECT * { ?s ?p ?o FILTER ?x }
EOF

testBad "lone-list.rq" <<EOF
# NegativeSyntax/lone-list.rq
SELECT * WHERE { () }
EOF

testBad "lone-node.rq" <<EOF
# NegativeSyntax/lone-node.rq
SELECT * WHERE {<a>}
EOF

