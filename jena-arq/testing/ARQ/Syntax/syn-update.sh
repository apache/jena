#!/bin/bash
# SPARQL Update (Recommendation) syntax examples

function fname
{
    R="$1"
    N="$2"
    E="$3"
    [ "$E" = "" ] && E="ru"
    echo $(printf "$R%02d.$E" $N)
}

## Structure
N=0

N=$((N+1)) ; testGood $SPARQL11U $(fname "syntax-update-" $N) <<EOF
BASE <http://example/base#>
PREFIX : <http://example/>
LOAD <http://example.org/faraway>
EOF

N=$((N+1)) ; testGood $SPARQL11U $(fname "syntax-update-" $N) <<EOF
# Comment
BASE <http://example/base#>
# Comment
PREFIX : <http://example/>
# Comment
LOAD <http://example.org/faraway>
# Comment
EOF

## ---- LOAD

N=$((N+1)) ; testGood $SPARQL11U $(fname "syntax-update-" $N) <<EOF
LOAD <http://example.org/faraway> ;
EOF

N=$((N+1)) ; testGood $SPARQL11U $(fname "syntax-update-" $N) <<EOF
LOAD <http://example.org/faraway> INTO GRAPH <localCopy>
EOF

## ---- DROP
N=$((N+1)) ; testGood $SPARQL11U $(fname "syntax-update-" $N) <<EOF
DROP NAMED
EOF

N=$((N+1)) ; testGood $SPARQL11U $(fname "syntax-update-" $N) <<EOF
DROP DEFAULT
EOF

N=$((N+1)) ; testGood $SPARQL11U $(fname "syntax-update-" $N) <<EOF
DROP ALL
EOF

N=$((N+1)) ; testGood $SPARQL11U $(fname "syntax-update-" $N) <<EOF
DROP GRAPH <graph>
EOF

N=$((N+1)) ; testGood $SPARQL11U $(fname "syntax-update-" $N) <<EOF
DROP SILENT NAMED
EOF

N=$((N+1)) ; testGood $SPARQL11U $(fname "syntax-update-" $N) <<EOF
DROP SILENT DEFAULT
EOF

N=$((N+1)) ; testGood $SPARQL11U $(fname "syntax-update-" $N) <<EOF
DROP SILENT ALL
EOF

N=$((N+1)) ; testGood $SPARQL11U $(fname "syntax-update-" $N) <<EOF
DROP SILENT GRAPH <graph>
EOF

# CREATE
N=$((N+1)) ; testGood $SPARQL11U $(fname "syntax-update-" $N) <<EOF
CREATE GRAPH <graph>
EOF

N=$((N+1)) ; testGood $SPARQL11U $(fname "syntax-update-" $N) <<EOF
CREATE SILENT GRAPH <graph>
EOF

# CLEAR
N=$((N+1)) ; testGood $SPARQL11U $(fname "syntax-update-" $N) <<EOF
CLEAR NAMED
EOF

N=$((N+1)) ; testGood $SPARQL11U $(fname "syntax-update-" $N) <<EOF
CLEAR DEFAULT
EOF

N=$((N+1)) ; testGood $SPARQL11U $(fname "syntax-update-" $N) <<EOF
CLEAR ALL
EOF

N=$((N+1)) ; testGood $SPARQL11U $(fname "syntax-update-" $N) <<EOF
CLEAR GRAPH <graph>
EOF

N=$((N+1)) ; testGood $SPARQL11U $(fname "syntax-update-" $N) <<EOF
CLEAR SILENT NAMED
EOF

N=$((N+1)) ; testGood $SPARQL11U $(fname "syntax-update-" $N) <<EOF
CLEAR SILENT DEFAULT
EOF

N=$((N+1)) ; testGood $SPARQL11U $(fname "syntax-update-" $N) <<EOF
CLEAR SILENT ALL
EOF

N=$((N+1)) ; testGood $SPARQL11U $(fname "syntax-update-" $N) <<EOF
CLEAR SILENT GRAPH <graph>
EOF

## ---- INSERT DATA
N=$((N+1)) ; testGood $SPARQL11U $(fname "syntax-update-" $N) <<EOF
INSERT DATA { <s> <p> 'o1', 'o2', 'o3' }
EOF

N=$((N+1)) ; testGood $SPARQL11U $(fname "syntax-update-" $N) <<EOF
INSERT DATA { GRAPH <G> { <s> <p> 'o1', 'o2', 'o3' } }
EOF

N=$((N+1)) ; testGood $SPARQL11U $(fname "syntax-update-" $N) <<EOF
INSERT DATA { 
  <s1> <p1> <o1>
  GRAPH <G> { <s> <p1> 'o1'; <p2> <o2> } 
  GRAPH <G1> { <s> <p1> 'o1'; <p2> <o2> } 
  <s1> <p1> <o1>
}
EOF


N=$((N+1)) ; testGood $SPARQL11U $(fname "syntax-update-" $N) <<EOF
INSERT 
# Comment
DATA { GRAPH <G> { <s> <p> 'o1', 'o2', 'o3' } }
EOF

N=$((N+1)) ; testGood $SPARQL11U $(fname "syntax-update-" $N) <<EOF
INSERT 
DATA { }
EOF

N=$((N+1)) ; testGood $SPARQL11U $(fname "syntax-update-" $N) <<EOF
INSERT 
DATA {  GRAPH <G>{} }
EOF

## ---- DELETE DATA
N=$((N+1)) ; testGood $SPARQL11U $(fname "syntax-update-" $N) <<EOF
DELETE DATA { <s> <p> 'o1', 'o2', 'o3' }
EOF

