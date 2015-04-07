#!/bin/bash
#   Licensed to the Apache Software Foundation (ASF) under one or more
#   contributor license agreements.  See the NOTICE file distributed with
#   this work for additional information regarding copyright ownership.
#   The ASF licenses this file to You under the Apache License, Version 2.0
#   (the "License"); you may not use this file except in compliance with
#   the License.  You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.

extensions="rdf ttl owl nt nquads"
PATTERNS=""
for e in $extensions ; do
  PATTERNS="$PATTERNS *.$e *.$e.gz"
done

if [ $# -eq 0 ] ; then 
  echo "$0 [DB] [PATTERN ...]" 
  echo "Load one or more RDF files into Jena Fuseki TDB database DB."
  echo ""
  echo "Current directory is assumed to be /staging"
  echo ""
  echo 'PATTERNs can be a filename or a shell glob pattern like *ttl'
  echo ""
  echo "If no PATTERN are given, the default patterns are searched:"
  echo "$PATTERNS"
  exit 0
fi

cd /staging 2>/dev/null || echo "/staging not found" >&2
echo "Current directory:" $(pwd)

DB=$1
shift

if [ $# -eq 0 ] ; then 
  patterns="$PATTERNS"
else
  patterns="$@"
fi

files=""
for f in $patterns; do
  if [ -f $f ] ; then
    files="$files $f"
  else 
    if [ $# -gt 0 ] ; then 
      # User-specified file/pattern missing
      echo "WARNING: Not found: $f" >&2
    fi
  fi
done

if [ "$files" == "" ] ; then
  echo "No files found for: " >&2
  echo "$patterns" >&2
  exit 1
fi

mkdir -p $FUSEKI_BASE/databases/

echo "#########"
echo "Loading to Fuseki TDB database $DB:"
echo ""
echo $files
echo "#########"


exec $FUSEKI_HOME/tdbloader --loc=$FUSEKI_BASE/databases/$DB $files
