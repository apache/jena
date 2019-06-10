#!/bin/bash

# Functions in support of syntax tests
# Source this file.

# Query
declare -a GOOD_S10
declare -a GOOD_S11
declare -a GOOD_ARQ
declare -a BAD_S10
declare -a BAD_S11
declare -a BAD_ARQ

# Update
declare -a GOOD_U11
declare -a BAD_U11
declare -a GOOD_ARQU
declare -a BAD_ARQU

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
    if [ "$#" != 2 ]
	then
	echo "Problems with $*"
    fi
    local LANG="$1"
    local FN="$2"
    local I
    case "$LANG" in
	($SPARQL10)   I=${#GOOD_S10[*]} ; GOOD_S10[$I]=$FN ;;
	($SPARQL11)   I=${#GOOD_S11[*]} ; GOOD_S11[$I]=$FN ;;
	($SPARQL11U)  I=${#GOOD_U11[*]} ; GOOD_U11[$I]=$FN ;;
	($ARQ)        I=${#GOOD_ARQ[*]} ; GOOD_ARQ[$I]=$FN ;;
	($ARQU)       I=${#GOOD_ARQU[*]} ; GOOD_ARQU[$I]=$FN ;;
	    *)      echo "Unrecognized: $*" ;;
    esac

    cat > $FN
}

# reads from stdin
function testBad
{
    if [ "$#" != 2 ]
	then
	echo "Problems with $*"
    fi
    local LANG="$1"
    local FN="$2"
    local I
    case "$LANG" in
	($SPARQL10)  I=${#BAD_S10[*]} ; BAD_S10[$I]=$FN ;;
	($SPARQL11)  I=${#BAD_S11[*]} ; BAD_S11[$I]=$FN ;;
	($SPARQL11U) I=${#BAD_U11[*]} ; BAD_U11[$I]=$FN ;;
	($ARQ)       I=${#BAD_ARQ[*]} ; BAD_ARQ[$I]=$FN ;;
	($ARQU)      I=${#BAD_ARQU[*]} ; BAD_ARQU[$I]=$FN ;;
	    *)      echo "Unrecognized: $*" ;;
    esac

    cat > $FN
}

function clean
{
    rm -f *.rq
    rm -f *.arq
    rm -f *.ttl
}

function output
{
    local FN="$1"
    local TYPE="$2"
    
    I="$(($I+1))"
    local N=":test_$I"
    local E="$N rdf:type   $TYPE ;"
    E="${E}\n   dawgt:approval dawgt:NotClassified ;" ;
    E="${E}\n   mf:name    \"$FN\" ;" 
    E="${E}\n   mf:action  <$FN> ;"
    E="${E}.\n"

    ENTRIES="$ENTRIES$E\n"
    ITEMS="$ITEMS\n$N"
}
    
function outputLicense
{
    cat <<EOF
#  Licensed to the Apache Software Foundation (ASF) under one or more
#  contributor license agreements.  See the NOTICE file distributed with
#  this work for additional information regarding copyright ownership.
#  The ASF licenses this file to You under the Apache License, Version 2.0
#  (the "License"); you may not use this file except in compliance with
#  the License.  You may obtain a copy of the License at
# 
#       http://www.apache.org/licenses/LICENSE-2.0
# 
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

EOF

}

function createManifest
{
    if [ "$#" != 2 ]
    then
	echo "Wrong number of arguments to createManifest" 1>&2
	exit 1
    fi

    local LABEL="$1"
    local URI="$2"
## Header
    (
	outputLicense
	cat <<EOF
@prefix :       $URI .
@prefix rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix rdfs:	<http://www.w3.org/2000/01/rdf-schema#> .
@prefix mf:     <http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#> .
@prefix mfx:    <http://jena.hpl.hp.com/2005/05/test-manifest-extra#> .
@prefix qt:     <http://www.w3.org/2001/sw/DataAccess/tests/test-query#> .
@prefix dawgt:  <http://www.w3.org/2001/sw/DataAccess/tests/test-dawg#> .

<>  rdf:type mf:Manifest ;
    rdfs:comment "Syntax tests $LABEL" ;
    mf:entries
    ( 
EOF

    )> manifest.ttl
# Build the manifest list.
# Build the manifest items.

    # These are globals.
    ENTRIES=""
    ITEMS=""
    I=0

    # SPARQL 1.0
    for f in "${GOOD_S10[@]}"
    do
      output "$f" "mf:PositiveSyntaxTest"
      done

    for f in "${BAD_S10[@]}"
    do
      output "$f" "mf:NegativeSyntaxTest"
      done

    # SPARQL 1.1
    for f in "${GOOD_S11[@]}"
    do
      output "$f" "mf:PositiveSyntaxTest11"
      done

    for f in "${BAD_S11[@]}"
    do
      output "$f" "mf:NegativeSyntaxTest11"
      done

    # ARQ
    for f in "${GOOD_ARQ[@]}"
    do
      output "$f" "mfx:PositiveSyntaxTestARQ"
      done

    for f in "${BAD_ARQ[@]}"
    do
      output "$f" "mfx:NegativeSyntaxTestARQ"
      done

    # SPARQL 1.1 Update
    for f in "${GOOD_U11[@]}"
    do
      output "$f" "mf:PositiveUpdateSyntaxTest11"
      done

    for f in "${BAD_U11[@]}"
    do
      output "$f" "mf:NegativeUpdateSyntaxTest11"
      done

    # ARQ 1.1 Update
    for f in "${GOOD_ARQU[@]}"
    do
      output "$f" "mf:PositiveUpdateSyntaxTestARQ"
      done

    for f in "${BAD_ARQ11[@]}"
    do
      output "$f" "mf:NegativeUpdateSyntaxTestARQ"
      done

    echo -e "$ITEMS" >> manifest.ttl
    echo ') .' >> manifest.ttl
    echo >> manifest.ttl
    echo -e "$ENTRIES" >> manifest.ttl
}
