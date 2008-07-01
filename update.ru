PREFIX : <http://example/>
PREFIX  rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX  rdfs:   <http://www.w3.org/2000/01/rdf-schema#>

LOAD <D.ttl>

INSERT { :x1 a ?t . ?t :TT "Added" }
WHERE  { :x1 a/rdfs:subClassOf+ ?t }
