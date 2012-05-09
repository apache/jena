BASE <http://example/> 

INSERT { ?o <p> <s> .
         <s> <p> ?o 
} 
WHERE 
{ BIND("object" AS ?o) }

