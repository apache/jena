#!/bin/bash

N=0
N=$((N+1)) ; testGood $SPARQL11 $(fname "syntax-service-" $N) <<EOF
SELECT * { SERVICE <g> { ?s ?p ?o } }
EOF

N=$((N+1)) ; testGood $SPARQL11 $(fname "syntax-service-" $N) <<EOF
SELECT * { ?s ?p ?o SERVICE <g> { ?s ?p ?o } ?s ?p ?o }
EOF

N=$((N+1)) ; testGood $SPARQL11 $(fname "syntax-service-" $N) <<EOF
SELECT * { ?s ?p ?o SERVICE SILENT <g> { ?s ?p ?o } ?s ?p ?o }
EOF