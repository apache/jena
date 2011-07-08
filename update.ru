PREFIX rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xsd:    <http://www.w3.org/2001/XMLSchema#>
PREFIX :       <http://example/>

INSERT DATA { GRAPH :graph1 {:x a :C } } ;

WITH :graph1
INSERT { :x99 a ?C }
WHERE { ?x a ?C }
