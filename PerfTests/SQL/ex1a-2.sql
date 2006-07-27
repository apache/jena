## PREFIX  rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
## PREFIX  :      <urn:lsid:uniprot.org:ontology:>
## SELECT  ?protein ?name
## WHERE
##   { 
##     ?gene     :name       "CRB" .
##     ?protein  :encodedBy  ?gene .
##     ?protein  :name       ?name .
##     ?protein  rdf:type    :Protein .
##   }

SELECT 
  R$7.lex AS protein$lex, R$7.datatype AS protein$datatype, R$7.lang AS protein$lang, R$7.type AS protein$type, 
  R$8.lex AS name$lex, R$8.datatype AS name$datatype, R$8.lang AS name$lang, R$8.type AS name$type
FROM
                     -- Const: <urn:lsid:uniprot.org:ontology:name>
    ( SELECT Nodes AS N$1 WHERE N$1.hash = -8084558025431482196 )
  INNER JOIN
                        -- Const: "CRB"
    ( SELECT Nodes AS N$2 WHERE N$2.hash = 828734478346052422 )
  INNER JOIN
                        -- Const: <urn:lsid:uniprot.org:ontology:encodedBy>
    ( SELECT Nodes AS N$3 WHERE N$3.hash = -2042754374504399221 )
  INNER JOIN
                        -- Const: <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>
    ( SELECT Nodes AS N$4 WHERE N$4.hash = -6430697865200335348 )
  INNER JOIN
                       -- Const: <urn:lsid:uniprot.org:ontology:Protein>
    ( SELECT Nodes AS N$5 WHERE N$5.hash = -3009713141553380375 )

  INNER JOIN
    Triples AS T$1                      -- ?gene <urn:lsid:uniprot.org:ontology:name> "CRB"
  ON ( T$1.p = N$1.id                   -- Const condition: <urn:lsid:uniprot.org:ontology:name>
   AND T$1.o = N$2.id                   -- Const condition: "CRB"
   )
  INNER JOIN
    Triples AS T$2                      -- ?protein <urn:lsid:uniprot.org:ontology:encodedBy> ?gene
  ON ( T$2.p = N$3.id                   -- Const condition: <urn:lsid:uniprot.org:ontology:encodedBy>
   AND T$1.s = T$2.o                    -- Join var: ?gene
   )
  INNER JOIN
    Triples AS T$3                      -- ?protein <urn:lsid:uniprot.org:ontology:name> ?name
  ON ( T$3.p = N$1.id                   -- Const condition: <urn:lsid:uniprot.org:ontology:name>
   AND T$2.s = T$3.s                    -- Join var: ?protein
   )
  INNER JOIN
    Triples AS T$4                      -- ?protein <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <urn:lsid:uniprot.org:ontology:Protein>
  ON ( T$4.p = N$4.id                   -- Const condition: <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>
   AND T$4.o = N$5.id                   -- Const condition: <urn:lsid:uniprot.org:ontology:Protein>
   AND T$2.s = T$4.s                    -- Join var: ?protein
   )
  INNER JOIN
    Nodes AS R$6                        -- Var: ?gene
  ON ( T$1.s = R$6.id )
  INNER JOIN
    Nodes AS R$7                        -- Var: ?protein
  ON ( T$2.s = R$7.id )
  INNER JOIN
    Nodes AS R$8                        -- Var: ?name
  ON ( T$3.o = R$8.id )
