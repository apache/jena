#!/bin/sh
set -e

CONFIG="${CONFIG:-/config/config.ttl}"
DB_DIR="${DB_DIR:-/data/DB}"
INPUT_DIR="${INPUT_DIR:-/input}"
JAVA_OPTS="${JAVA_OPTS:-}"

# Step 1: Load data files into TDB2
echo "=== TDB2 Bulk Load ==="
FILES=$(ls "$INPUT_DIR"/*.nq "$INPUT_DIR"/*.ttl "$INPUT_DIR"/*.nt 2>/dev/null || true)
if [ -z "$FILES" ]; then
  echo "No data files found in $INPUT_DIR"
  exit 1
fi
java $JAVA_OPTS -cp /app/jena-fuseki-server.jar tdb2.tdbloader --loc "$DB_DIR" $FILES

# Step 2: Build SHACL Lucene index
echo "=== SHACL Bulk Reindex ==="
java $JAVA_OPTS -cp /app/jena-fuseki-server.jar \
  org.apache.jena.query.text.cmd.shacltextindexer --desc="$CONFIG"

echo "=== Done ==="
