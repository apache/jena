PREFIX : <http://example/>

INSERT DATA
{ 
  :x :p :q .
  :x2 :p :q 
}

INSERT 
{ :y :p ?q } WHERE { :x :p ?q }


CREATE GRAPH <http://example/foo> 

INSERT DATA INTO <http://example/foo> 
{ :z :p 123 }