N=$((N+1)) ; testGood $SPARQL11U $(fname "syntax-update-" $N) <<EOF
DELETE DATA { GRAPH <G> { <s> <p> 'o1', 'o2', 'o3' } }
EOF

N=$((N+1)) ; testGood $SPARQL11U $(fname "syntax-update-" $N) <<EOF
DELETE DATA { 
  <s1> <p1> <o1>
  GRAPH <G> { <s> <p1> 'o1'; <p2> <o2> } 
  GRAPH <G1> { <s> <p1> 'o1'; <p2> <o2> } 
  <s1> <p1> <o1>
}
EOF

## ---- Full modify form.
N=$((N+1)) ; testGood $SPARQL11U $(fname "syntax-update-" $N) <<EOF
BASE    <base:>
PREFIX  :     <http://example/>

WITH :g
DELETE {
  <s> ?p ?o .
}
INSERT {
  ?s ?p <#o> .
}
USING <base:g1>
USING <base:g2>
USING NAMED :gn1
USING NAMED :gn2
WHERE
  { ?s ?p ?o }
EOF


N=$((N+1)) ; testGood $SPARQL11U $(fname "syntax-update-" $N) <<EOF
PREFIX  :     <http://example/>
WITH :g
DELETE {
  <base:s> ?p ?o .
}
WHERE
  { ?s ?p ?o }
EOF

N=$((N+1)) ; testGood $SPARQL11U $(fname "syntax-update-" $N) <<EOF
PREFIX  :     <http://example/>
WITH :g
INSERT {
  <base:s> ?p ?o .
}
WHERE
  { ?s ?p ?o }
EOF

## ---- DELETE WHERE

N=$((N+1)) ; testGood $SPARQL11U $(fname "syntax-update-" $N) <<EOF
DELETE WHERE { ?s ?p ?o }
EOF

N=$((N+1)) ; testGood $SPARQL11U $(fname "syntax-update-" $N) <<EOF
# Comment
DELETE 
# Comment
WHERE 
# Comment
{ GRAPH <G> { <s> <p> 123 ; <q> 4567.0 . } }
EOF

# ---- Compound
N=$((N+1)) ; testGood $SPARQL11U $(fname "syntax-update-" $N) <<EOF
CREATE GRAPH <g> ;
LOAD <remote> INTO GRAPH <g> ;
EOF

# ---- Empty 

N=$((N+1)) ; testGood $SPARQL11U $(fname "syntax-update-" $N) <<EOF
# Empty
EOF

N=$((N+1)) ; testGood $SPARQL11U $(fname "syntax-update-" $N) <<EOF
BASE <http://example/>
# Otherwise empty
EOF
N=$((N+1)) ; testGood $SPARQL11U $(fname "syntax-update-" $N) <<EOF
PREFIX : <http://example/>
# Otherwise empty
EOF

# Bad syntax
N=0

N=$((N+1)) ; testBad $SPARQL11U $(fname "syntax-update-bad-" $N) <<EOF
# No URL
LOAD ;
EOF

N=$((N+1)) ; testBad $SPARQL11U $(fname "syntax-update-bad-" $N) <<EOF
# Typo in keyword.
CREATE DEAFULT
EOF

N=$((N+1)) ; testBad $SPARQL11U $(fname "syntax-update-bad-" $N) <<EOF
# Variable in data.
DELETE DATA { ?s <p> <o> }
EOF

N=$((N+1)) ; testBad $SPARQL11U $(fname "syntax-update-bad-" $N) <<EOF
# Variable in data.
INSERT DATA { GRAPH ?g {<s> <p> <o> } }
EOF

N=$((N+1)) ; testBad $SPARQL11U $(fname "syntax-update-bad-" $N) <<EOF
# Nested GRAPH
DELETE DATA { 
  GRAPH <G> { 
    <s> <p> <o> .
    GRAPH <G1> { <s> <p1> 'o1' }
  }
}
EOF

N=$((N+1)) ; testBad $SPARQL11U $(fname "syntax-update-bad-" $N) <<EOF
# Missing template
INSERT WHERE { ?s ?p ?o }
EOF

N=$((N+1)) ; testBad $SPARQL11U $(fname "syntax-update-bad-" $N) <<EOF
# No separator
CREATE GRAPH <g>
LOAD <remote> INTO GRAPH <g>
EOF

N=$((N+1)) ; testBad $SPARQL11U $(fname "syntax-update-bad-" $N) <<EOF
# Too many separators
CREATE GRAPH <g>
;;
LOAD <remote> INTO GRAPH <g>
EOF

# Too many separators
N=$((N+1)) ; testBad $SPARQL11U $(fname "syntax-update-bad-" $N) <<EOF
CREATE GRAPH <g>
;
LOAD <remote> INTO GRAPH <g>
;;
EOF

# Bnodes in DELETE
N=$((N+1)) ; testBad $SPARQL11U $(fname "syntax-update-bad-" $N) <<EOF
# BNode in DELETE WHERE
DELETE WHERE { _:a <p> <o> }
EOF

N=$((N+1)) ; testBad $SPARQL11U $(fname "syntax-update-bad-" $N) <<EOF
# BNode in DELETE template
DELETE { <s> <p> [] } WHERE { ?x <p> <o> }
EOF

N=$((N+1)) ; testBad $SPARQL11U $(fname "syntax-update-bad-" $N) <<EOF
# BNode in DELETE DATA
DELETE DATA { _:a <p> <o> }
EOF
