PREFIX :        <http://example/>

INSERT DATA 
{ :s :p :o
  GRAPH :g { :s1 :p1 :o1 }
} ;

MOVE DEFAULT TO :g1 ;
ADD  DEFAULT TO :g ;
ADD  DEFAULT TO :g2 ;
DROP GRAPH :g2 ;
