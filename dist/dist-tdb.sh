#!/bin/bash

# You need a copy of the staging repo to get the checksum files.


# Or use command line tools.
# sha1sum $F > $F.sha1
# md5sum $F > $F.md5
# gpg --batch --armour --detach-sign F (creates $F.asc or use -o)

# Layout:
# /source-release/MOD-VER/
# or
# /source-release/
#   The offical release file: MOD-VER-source-release.{zip,tar.gz}{,.asc,.md5,.sha1}
# 
# /downloads/
#   Files
# of
#   MOD-XXX/files
#     jar and sources?


# NB This fails unless this first:
# cd somewhere_clean
# export NNN=....
# mkdir -p repository.apache.org/content/repositories/orgapachejena-${NNN}/org/apache/jena
#   otherwise it creates a file in this location then can't mirror below it.
#
# wget -e robots=off --wait 1 --mirror -np \
#     https://repository.apache.org/content/repositories/orgapachejena-${NNN}/org/apache/jena
# mv repository.apache.org/content/repositories/orgapachejena-${NNN} REPO
# rm -rf  repository.apache.org/

REPO=REPO/org/apache/jena
OUT="dist"
DOWNLOAD="download"
SRC_REL="source-release"

# This script collects everything for the incubator/dist/jena area
# for a TDB release. 
# It write a script that will build dist/ from rpo copy.
# Copy to dist/jena to add to the last jena release.

## To manaually sign:
# sha1sum -b FILE | cut -f1 -d' '

ECHO=echo
CPCMD="$ECHO cp"
MKDIR="$ECHO mkdir"
DELDIR="$ECHO rm -rf"

## 
echo "## Initalize"
$DELDIR $OUT
$MKDIR $OUT
$MKDIR $OUT/$DOWNLOAD
$MKDIR $OUT/$SRC_REL


# Copy a file , and its associated asc md5 sha1, to a directory.
#  cpfile FILE DIR
function cpfile
{
    local FILE="$1"
    local DIR="$2"

    local SRC="$REPO/$FILE"
    local DEST="$OUT/$DIR"

    #[ -e "$SRC" ]  || { echo "No such file: $SRC" 2>&1 ; exit 1 ; }
    #[ -d "$DEST" ] || { echo "Not a directory: $DEST" 2>&1 ; exit 1 ; }

    $CPCMD "$SRC" "$DEST"
    #for ext in asc asc.md5 asc.sha1 md5 sha1
    for ext in  asc md5 sha1
    do
	$CPCMD "$SRC.$ext" "$DEST"
    done
}

# Copy source-release files.
# cp_release MODULE VERSION
 
function cp_release
{
    local M="$1"
    local V="$2"
    local D="$M-$V-$inc"

    local SRC="$M/$V-$inc/$M-$V-$inc-source-release"
    local DEST="$SRC_REL/$M-$V-$inc"

    $MKDIR "$OUT/$DEST"
    for ext in zip # tar.gz tar.bz2
    do
	#[ ! -e "$REPO/$SRC.ext" ] && { echo "No such file: $SRC.$ext" ; exit 1 ; }
	cpfile "$SRC.$ext" $DEST
    done
}

# Copy all the maven files 
# jar, sources.jar, javadoc?, 
# cpallfiles MODULE VERSION
function cpallfiles
{
    # /download?

    local M="$1"
    local V="$2"
    local D="$DOWNLOAD/$M-$V-$inc"
    #[ ! -e "$OUT/$D" ] || { echo "Directory exists: $OUT/$D" 2>&1 ; exit 1 ; }

    $MKDIR $OUT/$D
    cpfile "$M/$V-$inc/$M-$V-$inc.jar" $D
    cpfile "$M/$V-$inc/$M-$V-$inc-sources.jar" $D
    if [ -e "$REPO/$M/$V-$inc/$M-$V-$inc-javadoc.jar" ]
    then
	 cpfile "$M/$V-$inc/$M-$V-$inc-javadoc.jar" $D
    else
	$ECHO echo "No javadoc: $REPO/$M/$V-$inc/$M-$V-$inc-javadoc.jar"
    fi
}

## ToDo: automate

V_TDB=0.9.0
inc=incubating

## source-release
cp_release jena-tdb "${V_TDB}"

## Module

echo "## TDB"
cpallfiles jena-tdb "${V_TDB}"

echo "## zip"
M=jena-tdb
V=${V_TDB}
D="$M-$V-$inc"
cpfile $M/$V-$inc/$D-distribution.zip      $DOWNLOAD
cpfile $M/$V-$inc/$D-distribution.tar.gz   $DOWNLOAD

# Distribution

# Fix the name.
for ext in {zip,tar.gz}{,.asc,.md5,.sha1}
do
    $ECHO mv $OUT/$DOWNLOAD/$M-$V-$inc-distribution.$ext $OUT/$DOWNLOAD/apache-$M-$V-$inc.$ext
done
