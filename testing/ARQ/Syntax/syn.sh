#!/bin/bash

source syn-func.sh

# DAWG core set,in 3 parts
(   source syn-func.sh 
    cd Syntax-SPARQL   
    clean 
    source ../syn-sparql.sh  
    createManifest "Syntax-SPARQL"
)

(   source syn-func.sh  
    cd Syntax-SPARQL2  
    clean 
    source ../syn-sparql2.sh  
    createManifest "Syntax-SPARQL2"
)

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

