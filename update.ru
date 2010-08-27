PREFIX :     <http:/example/>
PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs:  <http://www.w3.org/2000/01/rdf-schema#>

CREATE SILENT GRAPH :graph1 ;

INSERT DATA
{ 
  GRAPH :graph1
  { :s :p :o 
  }
}
