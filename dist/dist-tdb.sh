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

# This script collects everything for the 
# incubator/dist/jena area for a TDB release.

# You need a copy of the maven-built-artifacts, toegther with the .asc files.
# The .asc files should be created when the artifacts were created.
# In emergencies, create the .asc 
# gpg --batch --armour --detach-sign F (creates $F.asc or use -o)

# Layout: simple:
# /MOD-VER/

# Set the REPO, down to the 
# e.g. ~/.m2/repository
REPO=${REPO:-}
if [ -z "$REPO" ]
then
    echo "Environment variable REPO not set" 1>&2
    echo "(set to the root of your maven local repository e.g. /home/user/.m2/repository )" 1>&2
    exit 1
    fi
REPO="$REPO/org/apache/jena"
OUT="dist"

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
    chksum "$DEST" "$BASE"
}

chksum() # dir file
{
    local DIR="$1"
    local FILE="$2"
    $ECHO "("
    $ECHO "  cd $DIR > /dev/null" 
    $ECHO "  md5sum $FILE > $FILE.md5"
    $ECHO "  sha1sum $FILE > $FILE.sha1"
    $ECHO ")"
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

## Just the distribution and source-release
$MKDIR "$OUT/$M-$V"
cpfile "$M/$V/$M-$V-source-release.zip"   $D
cpfile "$M/$V/$M-$V-distribution.zip"     $D
cpfile "$M/$V/$M-$V-distribution.tar.gz"  $D



## ## If include maven artifacts and distribution.
## echo
## echo "## TDB"
## cpallfiles jena-tdb "${V_TDB}"


## ## If include a separate different copy of the distribution for easy finding.
## if [ "$DOWNLOAD_COPY" = 1 ]
## then
##     # Distribution
##     echo
##     echo "## zip"
##     M=jena-tdb
##     V=${V_TDB}
##     D="$M-$V-$inc"
##     cpfile $M/$V-$inc/$D-distribution.zip           $DOWNLOAD
##     cpfilemaybe $M/$V-$inc/$D-distribution.tar.gz   $DOWNLOAD
##     cpfilemaybe $M/$V-$inc/$D-distribution.tar.bz2  $DOWNLOAD
## 
##     echo
##     echo "# Distribution"
##     # Fix the name.
## 
##     for ext1 in zip tar.gz # tar.bz2
##     do
## 	for ext2 in "" .asc .md5 .sha1
## 	do
## 	    ext="$ext1$ext2"
## 	    F=$OUT/$DOWNLOAD/$D-distribution.$ext
## 	    $ECHO mv $F $OUT/$DOWNLOAD/apache-$D-distribution.$ext
## 	done
##     done
## fi


