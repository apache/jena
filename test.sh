#!/bin/bash
# run Jena tests; try and guess path separator.

S=":"
if [ "$OSTYPE" == "cygwin" ]; then S=";"; fi

LIBS="$(cat<<EOF
antlr.jar
commons-logging-api.jar
commons-logging.jar
concurrent.jar
icu4j.jar
jakarta-oro-2.0.5.jar
jena.jar
junit.jar
log4j-1.2.7.jar
rdf-api-2001-01-19.jar
xercesImpl.jar
xmlParserAPIs.jar
EOF
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

##echo $CP

java -classpath "$CP"  junit.textui.TestRunner ${1:-com.hp.hpl.jena.test.TestPackage}
