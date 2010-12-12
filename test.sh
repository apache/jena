#!/bin/bash

S=":"
if [ "$OSTYPE" == "cygwin" ]; then S=";"; fi

X=$(echo lib/arq-*-tests.jar)
[ -e "$X" ] || { echo "No such tests jar" 1>&2 ; exit 1 ; }
VER=$X
VER=${VER#lib/arq-}
VER=${VER%-tests.jar}
echo "Test: ARQ version: $VER"

CP=""
for jar in lib/*.jar
do
  if [ "$CP" == "" ]
      then
      CP="${jar}"
  else
      CP="$CP${S}${jar}"
  fi
done

## echo "Classpath: $CP"
## java  -cp "$CP" junit.textui.TestRunner com.hp.hpl.jena.query.test.ARQTestSuite

java -version
java -cp "$CP" arq.qtest --all
