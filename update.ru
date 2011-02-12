PREFIX rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xsd:    <http://www.w3.org/2001/XMLSchema#>
PREFIX :       <http://example/>

CREATE GRAPH <http://example/g5> ;

INSERT DATA { GRAPH <http://example/g5> { :s :p :o } }

