#!/bin/sh
## Licensed to the Apache Software Foundation (ASF) under one
## or more contributor license agreements.  See the NOTICE file
## distributed with this work for additional information
## regarding copyright ownership.  The ASF licenses this file
## to you under the Apache License, Version 2.0 (the
## "License"); you may not use this file except in compliance
## with the License.  You may obtain a copy of the License at
##
##     http://www.apache.org/licenses/LICENSE-2.0
##
## Unless required by applicable law or agreed to in writing, software
## distributed under the License is distributed on an "AS IS" BASIS,
## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
## See the License for the specific language governing permissions and
## limitations under the License.

# You need a copy of the maven-built-artifacts, toegther with the .asc files.
# gpg --batch --armour --detach-sign F (creates $F.asc or use -o)
# sha1sum $F > $F.sha1
# md5sum $F > $F.md5

# Layout: simple:
# /MOD-VER/

# Set the REPO, down to the 
# e.g. ~/.m2/repository
REPO=${REPO:-}
if [ -z "$REPO" ]
then
    echo "REPO not set" 1>&2
    exit 1
    fi
REPO="$REPO/org/apache/jena"
OUT="dist"

# This script collects everything for the incubator/dist/jena area
# for a TDB release. It write a script that will build dist/.

ECHO=echo
CPCMD="$ECHO cp"
MKDIR="$ECHO mkdir -p"
DELDIR="$ECHO rm -rf"

echo "## Initalize"
$DELDIR $OUT
$MKDIR $OUT

# Copy a file, and its associated asc, to a directory.
# Create .md5 and .sha1 files.
# cpfile FILE DIR
cpfile()
{
    local FILE="$1"
    local DIR="$2"

    local SRC="$REPO/$FILE"
    local DEST="$OUT/$DIR"

    [ -e "$SRC" ]  || { echo "No such file: $SRC" 2>&1 ; exit 1 ; }
    #[ -d "$DEST" ] || { echo "Not a directory: $DEST" 2>&1 ; exit 1 ; }

    $CPCMD "$SRC" "$DEST"
    $CPCMD "${SRC}.asc" "$DEST"
    local BASE="$(basename $FILE)"
    $ECHO "sha1sum $SRC > $DEST/${BASE}.sha1"
    $ECHO "md5sum  $SRC > $DEST/${BASE}.md5"
}

# Copy a file if it exists else add a comment
cpfilemaybe()
{
    local FILE="$1"
    local DIR="$2" 
    local SRC="$REPO/$FILE"


    if [ -e "$SRC" ]
    then
	cpfile "$FILE" "$DIR"
    else
	echo "# ** Skipped: $FILE" 2>&1
    fi

}

# Copy source-release files.
# cp_release MODULE VERSION
 
cp_release()
{
    local M="$1"
    local V="$2"
    local D="$M-$V-$inc"

    local SRC="$D/$M-$V-$inc-source-release"
    local DEST="$M-$V-$inc"

    $MKDIR "$OUT/$DEST"
    for ext in zip # tar.gz tar.bz2
    do
	#[ ! -e "$REPO/$SRC.ext" ] && { echo "No such file: $SRC.$ext" 2>&1 ; exit 1 ; }
	cpfile "$SRC.$ext" $DEST
    done
}

# Copy all the maven files 
# jar, sources.jar, javadoc?, 
# cpallfiles MODULE VERSION
cpallfiles()
{
    # /download?

    local M="$1"
    local V="$2"
    local D="$BINARIES/$M-$V-$inc"
    #[ ! -e "$OUT/$D" ] || { echo "Directory exists: $OUT/$D" 2>&1 ; exit 1 ; }

    $MKDIR $OUT/$D

    for ext in zip tar.gz tar.bz2
    do
	cpfilemaybe "$M/$V-$inc/$M-$V-$inc-distribution.${ext}" $D
    done

}

## ToDo: automate

V_TDB=0.9.0
inc=incubating

## source-release
echo
echo "# source-release"

M=jena-tdb
V="${V_TDB}-$inc"
D="$M-$V"

$MKDIR "$OUT/$M-$V"
cpfile "$M/$V/$M-$V-source-release.zip"   $D
cpfile "$M/$V/$M-$V-distribution.zip"     $D
cpfile "$M/$V/$M-$V-distribution.tar.gz"  $D
