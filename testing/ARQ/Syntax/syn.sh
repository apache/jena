#!/bin/bash

source syn-func.sh

# DAWG core set
(   source syn-func.sh 
    cd Syntax-SPARQL   
    clean 
    source ../syn-sparql.sh  
    createManifest "Syntax-SPARQL"
)

# DAWG extended
(   source syn-func.sh  
    cd Syntax-SPARQL2  
    clean 
    source ../syn-sparql2.sh  
    createManifest "Syntax-SPARQL2"
)

# Development
(   source syn-func.sh  
    cd Syntax-SPARQL3
    clean
    source ../syn-dev.sh
    createManifest "Syntax-SPARQL3"
 )

# ARQ
(   source syn-func.sh  
    cd Syntax-ARQ
    clean 
    source ../syn-arq.sh 
    createManifest "Syntax-ARQ"
)

