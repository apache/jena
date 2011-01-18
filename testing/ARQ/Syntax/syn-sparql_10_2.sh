#!/bin/bash
# Query syntax examples
# More tests


function fname
{
    R="$1"
    N="$2"
    E="$3"
    [ "$E" = "" ] && E="rq"
    echo $(printf "$R%02d.$E" $N)
}



# Not in original set of tests

N=0
N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-general-" $N) <<EOF
SELECT * WHERE { <a><b><c> }
EOF

N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-general-" $N) <<EOF
SELECT * WHERE { <a><b>_:x }
EOF

# Signed numbers

N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-general-" $N) <<EOF
SELECT * WHERE { <a><b>1 }
EOF

N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-general-" $N) <<EOF
SELECT * WHERE { <a><b>+11 }
EOF

N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-general-" $N) <<EOF
SELECT * WHERE { <a><b>-1 }
EOF

N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-general-" $N) <<EOF
SELECT * WHERE { <a><b>1.0 }
EOF

N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-general-" $N) <<EOF
SELECT * WHERE { <a><b>+1.0 }
EOF

N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-general-" $N) <<EOF
SELECT * WHERE { <a><b>-1.0 }
EOF

N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-general-" $N) <<EOF
SELECT * WHERE { <a><b>1.0e0 }
EOF

N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-general-" $N) <<EOF
SELECT * WHERE { <a><b>+1.0e+1 }
EOF

N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-general-" $N) <<EOF
SELECT * WHERE { <a><b>-1.0e-1 }
EOF

N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-general-" $N) <<EOF
# Legal, if unusual, IRIs
SELECT * WHERE { <a> <b> <?z> }
EOF

