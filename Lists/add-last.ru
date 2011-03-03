# Copyright (c) Epimorphics Ltd
# License: CC3.0: http://creativecommons.org/licenses/by/3.0

PREFIX : <http://example/> 
PREFIX rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 

INSERT DATA {
  :x0 :p () .
  :x0 :p "other" .

  :x1 :p (1) .
  :x1 :p "other" .

  :x2 :p (1 2) .
  :x2 :p "other" .

  :x3 :p (1 2 3) .
  :x3 :p "other" .
} ;

# The order here is important.
# Must do list >= 1 first.

# List of length >= 1
DELETE { ?elt rdf:rest rdf:nil }
INSERT { ?elt rdf:rest [ rdf:first 98 ; rdf:rest rdf:nil ] }
WHERE
{
  ?x :p ?list .
  # List of length >= 1
  ?list rdf:rest+ ?elt .
  ?elt rdf:rest rdf:nil .
  # ?elt is end of list.
} ;

# List of length = 0
DELETE { ?x :p rdf:nil . }
INSERT { ?x :p [ rdf:first 99 ; rdf:rest rdf:nil ] }
WHERE
{
   ?x :p rdf:nil .
}
