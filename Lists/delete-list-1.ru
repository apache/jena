PREFIX rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
PREFIX : <http://example.com/data/#> 

INSERT DATA {
:x0 :p () .
:x0 :q "abc" .

:x1 :p (1) .
:x1 :q "def" .

:x2 :p (1 2) .
:x2 :q "ghi" .
}

# Delete the cons cells.
DELETE
    { ?z rdf:first ?head ; rdf:rest ?tail . }
WHERE { 
      [] :p ?list .
      ?list rdf:rest* ?z .
      ?z rdf:first ?head ;
         rdf:rest ?tail .
      }

# Delete the triples that connect the lists.
DELETE WHERE { ?x :p ?z . }