N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-general-" $N) <<EOF
# Legal, if unusual, IRIs
BASE <http://example/page.html>
SELECT * WHERE { <a> <b> <#x> }
EOF

N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-general-" $N) <<EOF
# Legal, if unusual, IRIs
BASE <http://example/page.html?query>
SELECT * WHERE { <a> <b> <&param=value> }
EOF




# Keywords and qnames.
N=0
N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-keywords-" $N) <<EOF
# use keyword FILTER as a namespace prefix
PREFIX FILTER: <http://example.org/ns#> 
SELECT *
WHERE { ?x FILTER:foo ?z FILTER (?z) }
EOF

N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-keywords-" $N) <<EOF
# use keyword FILTER as a local name
PREFIX : <http://example.org/ns#> 
SELECT *
WHERE { ?x :FILTER ?z FILTER (?z) }
EOF

N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-keywords-" $N) <<EOF
# use keyword UNION as a namespace prefix
PREFIX UNION: <http://example.org/ns#> 
SELECT *
WHERE { ?x UNION:foo ?z }
EOF

## More on lists
# Checking white space in () and []
N=0
N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-lists-" $N) <<EOF
PREFIX : <http://example.org/>
SELECT * WHERE { () :p 1 }
EOF

N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-lists-" $N) <<EOF
PREFIX : <http://example.org/>
SELECT * WHERE { ( ) :p 1 }
EOF

N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-lists-" $N) <<EOF
PREFIX : <http://example.org/>
SELECT * WHERE { ( 
) :p 1 }
EOF

N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-lists-" $N) <<EOF
PREFIX : <http://example.org/>
SELECT * WHERE { ( 1 2
) :p 1 }
EOF

N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-lists-" $N) <<EOF
PREFIX : <http://example.org/>
SELECT * WHERE { ( 1 2
) }
EOF


## Blank node 
N=0
N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-bnode-" $N) <<EOF
PREFIX : <http://example.org/>
SELECT * WHERE { [] :p [] }
EOF

N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-bnode-" $N) <<EOF
PREFIX : <http://example.org/>
# Tab
SELECT * WHERE { [ ] :p [
	] }
EOF

N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-bnode-" $N) <<EOF
PREFIX : <http://example.org/>
SELECT * WHERE { [ :p :q 
 ] }
EOF

# Function calls
N=0
N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-function-" $N) <<EOF
PREFIX q: <http://example.org/>
SELECT * WHERE { FILTER (q:name()) }
EOF

N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-function-" $N) <<EOF
PREFIX q: <http://example.org/>
SELECT * WHERE { FILTER (q:name( )) }
EOF

N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-function-" $N) <<EOF
PREFIX q: <http://example.org/>
SELECT * WHERE { FILTER (q:name(
)) }
EOF

N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-function-" $N) <<EOF
PREFIX q: <http://example.org/>
SELECT * WHERE { FILTER (q:name(1
)) . FILTER (q:name(1,2)) . FILTER (q:name(1
,2))}
EOF


## Result forms
## Select
N=0

N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-form-select-" $N) <<EOF
SELECT * WHERE { }
EOF

N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-form-select-" $N) <<EOF
SELECT * { }
EOF

## Ask
N=0

## N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-form-ask-" $N) <<EOF
## ASK {}
## EOF
N=$((N+1))

N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-form-ask-" $N) <<EOF
ASK {}
EOF

## Construct
N=0

N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-form-construct" $N) <<EOF
CONSTRUCT { ?s ?p ?o . } WHERE {?s ?p ?o}
EOF

N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-form-construct" $N) <<EOF
CONSTRUCT { ?s ?p ?o } WHERE {?s ?p ?o}
EOF

N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-form-construct" $N) <<EOF
CONSTRUCT { ?s <p1> <o> . ?s <p2> ?o } WHERE {?s ?p ?o}
EOF

N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-form-construct" $N) <<EOF
CONSTRUCT { ?s <p1> <o> . ?s <p2> ?o .} WHERE {?s ?p ?o}
EOF

N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-form-construct" $N) <<EOF
PREFIX  rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
CONSTRUCT { [] rdf:subject ?s ;
               rdf:predicate ?p ;
               rdf:object ?o }
WHERE {?s ?p ?o}
EOF

N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-form-construct" $N) <<EOF
PREFIX  rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
CONSTRUCT { [] rdf:subject ?s ;
               rdf:predicate ?p ;
               rdf:object ?o . }
WHERE {?s ?p ?o}
EOF

N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-form-construct" $N) <<EOF
CONSTRUCT {} WHERE {}
EOF

# Describe
N=0
N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-form-describe" $N) <<EOF
DESCRIBE <u>
EOF

N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-form-describe" $N) <<EOF
DESCRIBE <u> ?u WHERE { <x> <q> ?u . }
EOF


## Dataset description
N=0

N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-dataset-" $N) <<EOF
PREFIX : <http://example.org/>
SELECT ?x
FROM <http://example.org/graph>
WHERE {}
EOF

N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-dataset-" $N) <<EOF
PREFIX : <http://example.org/>
SELECT ?x
FROM NAMED <http://example.org/graph1>
WHERE {}
EOF

N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-dataset-" $N) <<EOF
PREFIX : <http://example.org/>
SELECT ?x
FROM NAMED :graph1
FROM NAMED :graph2
WHERE {}
EOF

N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-dataset-" $N) <<EOF
PREFIX : <http://example.org/>
SELECT ?x
FROM :g1
FROM :g2
FROM NAMED :graph1
FROM NAMED :graph2
WHERE {}
EOF



## Dataset access
N=0

N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-graph-" $N) <<EOF
PREFIX : <http://example.org/>
SELECT *
WHERE
{
  GRAPH ?g { } 
}
EOF

# Now bad.
## N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-graph-" $N) <<EOF
## PREFIX : <http://example.org/>
## SELECT *
## WHERE
## {
##   GRAPH [] { } 
## }
## EOF

N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-graph-" $N) <<EOF
PREFIX : <http://example.org/>
SELECT *
WHERE
{
  GRAPH :a { } 
}
EOF

N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-graph-" $N) <<EOF
PREFIX : <http://example.org/>
SELECT *
WHERE
{
  GRAPH ?g { :x :b ?a } 
}
EOF

N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-graph-" $N) <<EOF
PREFIX : <http://example.org/>
SELECT *
WHERE
{
  :x :p :z
  GRAPH ?g { :x :b ?a } 
}
EOF

N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-graph-" $N) <<EOF
PREFIX : <http://example.org/>
SELECT *
WHERE
{
  :x :p :z
  GRAPH ?g { :x :b ?a . GRAPH ?g2 { :x :p ?x } }
}
EOF

## Escapes
N=0

N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-esc-" $N) <<EOF
SELECT *
WHERE { <x> <p> "\t" }
EOF

N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-esc-" $N) <<EOF
SELECT *
WHERE { <x> <p> "x\t" }
EOF

N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-esc-" $N) <<EOF
SELECT *
WHERE { <x> <p> "\tx" }
EOF

# Escpes in URIs, qnames and variables

N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-esc-" $N) <<EOF
PREFIX : <http://example/> 
SELECT *
WHERE { <\u0078> :\u0070 ?xx\u0078 }
EOF

N=$((N+1)) ; testGood $SPARQL10 $(fname "syntax-esc-" $N) <<EOF
PREFIX : <http://example/> 
SELECT *
# Comments can contain \ u
# <\u0078> :\u0070 ?xx\u0078
WHERE { <\u0078> :\u0070 ?xx\u0078 }
EOF

