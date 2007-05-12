#!/bin/bash
# Query syntax examples

function fname
{
    R="$1"
    N="$2"
    E="$3"
    [ "$E" = "" ] && E="rq"
    echo $(printf "$R%02d.$E" $N)
}

## Basic
N=0

N=$((N+1)) ; testGood $(fname "syntax-basic-" $N) <<EOF
SELECT *
WHERE { }
EOF

N=$((N+1)) ; testGood $(fname "syntax-basic-" $N) <<EOF
SELECT * {}
EOF

N=$((N+1)) ; testGood $(fname "syntax-basic-" $N) <<EOF
# No trailing dot
PREFIX : <http://example.org/ns#> 
SELECT *
WHERE { ?x ?y ?z }
EOF

N=$((N+1)) ; testGood $(fname "syntax-basic-" $N) <<EOF
# With trailing dot
SELECT *
WHERE { ?x ?y ?z . }
EOF

N=$((N+1)) ; testGood $(fname "syntax-basic-" $N) <<EOF
# Two triples : no trailing dot
SELECT *
WHERE { ?x ?y ?z . ?a ?b ?c }
EOF

N=$((N+1)) ; testGood $(fname "syntax-basic-" $N) <<EOF
# Two triples : with trailing dot
SELECT *
WHERE { ?x ?y ?z . ?a ?b ?c . }
EOF


## QNames
N=0

N=$((N+1)) ; testGood $(fname "syntax-qname-" $N) <<EOF
PREFIX : <http://example.org/ns#> 
SELECT *
{ ?x :p ?z }
EOF

N=$((N+1)) ; testGood $(fname "syntax-qname-" $N) <<EOF
PREFIX : <http://example.org/ns#> 
SELECT *
WHERE { :x :p :z . }
EOF

N=$((N+1)) ; testGood $(fname "syntax-qname-" $N) <<EOF
PREFIX : <http://example.org/ns#> 
SELECT *
WHERE { :_1 :p.rdf :z.z . }
EOF

N=$((N+1)) ; testGood $(fname "syntax-qname-" $N) <<EOF
PREFIX :  <http://example.org/ns#> 
PREFIX a: <http://example.org/ns2#> 
SELECT *
WHERE { : a: :a . : : : . }
EOF

N=$((N+1)) ; testGood $(fname "syntax-qname-" $N) <<EOF
PREFIX :  <> 
SELECT *
WHERE { : : : . }
EOF

N=$((N+1)) ; testGood $(fname "syntax-qname-" $N) <<EOF
PREFIX :  <#> 
SELECT *
WHERE { : : : . }
EOF

N=$((N+1)) ; testGood $(fname "syntax-qname-" $N) <<EOF
BASE   <http://example.org/>
PREFIX :  <#> 
SELECT *
WHERE { : : : . }
EOF


N=$((N+1)) ; testGood $(fname "syntax-qname-" $N) <<EOF
BASE   <http://example.org/>
PREFIX :  <#>
PREFIX x.y:  <x#>
SELECT *
WHERE { :a.b  x.y:  : . }
EOF

## OLD - has trailing dots
## BASE   <http://example.org/>
## PREFIX :  <#> 
## PREFIX x.:  <x#> 
## SELECT *
## WHERE { :a.  x.:  : . }
## EOF

## Literals
N=0

N=$((N+1)) ; testGood $(fname "syntax-lit-" $N) <<EOF
BASE   <http://example.org/>
PREFIX :  <#> 
SELECT * WHERE { :x :p "x" }
EOF

N=$((N+1)) ; testGood $(fname "syntax-lit-" $N) <<EOF
BASE   <http://example.org/>
PREFIX :  <#> 
SELECT * WHERE { :x :p 'x' }
EOF

