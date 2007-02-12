#!/bin/bash

## Reification
N=0

N=$((N+1)) ; testGood $(fname "syntax-reif-arq-" $N arq) <<EOF
SELECT *
{ ?id << ?x ?y ?z >> }
EOF

N=$((N+1)) ; testGood $(fname "syntax-reif-arq-" $N arq) <<EOF
SELECT *
{ << ?x ?y ?z >> }
EOF

N=$((N+1)) ; testGood $(fname "syntax-reif-arq-" $N arq) <<EOF
SELECT *
{ [] << ?x ?y ?z >> }
EOF

N=$((N+1)) ; testGood $(fname "syntax-reif-arq-" $N arq) <<EOF
PREFIX : <http://example.org/ns#>
SELECT * WHERE
{  << ?s ?p ?o >> :p "" } # reification as subject
EOF

N=$((N+1)) ; testGood $(fname "syntax-reif-arq-" $N arq) <<EOF
PREFIX : <http://example.org/ns#>
SELECT * WHERE
{  << ?s ?p ?o >> :p << ?s ?p ?o >> } # reification as subject and object
EOF

## Mixed forms
N=0

N=$((N+1)) ; testGood $(fname "syntax-forms-arq-" $N arq) <<EOF
SELECT *
{ [ << ?x ?y ?z >> ] } # Same as [] << ?x ?y ?z >>
EOF

N=$((N+1)) ; testGood $(fname "syntax-forms-arq-" $N arq) <<EOF
CONSTRUCT { [] << ?s ?p ?o >> } WHERE { ?s ?p ?o }
EOF

N=$((N+1)) ; testGood $(fname "syntax-forms-arq-" $N arq) <<EOF
CONSTRUCT { [ << ?s ?p ?o >> ] } WHERE { ?s ?p ?o }
EOF

N=$((N+1)) ; testGood $(fname "syntax-forms-arq-" $N arq) <<EOF
CONSTRUCT { ?s ?p ?o } WHERE { [ << ?s ?p ?o >> ] }
EOF

N=$((N+1)) ; testGood $(fname "syntax-forms-arq-" $N arq) <<EOF
# Not a common query
CONSTRUCT { ?s ?p ?o } WHERE { ( [ << ?s ?p ?o >> ] ) }
EOF


# Silly but legal

N=$((N+1)) ; testGood $(fname "syntax-forms-arq-" $N arq) <<EOF
PREFIX : <http://example.org/ns#>
SELECT *
WHERE
{
  [] << (?s) [ :pp :qq ] << :s :p :o >> >> .
  ( [ :p () ] ) . 
  ( [ :p << ?s ?p ?o >> ] ) .  # reification-as-object
  { ( [ << ?s ?p ?o >> ] ) } .
  { ( [ << [:p :q ] << 1 2 3 >> [a [] ] >> ] ) } .
}
EOF
