#!/usr/bin/bash
## Standalone generation for now.

## SPARQL10="sparql10"
## SPARQL11="sparql11"
## SPARQL12="sparql12"
## ARQ="arq"
## SPARQL11U="sparql11update"
## SPARQL12U="sparql12update"

SPARQL12="sparql12"

source syn-func12.sh

(
    cd Syntax-SPARQL_12
    clean 
    source ../syn-sparql_12.sh  
    createManifest "Syntax SPARQL 1.2" '<manifest#>'
)
