#!/bin/bash

# Expect: environment contains M2_REPO
REPO=public_html/jena-R1-repo/org/apache/jena
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

V_IRI=0.9.0
V_CORE=2.7.0
V_DIST=2.7.0
V_ARQ=2.9.0
V_LARQ=1.0.0
inc=incubating

## Step 1 : top level
echo "## Top level"
$CPCMD KEYS dist
cpfile "apache-jena/${V_DIST}-$inc/apache-jena-${V_DIST}-$inc.zip" "."
cpfile "apache-jena/${V_DIST}-$inc/apache-jena-${V_DIST}-$inc.tar.gz" "."

## Step 2: modules

echo "## IRI"
D="jena-iri-${V_IRI}-$inc"
$MKDIR "$OUT/$D"
cpfile "jena-iri/${V_IRI}-$inc/jena-iri-${V_IRI}-$inc.jar"  $D
cpfile "jena-iri/${V_IRI}-$inc/jena-iri-${V_IRI}-$inc-sources.jar" $D

echo "## Core"
D="jena-core-${V_CORE}-$inc"
$MKDIR "$OUT/$D"
cpfile "jena-core/${V_CORE}-$inc/jena-core-${V_CORE}-$inc.jar"  $D
cpfile "jena-core/${V_CORE}-$inc/jena-core-${V_CORE}-$inc-sources.jar" $D
cpfile "jena-core/${V_CORE}-$inc/jena-core-${V_CORE}-$inc-javadoc.jar" $D

echo "## ARQ"
D="jena-arq-${V_ARQ}-$inc"
$MKDIR "$OUT/$D"
cpfile "jena-arq/${V_ARQ}-$inc/jena-arq-${V_ARQ}-$inc.jar"  $D
cpfile "jena-arq/${V_ARQ}-$inc/jena-arq-${V_ARQ}-$inc-sources.jar" $D
cpfile "jena-arq/${V_ARQ}-$inc/jena-arq-${V_ARQ}-$inc-javadoc.jar" $D

echo "## LARQ"
D="jena-larq-${V_LARQ}-$inc"
$MKDIR "$OUT/$D"
cpfile "jena-larq/${V_LARQ}-$inc/jena-larq-${V_LARQ}-$inc.jar"  $D
cpfile "jena-larq/${V_LARQ}-$inc/jena-larq-${V_LARQ}-$inc-sources.jar" $D
cpfile "jena-larq/${V_LARQ}-$inc/jena-larq-${V_LARQ}-$inc-javadoc.jar" $D
