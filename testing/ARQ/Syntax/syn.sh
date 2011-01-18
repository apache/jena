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
    cd Syntax-SPARQL_10_1   
    clean 
    source ../syn-sparql_10_1.sh  
    createManifest "Syntax SPARQL 1.0 part 1"
)

(
    cd Syntax-SPARQL_10_2
    clean 
    source ../syn-sparql_10_2.sh  
    createManifest "Syntax SPARQL 1.0 part 2"
)

(
    cd Syntax-SPARQL_10_3
    clean
    source ../syn-sparql_10_3.sh
    createManifest "Syntax SPARQL 1.0 part 3"
)

# SPARQL 11
(
    cd Syntax-SPARQL_11
    clean
    source ../syn-sparql_11_1.sh
    createManifest "Syntax SPARQL 1.1"
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
