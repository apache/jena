#!/bin/bash

# Expect: environment contains M2_REPO
#REPO=public_html/jena-R1-repo/org/apache/jena
REPO=REPO/org/apache/jena/

OUT="dist"

# This script collects everything for the incubator/dist/jena area
# Layout:  VER is whatever VER is needed - different for different subsystems
# .../jena/KEYS
# .../jena/apache-jena-VER.zip
# .../jena/jena-iri-VER/
# .../jena/jena-core-VER/
# .../jena/jena-arq-VER/
# .../jena/jena-larq-VER/
#   with jar, javadoc (not IRI) and sources and their .asc, .md5 and .sha1 files.

## Or copy the repo and shuffle.

ECHO=echo
CPCMD="$ECHO cp"
MKDIR="$ECHO mkdir"
DELDIR="$ECHO rm -rf"

## 
echo "## Initalize"
$DELDIR $OUT
$MKDIR $OUT

function cpfile
{
    local FILE="$1"
    local DIR="$2"

    local SRC="$REPO/$FILE"
    local DEST="$OUT/$DIR"

    $CPCMD "$SRC" "$DEST"
    #for ext in asc asc.md5 asc.sha1 md5 sha1
    for ext in  asc md5 sha1
    do
	$CPCMD "$SRC.$ext" "$DEST"
    done
}

## ToDo: automate

V_TOP=0
V_IRI=0.9.0
V_CORE=2.7.0
V_DIST=2.7.0
V_ARQ=2.9.0
V_LARQ=1.0.0
V_ZIP=$V_CORE
inc=incubating

## Step 1 : top level
echo "## Top level"
$CPCMD KEYS dist
$CPCMD HEADER.html dist

cpfile "apache-jena/${V_DIST}-$inc/apache-jena-${V_DIST}-$inc.zip" "."
cpfile "apache-jena/${V_DIST}-$inc/apache-jena-${V_DIST}-$inc.tar.gz" "."

function cpallfiles
{
    local M="$1"
    local V="$2"
    local D="$M-$V-$inc"
    $MKDIR $OUT/$D
    cpfile "$M/$V-$inc/$M-$V-$inc.jar" $D
    cpfile "$M/$V-$inc/$M-$V-$inc-sources.jar" $D
    if [ -e "$M/$V-$inc/$M-$V-$inc-javadoc.jar" ]
    then
	 cpfile "$M/$V-$inc/$M-$V-$inc-javadoc.jar" $D
    fi
    cpfile "$M/$V-$inc/$M-$V-$inc-source-release.zip" $D
}


## Step 2: modules

echo "## JenaTop"
M=jena-top
V=${V_TOP}
D="$M-$V-$inc"
$MKDIR $OUT/$D
cpfile "$M/$V-$inc/$M-$V-$inc.pom" $D
cpfile "$M/$V-$inc/$M-$V-$inc-source-release.zip" $D

echo "## IRI"
cpallfiles jena-iri "${V_IRI}"

echo "## Core"
cpallfiles jena-core ${V_CORE}

echo "## ARQ"
cpallfiles jena-arq ${V_ARQ}

## echo "## LARQ"
## cpallfiles jena-larq ${V_LARQ}

echo "## Download"
M=apache-jena
V=${V_ZIP}
D="$M-$V-$inc"
$MKDIR $OUT/$D
cpfile "$M/$V-$inc/$M-$V-$inc-source-release.zip" $D
