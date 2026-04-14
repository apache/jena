#!/bin/bash

## ---- ARQ tests

N=0

N=$((N+1)) ; testBad $ARQ $(fname "syntax-scope-bad-" $N arq) <<EOF
SELECT ( (?x+1) AS ?x ) {}
EOF

N=$((N+1)) ; testBad $ARQ $(fname "syntax-scope-bad-" $N arq) <<EOF
SELECT ( (?x+1) AS ?y)  (2 AS ?x) {}
EOF

## ---- Expressions in SELECT

N=0

N=$((N+1)) ; testGood $ARQ $(fname "syntax-select-expr-" $N arq) <<EOF
SELECT (?x +?y AS ?z) {}
EOF

N=$((N+1)) ; testGood $ARQ $(fname "syntax-select-expr-" $N arq) <<EOF
SELECT (?x +?y) {}
EOF

N=$((N+1)) ; testGood $ARQ $(fname "syntax-select-expr-" $N arq) <<EOF
SELECT ?x ?y (?x +?y AS ?z) {}
EOF

N=$((N+1)) ; testGood $ARQ $(fname "syntax-select-expr-" $N arq) <<EOF
SELECT (datatype(?x +?y) AS ?z) {}
EOF

N=$((N+1)) ; testGood $ARQ $(fname "syntax-select-expr-" $N arq) <<EOF
PREFIX : <http://example/>
SELECT :function(?x +?y) ?z {}
EOF

N=$((N+1)) ; testGood $ARQ $(fname "syntax-select-expr-" $N arq) <<EOF
PREFIX : <http://example/>
SELECT (:function(?x +?y)) ?z {}
EOF

N=$((N+1)) ; testGood $ARQ $(fname "syntax-select-expr-" $N arq) <<EOF
PREFIX : <http://example/>
SELECT str(?z) ?z {}
EOF
## ---- Nested aggregates

N=0

N=$((N+1)) ; testBad $ARQ $(fname "syntax-agg-expr-bad-" $N arq) <<EOF
PREFIX : <http://example/>
SELECT (SUM(COUNT(*)) AS ?Z) {}
EOF

N=$((N+1)) ; testBad $ARQ $(fname "syntax-agg-expr-bad-" $N arq) <<EOF
PREFIX : <http://example/>
SELECT ?Z {} HAVING (COUNT(AVG(?x)) > 0)
EOF

N=$((N+1)) ; testBad $ARQ $(fname "syntax-agg-expr-bad-" $N arq) <<EOF
PREFIX : <http://example/>
SELECT ?Z {} ORDER BY COUNT(AVG(?x))
EOF


## ---- SERVICE

N=0

