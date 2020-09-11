#!/bin/sh

## Licensed under the terms of http://www.apache.org/licenses/LICENSE-2.0
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# This is an ash/dash script (it uses "local"), not a bash script.
# It can run in an Alpine image durign a docker build.
#
# The advantage over using docker ADD is that it checks
# whether download file is already present and does not
# download each time.
#
# Shell script to download URL and check the checksum

USAGE="Usage: $(basename "$0") --chksum [sha1|sha512] URL"

if [ $# -eq 0 ]
then
    echo "$USAGE" 2>&1
    exit 1
fi

CHKSUM_TYPE='unset'

while [ $# -gt 0 ] ; do
    case "$1" in
	--chksum|-chksum|-sha|--sha)
	    if [ $# -lt 2 ]
	    then
		echo "$USAGE" 1>&2
		exit 1
	    fi
	    CHKSUM_TYPE=$2
	    shift
	    shift
	    ;;
	-h|--help)
	    echo "$USAGE" 1>&2
	    exit 0
	    ;;
	-*)
	    echo "$USAGE" 1>&2
	    exit 1
	    ;;
	*)
	    if [ $# -ne 1 ]
	    then
		echo "$USAGE" 1>&2
		exit 1
	    fi
	    URL="$1"
	    shift
	    ;;
    esac
done

case "${CHKSUM_TYPE}" in
    unset)
	echo "$USAGE" 1>&2
	exit 1
	;;
    sha*|md5) ;;
    *)
	echo "Bad checksum type: '$CHKSUM_TYPE' (must start 'sha' or be 'md5')" 2>&1
	exit 1	 
	;;
esac

## ---- Script starts ----

ARTIFACT_URL="${URL}"
ARTIFACT_NAME="$(basename "$ARTIFACT_URL")"

# -------- Checksum details

CHKSUM_EXT=".${CHKSUM_TYPE}"
CHKSUM_URL="${ARTIFACT_URL}${CHKSUM_EXT}"
CHKSUM_FILE="${ARTIFACT_NAME}${CHKSUM_EXT}"
CHKSUMPROG="${CHKSUM_TYPE}sum"
# --------

CURL_FETCH_OPTS="-s -S --fail --location --max-redirs 3"
if false
then
    echo "ARTIFACT_URL=$ARTIFACT_URL"
    echo "CHKSUM_URL=$CHKSUM_URL"
fi

download() { # URL
    local URL="$1"
    local FN="$(basename "$URL")"
    if [ ! -e "$FN" ]
    then
	echo "Fetching $URL"
	curl $CURL_FETCH_OPTS "$URL" --output "$FN" \
	    || { echo "Bad download of $FN" 2>&1 ; return 1 ; }
    else
	echo "$FN already present"
    fi
    return 0
}

checkChksum() { # Filename checksum
    local FN="$1"
    local CHKSUM="$2"
    if [ ! -e "$FN" ]
    then
	echo "No such file: '$FN'" 2>&1
	exit 1
    fi
    # NB Two spaces required for busybox
    echo "$CHKSUM  $FN" | ${CHKSUMPROG} -c > /dev/null
}

download "$ARTIFACT_URL" || exit 1

if [ -z "$CHKSUM" ]
then
    # Checksum not previously set.
    # Extract from file, copes with variations in content (filename or not)
    download "$CHKSUM_URL" || exit 1
    CHKSUM="$(cut -d' ' -f1 "$CHKSUM_FILE")"
fi

checkChksum "${ARTIFACT_NAME}" "$CHKSUM"
if [ $? = 0 ]
then
    echo "Good download: $ARTIFACT_NAME"
else
    echo "BAD download !!!! $ARTIFACT_NAME"
    echo "To retry: delete downloaded files and try again"
    exit 1
fi
