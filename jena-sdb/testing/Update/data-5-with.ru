PREFIX ex: <http://example/>

DROP ALL
;

INSERT DATA { GRAPH ex:g { ex:s ex:p ex:o }}
;

WITH ex:g
INSERT { ex:s2 ex:p2 ex:o2 }
WHERE { ?s ?p ?o }
