#!/bin/bash

# Perform the bulk work to convert ARQ to using Jena/Java5.

## echo src/com/hp/hpl/jena/sparql/engine/iterator/QueryIterTriplePattern.java |

find src -name \*java -print -o -name .svn -prune -o -name CVS -prune | \
    xargs -n 1 perl -i.bak -p \
    -e 's!ExtendedIterator<([^>]*)>!ExtendedIterator/*<$1>*/! ; s!ClosableIterator<([^>]*)>!ClosableIterator/*<$1>*/!'