N=$((N+1)) ; testGood $(fname "syntax-lit-" $N) <<EOF
BASE   <http://example.org/>
PREFIX :  <#> 
SELECT * WHERE { :x :p "x\\"y'z" }
EOF

N=$((N+1)) ; testGood $(fname "syntax-lit-" $N) <<EOF
BASE   <http://example.org/>
PREFIX :  <#> 
SELECT * WHERE { :x :p 'x"y\\'z' }
EOF


N=$((N+1)) ; testGood $(fname "syntax-lit-" $N) <<EOF
BASE   <http://example.org/>
PREFIX :  <#> 
SELECT * WHERE { :x :p "x\\"" }
EOF

N=$((N+1)) ; testGood $(fname "syntax-lit-" $N) <<EOF
BASE   <http://example.org/>
PREFIX :  <#> 
SELECT * WHERE { :x :p 'x\\'' }
EOF

N=$((N+1)) ; testGood $(fname "syntax-lit-" $N) <<EOF
BASE   <http://example.org/>
PREFIX :  <#> 
SELECT * WHERE { :x :p 123 }
EOF

N=$((N+1)) ; testGood $(fname "syntax-lit-" $N) <<EOF
BASE   <http://example.org/>
PREFIX :  <#> 
SELECT * WHERE { :x :p 123. . }
EOF

N=$((N+1)) ; testGood $(fname "syntax-lit-" $N) <<EOF
BASE   <http://example.org/>
PREFIX :  <#> 
SELECT * WHERE { :x :p """Long
""
Literal
""" }
EOF

N=$((N+1)) ; testGood $(fname "syntax-lit-" $N) <<EOF
BASE   <http://example.org/>
PREFIX :  <#> 
SELECT * WHERE { :x :p '''Long
'' """
Literal''' }
EOF

N=$((N+1)) ; testGood $(fname "syntax-lit-" $N) <<EOF
BASE   <http://example.org/>
PREFIX :  <#> 
SELECT * WHERE { :x :p """Long""\\"Literal""" }
EOF

N=$((N+1)) ; testGood $(fname "syntax-lit-" $N) <<EOF
BASE   <http://example.org/>
PREFIX :  <#> 
SELECT * WHERE { :x :p '''Long''\\'Literal''' }
EOF

N=$((N+1)) ; testGood $(fname "syntax-lit-" $N) <<EOF
BASE   <http://example.org/>
PREFIX :  <#> 
SELECT * WHERE { :x :p """Long\\"""Literal""" }
EOF

N=$((N+1)) ; testGood $(fname "syntax-lit-" $N) <<EOF
BASE   <http://example.org/>
PREFIX :  <#> 
SELECT * WHERE { :x :p '''Long\\'''Literal''' }
EOF


N=$((N+1)) ; testGood $(fname "syntax-lit-" $N) <<EOF
BASE   <http://example.org/>
PREFIX :  <#> 
SELECT * WHERE { :x :p '''Long '' Literal''' }
EOF

N=$((N+1)) ; testGood $(fname "syntax-lit-" $N) <<EOF
BASE   <http://example.org/>
PREFIX :  <#> 
SELECT * WHERE { :x :p '''Long ' Literal''' }
EOF

# Escapes after quotes

N=$((N+1)) ; testGood $(fname "syntax-lit-" $N) <<EOF
BASE   <http://example.org/>
PREFIX :  <#> 
SELECT * WHERE { :x :p '''Long''\\\\Literal with '\\\\ single quotes ''' }
EOF

N=$((N+1)) ; testGood $(fname "syntax-lit-" $N) <<EOF
BASE   <http://example.org/>
PREFIX :  <#> 
SELECT * WHERE { :x :p """Long "" Literal""" }
EOF

N=$((N+1)) ; testGood $(fname "syntax-lit-" $N) <<EOF
BASE   <http://example.org/>
PREFIX :  <#> 
SELECT * WHERE { :x :p """Long " Literal""" }
EOF

N=$((N+1)) ; testGood $(fname "syntax-lit-" $N) <<EOF
BASE   <http://example.org/>
PREFIX :  <#> 
SELECT * WHERE { :x :p """Long""\\\\Literal with "\\\\ single quotes""" }
EOF

## Structure
N=0

N=$((N+1)) ; testGood $(fname "syntax-struct-" $N) <<EOF
# Operator
PREFIX :  <http://example.org/ns#> 
SELECT *
{ OPTIONAL { } }
EOF

N=$((N+1)) ; testGood $(fname "syntax-struct-" $N) <<EOF
# Operator
PREFIX :  <http://example.org/ns#> 
SELECT *
{ OPTIONAL { :a :b :c } }
EOF

N=$((N+1)) ; testGood $(fname "syntax-struct-" $N) <<EOF
# Triple, no DOT, operator
PREFIX :  <http://example.org/ns#> 
SELECT *
{ :p :q :r OPTIONAL { :a :b :c } }
EOF

# Duplicate - skip
## N=$((N+1)) ; testGood $(fname "syntax-struct-" $N) <<EOF
## # Triple, DOT, operator
## PREFIX :  <http://example.org/ns#> 
## SELECT *
## { :p :q :r . OPTIONAL { :a :b :c } }
## EOF
N=$((N+1))

N=$((N+1)) ; testGood $(fname "syntax-struct-" $N) <<EOF
# Triple, DOT, operator
PREFIX :  <http://example.org/ns#> 
SELECT *
{ :p :q :r . OPTIONAL { :a :b :c } }
EOF

N=$((N+1)) ; testGood $(fname "syntax-struct-" $N) <<EOF
# Triple, DOT, operator, DOT
PREFIX :  <http://example.org/ns#> 
SELECT *
{ :p :q :r . OPTIONAL { :a :b :c } . }
EOF


N=$((N+1)) ; testGood $(fname "syntax-struct-" $N) <<EOF
# Operator, no DOT
PREFIX :  <http://example.org/ns#> 
SELECT *
{ OPTIONAL { :a :b :c } }
EOF

N=$((N+1)) ; testGood $(fname "syntax-struct-" $N) <<EOF
# Operator, DOT
PREFIX :  <http://example.org/ns#> 
SELECT *
{ OPTIONAL { :a :b :c } . }
EOF

N=$((N+1)) ; testGood $(fname "syntax-struct-" $N) <<EOF
# Operator, triple
PREFIX :  <http://example.org/ns#> 
SELECT *
{ OPTIONAL { :a :b :c } ?x ?y ?z }
EOF

N=$((N+1)) ; testGood $(fname "syntax-struct-" $N) <<EOF
# Operator, DOT triple
PREFIX :  <http://example.org/ns#> 
SELECT *
{ OPTIONAL { :a :b :c } . ?x ?y ?z }
EOF

N=$((N+1)) ; testGood $(fname "syntax-struct-" $N) <<EOF
# Triple, semi, operator
PREFIX :  <http://example.org/ns#>
SELECT *
{ :p :q :r ; OPTIONAL { :a :b :c } }
EOF

N=$((N+1)) ; testGood $(fname "syntax-struct-" $N) <<EOF
# Triple, semi, DOT, operator
PREFIX :  <http://example.org/ns#>
SELECT *
{ :p :q :r ; . OPTIONAL { :a :b :c } }
EOF

N=$((N+1)) ; testGood $(fname "syntax-struct-" $N) <<EOF
# Two elements in the group
PREFIX :  <http://example.org/ns#>
SELECT *
{ :p :q :r . OPTIONAL { :a :b :c } 
  :p :q :r . OPTIONAL { :a :b :c } 
}
EOF

N=$((N+1)) ; testGood $(fname "syntax-struct-" $N) <<EOF
# Two elements in the group
PREFIX :  <http://example.org/ns#>
SELECT *
{ :p :q :r  OPTIONAL { :a :b :c } 
  :p :q :r  OPTIONAL { :a :b :c } 
}
EOF

## Syntactic sugar
N=0

## Lists (RDF collections)
N=$((N+1)) ; testGood $(fname "syntax-lists-" $N) <<EOF
PREFIX : <http://example.org/ns#> 
SELECT * WHERE { ( ?x ) :p ?z  }
EOF

N=$((N+1)) ; testGood $(fname "syntax-lists-" $N) <<EOF
PREFIX : <http://example.org/ns#> 
SELECT * WHERE { ?x :p ( ?z ) }
EOF

N=$((N+1)) ; testGood $(fname "syntax-lists-" $N) <<EOF
SELECT * WHERE { ( ?z ) }
EOF

N=$((N+1)) ; testGood $(fname "syntax-lists-" $N) <<EOF
SELECT * WHERE { ( ( ?z ) ) }
EOF

N=$((N+1)) ; testGood $(fname "syntax-lists-" $N) <<EOF
SELECT * WHERE { ( ( ) ) }
EOF

## BlankNodes
N=0

N=$((N+1)) ; testGood $(fname "syntax-bnodes-" $N) <<EOF
PREFIX : <http://example.org/ns#>
SELECT * WHERE { [:p :q ] }
EOF

N=$((N+1)) ; testGood $(fname "syntax-bnodes-" $N) <<EOF
PREFIX : <http://example.org/ns#>
SELECT * WHERE { [] :p :q }
EOF

N=$((N+1)) ; testGood $(fname "syntax-bnodes-" $N) <<EOF
PREFIX : <http://example.org/ns#>
SELECT * WHERE { [ ?x ?y ] :p [ ?pa ?b ] }
EOF

N=$((N+1)) ; testGood $(fname "syntax-bnodes-" $N) <<EOF
PREFIX : <http://example.org/ns#> 
SELECT *
WHERE { [ :p :q ; ] }
EOF

N=$((N+1)) ; testGood $(fname "syntax-bnodes-" $N) <<EOF
PREFIX : <http://example.org/ns#> 
SELECT *
WHERE { _:a :p1 :q1 .
        _:a :p2 :q2 .
      }
EOF

## Mixed forms
N=0

N=$((N+1)) ; testGood $(fname "syntax-forms-" $N) <<EOF
PREFIX : <http://example.org/ns#>
SELECT * WHERE { ( [ ?x ?y ] ) :p ( [ ?pa ?b ] 57 ) }
EOF

N=$((N+1)) ; testGood $(fname "syntax-forms-" $N) <<EOF
PREFIX : <http://example.org/ns#>
SELECT * WHERE { ( [] [] ) }
EOF


## Optional
N=0

## Union
N=0

N=$((N+1)) ; testGood $(fname "syntax-union-" $N) <<EOF
PREFIX : <http://example.org/ns#>
SELECT *
{
  { ?s ?p ?o } UNION { ?a ?b ?c } 
}
EOF

N=$((N+1)) ; testGood $(fname "syntax-union-" $N) <<EOF
PREFIX : <http://example.org/ns#>
SELECT *
{
  { ?s ?p ?o } UNION { ?a ?b ?c } UNION { ?r ?s ?t }
}
EOF

## Graph
N=0

## Expressions
N=0

## No longer legal
## N=$((N+1)) ; testGood $(fname "syntax-expr-" $N) <<EOF
## SELECT *
## WHERE { ?s ?p ?o . FILTER ?o }
## EOF

N=$((N+1)) ; testGood $(fname "syntax-expr-" $N) <<EOF
SELECT *
WHERE { ?s ?p ?o . FILTER (?o) }
EOF

N=$((N+1)) ; testGood $(fname "syntax-expr-" $N) <<EOF
SELECT *
WHERE { ?s ?p ?o . FILTER REGEX(?o, "foo") }
EOF

N=$((N+1)) ; testGood $(fname "syntax-expr-" $N) <<EOF
SELECT *
WHERE { ?s ?p ?o . FILTER REGEX(?o, "foo", "i") }
EOF

N=$((N+1)) ; testGood $(fname "syntax-expr-" $N) <<EOF
PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#>
SELECT *
WHERE { ?s ?p ?o . FILTER xsd:integer(?o) }
EOF

N=$((N+1)) ; testGood $(fname "syntax-expr-" $N) <<EOF
PREFIX :      <http://example.org/ns#> 
PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#>
SELECT *
WHERE { ?s ?p ?o . FILTER :myFunc(?s,?o) }
EOF

## ORDER BY, LIMIT, OFFSET
N=0

N=$((N+1)) ; testGood $(fname "syntax-order-" $N) <<EOF
PREFIX :      <http://example.org/ns#> 
SELECT *
{ ?s ?p ?o }
ORDER BY ?o
EOF

N=$((N+1)) ; testGood $(fname "syntax-order-" $N) <<EOF
PREFIX :      <http://example.org/ns#> 
SELECT *
{ ?s ?p ?o }
ORDER BY (?o+5)
EOF

N=$((N+1)) ; testGood $(fname "syntax-order-" $N) <<EOF
PREFIX :      <http://example.org/ns#> 
SELECT *
{ ?s ?p ?o }
ORDER BY ASC(?o)
EOF

N=$((N+1)) ; testGood $(fname "syntax-order-" $N) <<EOF
PREFIX :      <http://example.org/ns#> 
SELECT *
{ ?s ?p ?o }
ORDER BY DESC(?o)
EOF

N=$((N+1)) ; testGood $(fname "syntax-order-" $N) <<EOF
PREFIX :      <http://example.org/ns#> 
SELECT *
{ ?s ?p ?o }
ORDER BY DESC(:func(?s, ?o))
EOF

N=$((N+1)) ; testGood $(fname "syntax-order-" $N) <<EOF
PREFIX :      <http://example.org/ns#> 
SELECT *
{ ?s ?p ?o }
ORDER BY 
  DESC(?o+57) :func2(?o) ASC(?s)
EOF

N=$((N+1)) ; testGood $(fname "syntax-order-" $N) <<EOF
PREFIX :      <http://example.org/ns#> 
SELECT *
{ ?s ?p ?o }
ORDER BY str(?o)
EOF

## Limit and offset
N=0

N=$((N+1)) ; testGood $(fname "syntax-limit-offset-" $N) <<EOF
PREFIX :      <http://example.org/ns#> 
SELECT *
{ ?s ?p ?o }
ORDER BY ?o
LIMIT 5
EOF

N=$((N+1)) ; testGood $(fname "syntax-limit-offset-" $N) <<EOF
# LIMIT and OFFSET can be in either order
PREFIX :      <http://example.org/ns#> 
SELECT *
{ ?s ?p ?o }
ORDER BY ?o
LIMIT 5
OFFSET 3
EOF

N=$((N+1)) ; testGood $(fname "syntax-limit-offset-" $N) <<EOF
# LIMIT and OFFSET can be in either order
PREFIX :      <http://example.org/ns#> 
SELECT *
{ ?s ?p ?o }
ORDER BY ?o
OFFSET 3
LIMIT 5
EOF

N=$((N+1)) ; testGood $(fname "syntax-limit-offset-" $N) <<EOF
PREFIX :      <http://example.org/ns#> 
SELECT *
{ ?s ?p ?o }
ORDER BY ?o
OFFSET 3
EOF

## Pattern syntax : DOTs
N=0

N=$((N+1)) ; testGood $(fname "syntax-pat-" $N) <<EOF
PREFIX : <http://example.org/ns#> 
SELECT *
{ }
EOF

N=$((N+1)) ; testGood $(fname "syntax-pat-" $N) <<EOF
# No DOT after optional
PREFIX : <http://example.org/ns#> 
SELECT *
{ ?a :b :c OPTIONAL{:x :y :z} :x ?y ?z }
EOF

N=$((N+1)) ; testGood $(fname "syntax-pat-" $N) <<EOF
# No DOT between non-triples patterns
PREFIX : <http://example.org/ns#> 
SELECT *
{ ?a :b :c 
  OPTIONAL{:x :y :z} 
  { :x1 :y1 :z1 } UNION { :x2 :y2 :z2 }
}
EOF

N=$((N+1)) ; testGood $(fname "syntax-pat-" $N) <<EOF
# No DOT between non-triples patterns
PREFIX : <http://example.org/ns#> 
SELECT *
{
  OPTIONAL{:x :y :z} 
  ?a :b :c 
  { :x1 :y1 :z1 } UNION { :x2 :y2 :z2 }
}
EOF

## ## Query pattern special
## N=0
## 
## N=$((N+1)) ; testGood $(fname "syntax-query-pat-" $N) <<EOF
## PREFIX : <http://example.org/ns#> 
## SELECT *
## WHERE
##   ?a ?b ?c
## EOF
## 
## N=$((N+1)) ; testGood $(fname "syntax-query-pat-" $N) <<EOF
## # No trailing DOT in a query pattern
## PREFIX : <http://example.org/ns#> 
## SELECT *
## WHERE
##   :a :p ?x .
##   :x :p ?x .
##   :x :p ?x ; :q ?v
## EOF
## 
## N=$((N+1)) ; testGood $(fname "syntax-query-pat-" $N) <<EOF
## # Trailing DOT in a query pattern
## PREFIX : <http://example.org/ns#> 
## SELECT *
## WHERE
##   :a :p ?x .
##   :x :p ?x .
##   :x :p ?x ; :q ?v .
## EOF
## 
## N=$((N+1)) ; testGood $(fname "syntax-query-pat-" $N) <<EOF
## PREFIX : <http://example.org/ns#> 
## SELECT *
## WHERE
##   ?a ?b ?c
## LIMIT 5
## EOF
## 
## N=$((N+1)) ; testGood $(fname "syntax-query-pat-" $N) <<EOF
## PREFIX : <http://example.org/ns#> 
## SELECT *
## WHERE
##   ?a ?b ?c .
## LIMIT 5
## EOF
