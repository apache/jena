PREFIX : <http://example/>

LOAD <http://rdf.freebase.com/ns/en.miles_davis> INTO GRAPH <http://example/tmp> ;

INSERT { <http://rdf.freebase.com/ns/en.miles_davis> ?p ?o }
WHERE { GRAPH <http://example/tmp> 
   { <http://rdf.freebase.com/ns/en.miles_davis> ?p ?o } }
