#!/bin/bash

S=":"
if [ "$OSTYPE" == "cygwin" ]; then S=";"; fi

LIBS="$(cat<<EOF
arq*.jar
slf4j-api-1.5.6.jar
slf4j-log4j12-1.5.6.jar
icu4j_3_4.jar
jena.jar
json.jar
junit-4.5.jar
iri.jar
log4j-1.2.12.jar
lucene-core-2.3.1.jar
wstx-asl-3.0.0.jar
stax-api-1.0.jar
xercesImpl.jar
)"

CP=""
for jar in $LIBS
do
  # File expansion.
  for j in lib/$jar
  do
    if [ "$CP" == "" ]
	then
	CP="${j}"
    else
	CP="$CP${S}${j}"
    fi
  done
done

echo $CP
## java  -cp "$CP" junit.textui.TestRunner com.hp.hpl.jena.query.test.ARQTestSuite

java -version
java -cp "$CP" arq.qtest --all