N=$((N+1)) ; testGood $ARQ $(fname "syntax-service-" $N arq) <<EOF
PREFIX : <http://example/>
SELECT *
{ SERVICE <http://host/service> {} }
EOF

## ---- GROUP BY

N=0

N=$((N+1)) ; testGood $ARQ $(fname "syntax-group-" $N arq) <<EOF
PREFIX : <http://example/>
SELECT *
{ ?x :p ?p .}
GROUP BY ?p
EOF

N=$((N+1)) ; testGood $ARQ $(fname "syntax-group-" $N arq) <<EOF
PREFIX : <http://example/>

SELECT * { ?x :p ?p .}
GROUP BY ?p ?q (?p*?q)
HAVING (?p*?q > 1)
EOF

N=$((N+1)) ; testGood $ARQ $(fname "syntax-group-" $N arq) <<EOF
PREFIX : <http://example/>

SELECT * { ?x :p ?p .}
GROUP BY ?p ?q (?p*?q AS ?z)
EOF

N=$((N+1)) ; testGood $ARQ $(fname "syntax-group-" $N arq) <<EOF
PREFIX : <http://example/>

SELECT * { ?x :p ?p .}
GROUP BY ?p ?q str(?p)
EOF


## ---- COUNT

N=0

N=$((N+1)) ; testGood $ARQ $(fname "syntax-count-" $N arq) <<EOF
PREFIX : <http://example/>

SELECT count(*) { ?x :p ?p .}
EOF

N=$((N+1)) ; testGood $ARQ $(fname "syntax-count-" $N arq) <<EOF
PREFIX : <http://example/>

SELECT (count(distinct *) AS ?count) { ?x :p ?p .}
EOF

N=$((N+1)) ; testGood $ARQ $(fname "syntax-count-" $N arq) <<EOF
PREFIX : <http://example/>

SELECT (count(?x) AS ?count) { ?x :p ?p .}
EOF

N=$((N+1)) ; testGood $ARQ $(fname "syntax-count-" $N arq) <<EOF
PREFIX : <http://example/>

SELECT (COUNT(DISTINCT ?x) AS ?count) { ?x :p ?p .}
EOF


## ---- { SELECT }
N=0
N=$((N+1)) ; testGood $ARQ $(fname "syntax-subquery-" $N arq) <<EOF
PREFIX : <http://example/>

SELECT *
{ SELECT * { ?x ?y ?z } }
EOF

N=$((N+1)) ; testGood $ARQ $(fname "syntax-subquery-" $N arq) <<EOF
PREFIX : <http://example/>

SELECT *
{ ?s ?p ?o
  { SELECT * { ?x ?y ?z } }
}
EOF

N=$((N+1)) ; testGood $ARQ $(fname "syntax-subquery-" $N arq) <<EOF
PREFIX : <http://example/>

SELECT *
{ ?s ?p ?o
  { SELECT count( distinct * ) { ?x :p ?z } ORDER BY ?z LIMIT 5 OFFSET 6 }
}
EOF

N=$((N+1)) ; testGood $ARQ $(fname "syntax-subquery-" $N arq) <<EOF
PREFIX : <http://example/>

SELECT *
{ ?s ?p ?o
  OPTIONAL { SELECT * { ?a ?b ?c } }
}
EOF

N=0
N=$((N+1)) ; testBad $ARQ $(fname "syntax-subquery-bad-" $N arq) <<EOF
PREFIX : <http://example/>

SELECT *
  SELECT * { ?x ?y ?z }
EOF

N=$((N+1)) ; testBad $ARQ $(fname "syntax-subquery-bad-" $N arq) <<EOF
PREFIX : <http://example/>

SELECT *
{
  ASK { ?x ?y ?z } 
}
EOF

## ---- LET

N=0
N=$((N+1)) ; testGood $ARQ $(fname "syntax-let-" $N arq) <<EOF
PREFIX : <http://example/>

SELECT * { LET ( ?x := 3 ) }
EOF

N=$((N+1)) ; testGood $ARQ $(fname "syntax-let-" $N arq) <<EOF
PREFIX : <http://example/>

SELECT * { ?s ?p ?o . OPTIONAL { ?o :p ?q LET ( ?q := true ) } }
EOF

N=0
N=$((N+1)) ; testBad $ARQ $(fname "syntax-let-bad-" $N arq) <<EOF
PREFIX : <http://example/>

SELECT * 
{
  LET ?x := (4+5)
}
EOF

## ---- CONSTRUCT QUAD

N=0

N=$((N+1)) ; testGood $ARQ $(fname "syntax-quad-construct-" $N arq) <<EOF
PREFIX : <http://example/>

CONSTRUCT { GRAPH :g { :s :p :o } } WHERE {}
EOF

N=$((N+1)) ; testGood $ARQ $(fname "syntax-quad-construct-" $N arq) <<EOF
PREFIX : <http://example/>

CONSTRUCT { GRAPH ?g { ?s ?p ?o } } WHERE { ?s ?p ?o }
EOF

N=$((N+1)) ; testGood $ARQ $(fname "syntax-quad-construct-" $N arq) <<EOF
PREFIX : <http://example/>

CONSTRUCT { :s :p :o } WHERE {}
EOF

N=$((N+1)) ; testGood $ARQ $(fname "syntax-quad-construct-" $N arq) <<EOF
PREFIX : <http://example/>

CONSTRUCT {
   GRAPH ?g { :s :p :o }
   ?s ?p ?o
   }
WHERE
   { GRAPH ?g { ?s ?p ?o } }
EOF

N=$((N+1)) ; testGood $ARQ $(fname "syntax-quad-construct-" $N arq) <<EOF
PREFIX : <http://example/>

CONSTRUCT {
   ?s ?p ?o
   GRAPH ?g { :s :p :o }
   }
WHERE
   { GRAPH ?g { ?s ?p ?o } }
EOF

N=$((N+1)) ; testGood $ARQ $(fname "syntax-quad-construct-" $N arq) <<EOF
PREFIX : <http://example/>

CONSTRUCT {
   GRAPH ?g { :s :p :o }
   ?s ?p ?o .
   ?s ?p ?o .
   GRAPH ?g { ?s ?p ?o }
   ?s ?p ?o .
   ?s ?p ?o
   GRAPH ?g { ?s ?p ?o }
   }
WHERE
   { GRAPH ?g { ?s ?p ?o } }
EOF

N=$((N+1)) ; testGood $ARQ $(fname "syntax-quad-construct-" $N arq) <<EOF
PREFIX : <http://example/>

CONSTRUCT {
   GRAPH <urn:x-arq:DefaultGraphNode> {:s :p :o .}
   }
WHERE {}
EOF

N=$((N+1)) ; testGood $ARQ $(fname "syntax-quad-construct-" $N arq) <<EOF
PREFIX : <http://example/>

CONSTRUCT {
   GRAPH ?g { :s :p :o }
   GRAPH ?g1 { :s :p :o }
   }
WHERE
   { }
EOF

N=$((N+1)) ; testGood $ARQ $(fname "syntax-quad-construct-" $N arq) <<EOF
PREFIX : <http://example/>

CONSTRUCT {
    { ?s ?p ?o }
   }
WHERE
   { }
EOF

N=$((N+1)) ; testGood $ARQ $(fname "syntax-quad-construct-" $N arq) <<EOF
PREFIX : <http://example/>

CONSTRUCT 
WHERE
   { 
     ?s ?p ?o
   }
EOF

N=$((N+1)) ; testGood $ARQ $(fname "syntax-quad-construct-" $N arq) <<EOF
PREFIX : <http://example/>

CONSTRUCT 
WHERE
   { 
     GRAPH ?g { ?s ?p ?o }
   }
EOF

N=$((N+1)) ; testGood $ARQ $(fname "syntax-quad-construct-" $N arq) <<EOF
PREFIX : <http://example/>

CONSTRUCT 
WHERE
   { 
     { ?s ?p ?o }
   }
EOF

N=0
N=$((N+1)) ; testBad $ARQ $(fname "syntax-quad-construct-bad-" $N arq) <<EOF
PREFIX : <http://example/>

CONSTRUCT 
WHERE
   { 
     GRAPH ?g { ?s ?p ?o. FILTER isIRI(?o) }
   }
EOF

#median
N=0
N=$((N+1)) ; testGood $ARQ $(fname "syntax-median-" $N arq) <<EOF
PREFIX : <http://example/>

SELECT median(?x)
WHERE
   {
     VALUES ?x { 1 2 3 4 5 }
     ?s ?p ?x.
   }
EOF

# Delimited escape sequences \{H..H}
##     @Test public void unescape_unicode_20()   { test_unesc_unicode("\\u{41}", "A") ; }

##     @Test public void unescape_unicode_21()   { test_unesc_unicode("\\u{000000}", "\u0000") ; }
##     @Test public void unescape_unicode_22()   { test_unesc_unicode("\\u{1F0A1}", "🂡") ; }
##     @Test public void unescape_unicode_23()   { test_unesc_unicode("\\u{01F0A1}", "🂡") ; }
##     @Test public void unescape_unicode_24()   { test_unesc_unicode("\\u{10FFFF}", 0x10FFFF) ; }

N=0
N=$((N+1)) ; testGood $ARQ $(fname "syntax-delim-hex-escape-" $N arq) <<EOF
PREFIX : <http://example/>
ASK { :s :p "\\u{41}" }
EOF

N=$((N+1)) ; testGood $ARQ $(fname "syntax-delim-hex-escape-" $N arq) <<EOF
PREFIX : <http://example/>
ASK { :s :p "\\u{0}" }
EOF

N=$((N+1)) ; testGood $ARQ $(fname "syntax-delim-hex-escape-" $N arq) <<EOF
PREFIX : <http://example/>
ASK { :s :p "\\u{1F0A1}" }
EOF

N=$((N+1)) ; testGood $ARQ $(fname "syntax-delim-hex-escape-" $N arq) <<EOF
PREFIX : <http://example/>
ASK { :s :p "\\u{01F0A1}" }
EOF

N=$((N+1)) ; testGood $ARQ $(fname "syntax-delim-hex-escape-" $N arq) <<EOF
PREFIX : <http://example/>
ASK { :s :p "\\u{10FFFF}" }
EOF

N=0
N=$((N+1)) ; testBad $ARQ $(fname "syntax-delim-hex-escape-bad-" $N arq) <<EOF
PREFIX : <http://example/>
ASK { :s :p "\\u{}" }
EOF

N=$((N+1)) ; testBad $ARQ $(fname "syntax-delim-hex-escape-bad-" $N arq) <<EOF
PREFIX : <http://example/>
ASK { :s :p "\\u{12345678}" }
EOF

N=$((N+1)) ; testBad $ARQ $(fname "syntax-delim-hex-escape-bad-" $N arq) <<EOF
PREFIX : <http://example/>
ASK { :s :p "\\u{00000000}" }
EOF

N=$((N+1)) ; testBad $ARQ $(fname "syntax-delim-hex-escape-bad-" $N arq) <<EOF
PREFIX : <http://example/>
ASK { :s :p "\\u{1234567}" }
EOF
