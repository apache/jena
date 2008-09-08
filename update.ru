prefix ModelManagement:
<http://www.boeing.com/IVHM/ModelManagement.owl#>
prefix Common: <http://www.boeing.com/IVHM/Common.owl#>
INSERT
{ModelManagement:jeff a ModelManagement:User .
 ModelManagement:jeff Common:name "jeff" .
 ModelManagement:jeff ModelManagement:id ?userID }
WHERE {?proj a ModelManagement:Project .
       ?proj ModelManagement:id ?lastUserID  .
       LET (?userID := ?lastUserID + 1)}

## PREFIX : <http://example/>
## PREFIX  rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
## PREFIX  rdfs:   <http://www.w3.org/2000/01/rdf-schema#>
## 
## LOAD <D.ttl>
## 
## INSERT { :x1 a ?t . ?t :TT "Added" }
## WHERE  { :x1 a/rdfs:subClassOf+ ?t }
