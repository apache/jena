#!/bin/bash

# You need a copy of the staging repo to get the checksum files.

# NB This fails unless this first:
#
# mkdir -p https://repository.apache.org/content/repositories/orgapachejena-NNN/org/apache/jena
#   otherwise it creates a file in this location then can't mirror below it.
#
# wget -e robots=off --wait 1 --mirror -np \
#     https://repository.apache.org/content/repositories/orgapachejena-NNN/org/apache/jena

# Expect: environment contains M2_REPO
#REPO=public_html/jena-R1-repo/org/apache/jena
REPO=REPO/org/apache/jena

OUT="dist"

# This script collects everything for the incubator/dist/jena area
# for a TDB release.  Copy to dist/jena to add to the last jena release.

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



## ToDo: automate

V_TDB=0.9.0
inc=incubating

## Top level directory

## Modules

echo "## TDB"
cpallfiles jena-tdb "${V_TDB}"

## echo "## zip"
## M=jena-tdb
## V=${V_TDB}
## D="$M-$V-$inc"
## cpfile $D/$D-distribution.zip      .
## cpfile $D/$D-distribution.tar.gz   .
