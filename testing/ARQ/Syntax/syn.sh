#!/bin/bash

source syn-func.sh

( cd Syntax-SPARQL  ; clean ; source ../syn-sparql.sh   ; createManifest 1 )
( cd Syntax-SPARQL2 ; clean ; source ../syn-sparql2.sh  ; createManifest 2 )

( cd Syntax-ARQ     ; clean ; source ../syn-arq.sh      ; createManifest 3 )
( cd Syntax-SPARQL3 ; clean ; source ../syn-dev.sh      ; createManifest 4 )
