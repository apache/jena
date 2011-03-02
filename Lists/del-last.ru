# Copyright 2011 Epimorphics Ltd
# License: CC3.0: http://creativecommons.org/licenses/by/3.0

PREFIX : <http://example/> 
PREFIX rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 

INSERT DATA {
  :x3 :p (1 2 3) .
  :x2 :p (1 2) .
  :x1 :p (1) .
  :x0 :p () .
}

# List of length 1
# Do before other lists.

DELETE { ?x :p ?elt .
         ?elt  rdf:first ?v .
         ?elt  rdf:rest  rdf:nil .
       }
INSERT { ?x :p rdf:nil . }
WHERE
{
  ?x :p ?elt .
  ?elt rdf:first ?v ;
       rdf:rest rdf:nil .
}

# List of length >= 2
DELETE { ?elt1 rdf:rest ?elt .
         ?elt  rdf:first ?v .
         ?elt  rdf:rest  rdf:nil .
       }
INSERT { ?elt1 rdf:rest rdf:nil }
WHERE
{
  ?x :p ?list .
  ?list rdf:rest* ?elt1 .

  # Second to end.
  ?elt1 rdf:rest ?elt .
  # End.
  ?elt rdf:first ?v ;
       rdf:rest rdf:nil .
}

