#!/bin/bash
# SPARQL Update (Recommednation) syntax examples

function fname
{
    R="$1"
    N="$2"
    E="$3"
    [ "$E" = "" ] && E="ru"
    echo $(printf "$R%02d.$E" $N)
}

## Structure
N=0

N=$((N+1)) ; testGood $SPARQL11U $(fname "syntax-update-" $N) <<EOF
BASE <http://example/base#>
PREFIX : <http://example/>
LOAD <http://example.org/faraway>
EOF

N=$((N+1)) ; testGood $SPARQL11U $(fname "syntax-update-" $N) <<EOF
LOAD <http://example.org/faraway> ;
EOF

