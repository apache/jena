#!/bin/bash

S=":"
if [ "$OSTYPE" == "cygwin" ]; then S=";"; fi

LIBS="$(cat<<EOF
antlr-2.7.5.jar
arq.jar
arq-extra.jar
commons-logging-1.1.1.jar
concurrent.jar
icu4j_3_4.jar
jena.jar
json.jar
junit.jar
iri.jar
log4j-1.2.12.jar
lucene-core-2.3.1.jar
wstx-asl-3.0.0.jar
stax-api-1.0.jar
xercesImpl.jar
xml-apis.jar
)"

CP=""
for jar in $LIBS
do
  jar="lib/${jar}"
  if [ "$CP" == "" ]
  then
      CP="${jar}"
  else
      CP="$CP${S}${jar}"
      fi
  done

## echo $CP
## java  -cp "$CP" junit.textui.TestRunner com.hp.hpl.jena.query.test.ARQTestSuite

java -version
java -cp "$CP" arq.qtest --all
