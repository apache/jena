#!/bin/bash

# Language names
SPARQL10="sparql10"
SPARQL11="sparql11"
ARQ="arq"
SPARQL11U="sparql11update"

source syn-func.sh

# Todo: geneate URIs, not bnodes, for tests.

# DAWG core set,in 3 parts
(
    cd Syntax-SPARQL   
    clean 
    source ../syn-sparql.sh  
    createManifest "Syntax-SPARQL"
)

(
    cd Syntax-SPARQL2  
    clean 
    source ../syn-sparql2.sh  
    createManifest "Syntax-SPARQL2"
)

(
    cd Syntax-SPARQL3
    clean
    source ../syn-sparql3.sh
    createManifest "Syntax-SPARQL3"
)

# SPARQL 11
(
    cd Syntax-SPARQL4
    clean
    source ../syn-sparql4.sh
    createManifest "Syntax-SPARQL4"
 )

# ARQ
(
    cd Syntax-ARQ
    clean 
    source ../syn-arq.sh 
    createManifest "Syntax-ARQ"
)

# SPARQL Update
(
    cd Syntax-SPARQL-Update
    clean 
    source ../syn-update.sh 
    createManifest "Syntax SPARQL Update"
)

# Syntax-SPARQL-Update-2 is handwritten scripts.
