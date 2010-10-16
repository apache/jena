prefix ex: <http://example.com/ex>

## create silent graph <http://example.com/graph/1> 
## create silent graph <http://example.com/graph/2> 

insert data 
{
  GRAPH <http://example.com/graph/1> 
  { ex:a1 ex:prop ex:b1 }
}

;

insert data
{
  GRAPH <http://example.com/graph/2> 
  { ex:a2 ex:prop ex:b2 }
}
