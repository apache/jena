#!/bin/bash
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

V_FUSEKI=0.2.1
inc=incubating

## ToDo: automate

V_TDB=0.9.0
inc=incubating

M=jena-tdb
V="${V_TDB}-$inc"
D="$M-$V"

$MKDIR "$OUT/$M-$V"
cpfile "$M/$V/$M-$V-source-release.zip"   $D
cpfile "$M/$V/$M-$V-distribution.zip"     $D
cpfile "$M/$V/$M-$V-distribution.tar.gz"  $D
