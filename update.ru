PREFIX : <http://example/>

MODIFY
DELETE
{ ?x :p ?v }
INSERT
{ ?x :p ?v1 }
WHERE 
{ ?x :p ?v
  LET (?v1 := ?v + 1)
}

