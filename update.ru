PREFIX : <http://example/>

INSERT
{ 
  :x :p :q .
  :x2 :p :q 
}


MODIFY
DELETE { :x :p ?x }
INSERT { :x :p 99 }
WHERE  { :x :p ?x }
