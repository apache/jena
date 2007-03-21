#!/bin/bash

# Functions in support of syntax tests
# Source this file.

declare -a GOOD
GOOD_N=0
declare -a BAD
BAD_N=0

function fname
{
    local BASE="$1"	# Base
    local N="$2"	# Number
    local EXT="$3"	# Extension
    [ "$EXT" = "" ] && EXT="rq"
    echo $(printf "$BASE%02d.$EXT" $N)
}

# reads from stdin
function testGood
{
    local FN="$1"
    local I=${#GOOD[*]}
    GOOD[$I]=$FN
    cat > $FN
}

# reads from stdin
function testBad
{
    local FN="$1"
    local I=${#BAD[*]}
    BAD[$I]=$FN
    cat > $FN
}

function clean
{
    rm -f *.rq
    rm -f *.arq
    rm -f *.ttl
}

function createManifest
{
    local LABEL="$1"
## Header
    cat > manifest.ttl <<EOF
@prefix rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix rdfs:	<http://www.w3.org/2000/01/rdf-schema#> .
@prefix mf:     <http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#> .
@prefix mfx:    <http://jena.hpl.hp.com/2005/05/test-manifest-extra#> .
@prefix qt:     <http://www.w3.org/2001/sw/DataAccess/tests/test-query#> .

<>  rdf:type mf:Manifest ;
    rdfs:comment "Syntax tests $LABEL" ;
    mf:entries
    ( 
EOF
    # Queries good syntax
    for f in "${GOOD[@]}"
      do
      cat >> manifest.ttl <<EOF
      [  mf:name    "$f" ;
         rdf:type   mfx:TestSyntax ;
         mf:action  <$f> ; 
      ]
EOF
    done

    # Queries - bad syntax
    for f in "${BAD[@]}"
      do
      cat >> manifest.ttl <<EOF
      [  mf:name    "$f" ;
         rdf:type   mfx:TestBadSyntax ;
         mf:action  <$f> ; 
      ]
EOF
    done

## Trailer
    cat >> manifest.ttl <<EOF
    ) .
EOF
}
