#!/bin/bash

ROOT="$JENAROOT"
if [ -z "$ROOT" ]
then
    R="$ARQROOT"
    fi
if [ -z "$ROOT" ]
then
    echo "JENAROOT not set" 1>&2
    exit 1
    fi


# ---- Setup
JVM_ARGS=${JVM_ARGS:--Xmx1024M}
# Expand JENAROOT but literal *
JENA_CP="$JENAROOT"'/lib/*'
SOCKS=
LOGGING="-Dlog4j.configuration=file:$JENAROOT/log4j.properties"

java $JVM_ARGS $LOGGING -cp "$JENA_CP" JENA_CMD "$@" 
