PREFIX : <http://example/> 
PREFIX rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 

INSERT DATA {
  :x0 :p () .
  :x1 :p (1) .
  :x2 :p (1 2) .
  :x3 :p (1 2 3) .
}

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
}

# List of length = 0
DELETE { ?x :p rdf:nil . }
INSERT { ?x :p [ rdf:first 99 ; rdf:rest rdf:nil ] }
WHERE
{
   ?x :p rdf:nil .
}
