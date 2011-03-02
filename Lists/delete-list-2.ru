PREFIX rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
PREFIX : <http://example.com/data/#> 

INSERT DATA {
:x0 :p () .
:x0 :p "String 0" .
:x0 :p [] .

:x1 :p (1) .
:x1 :p "String 1" .
:x1 :p [] .

:x2 :p (1 2) .
:x2 :p "String 2" .
:x2 :p [] .

# A list not connected.
(1 2) .

# Not legal RDF.
# () .

}

# Mark the objects we need to delete at the end
# A list is either () AKA rdf:nil, or has a rdf:rest.

# We do this so we can find the cons cells to delete
# in stage two but leave evidence for the removal of
# the link to the list even if the property is used 
# for a non-list.


INSERT { ?list :deleteMe true . }
WHERE {
   ?x :p ?list . 
   FILTER (?list = rdf:nil || EXISTS{?list rdf:rest ?z} )
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

# Delete the marked nodes
DELETE 
WHERE { ?x :p ?z . 
        ?z :deleteMe true . 
}

## ------
## Unconnected lists.

DELETE
    { ?z rdf:first ?head ; rdf:rest ?tail . }
WHERE { 
      ?list rdf:rest ?z2 .
      FILTER NOT EXISTS { ?s ?p ?list }
      ?list rdf:rest* ?z .
      ?z rdf:first ?head ;
         rdf:rest ?tail .
      } 