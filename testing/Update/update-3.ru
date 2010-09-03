PREFIX : <http://example/> 

CREATE GRAPH <http://example/g1> ;

INSERT DATA {
  GRAPH <http://example/g1> { :r :p 123 } 
} ;

DELETE DATA {
  GRAPH <http://example/g1> { :r :p 123 } 
} ;

