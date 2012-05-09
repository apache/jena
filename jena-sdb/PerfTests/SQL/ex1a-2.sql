# Use (SELECT...) for the constants
SELECT 
  R$6.lex AS protein$lex, R$6.datatype AS protein$datatype, R$6.lang AS protein$lang, R$6.type AS protein$type, 
  R$7.lex AS name$lex, R$7.datatype AS name$datatype, R$7.lang AS name$lang, R$7.type AS name$type
FROM
    ( SELECT * FROM Nodes WHERE Nodes.hash = -8084558025431482196 ) AS N$1 ,
    ( SELECT * FROM Nodes WHERE Nodes.hash =  828734478346052422  ) AS N$2 ,
    ( SELECT * FROM Nodes WHERE Nodes.hash = -2042754374504399221 ) AS N$3 ,
    ( SELECT * FROM Nodes WHERE Nodes.hash = -6430697865200335348 ) AS N$4 ,
    ( SELECT * FROM Nodes WHERE Nodes.hash = -3009713141553380375 ) AS N$5 ,
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
    Nodes AS R$6                        -- Var: ?protein
  ON ( T$2.s = R$6.id )
  INNER JOIN
    Nodes AS R$7                        -- Var: ?name
  ON ( T$3.o = R$7.id )
