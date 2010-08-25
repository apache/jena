#!/bin/bash

# Language names
SPARQL10="sparql10"
SPARQL11="sparql11"
ARQ="arq"
SPARQL11U="sparql11update"

source syn-func.sh

# SPARQL Update
(
    cd Syntax-SPARQL-Update
    clean 
    source ../syn-update.sh 
    createManifest "Syntax SPARQL Update"
)
