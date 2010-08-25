BASE    <base:>
PREFIX  :     <http://example/>

WITH :g
DELETE {
  <s> ?p ?o .
}
INSERT {
  ?s ?p <#o> .
}
USING <base:g>
USING NAMED :gn
WHERE
  { ?s ?p ?o }
