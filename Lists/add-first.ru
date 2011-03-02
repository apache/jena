# Copyright (c) Epimorphics Ltd
# License: CC3.0: http://creativecommons.org/licenses/by/3.0

PREFIX : <http://example/> 
PREFIX rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 

INSERT DATA {
  :x0 :p () .
  # :x0 :p "other" .

  :x1 :p (1) .
  # :x1 :p "other" .

  :x2 :p (1 2) .
  # :x2 :p "other" .

  :x3 :p (1 2 3) .
  # :x3 :p "other" .
}

DELETE { ?x :p ?list }
INSERT { ?x :p [ rdf:first 0 ; 
                 rdf:rest ?list ]
       }
WHERE
{
  ?x :p ?list .
}
