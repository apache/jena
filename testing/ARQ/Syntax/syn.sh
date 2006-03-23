#!/bin/bash

source syn-func.sh

# DAWG core set
(   source syn-func.sh 
    cd Syntax-SPARQL   
    clean 
    source ../syn-sparql.sh  
    createManifest 1
)

# DAWG extended
(   source syn-func.sh  
    cd Syntax-SPARQL2  
    clean 
    source ../syn-sparql2.sh  
    createManifest 2
)

# Development
(   source syn-func.sh  
    cd Syntax-SPARQL3
    clean
    source ../syn-dev.sh
    createManifest 3
 )

# ARQ
(   source syn-func.sh  
    cd Syntax-ARQ
    clean 
    source ../syn-arq.sh 
    createManifest 4
)

