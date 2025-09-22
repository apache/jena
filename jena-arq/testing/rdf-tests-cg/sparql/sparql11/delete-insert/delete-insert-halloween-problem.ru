PREFIX ex: <http://example.com/>

DELETE { ?s ex:salary ?o }
INSERT { ?s ex:salary ?v }
WHERE { ?s ex:salary ?o FILTER(?o < 1500) BIND(?o + 100 AS ?v) }
