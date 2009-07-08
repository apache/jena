#!/bin/bash
# run Jena tests; try and guess path separator.

S=":"
if [ "$OSTYPE" == "cygwin" ]; then S=";"; fi

CP=""

# File name expansion in a scrip.
X=$(echo lib/jena-*-tests.jar)
[ -e "$X" ] || { echo "No such tests jar" 1>&2 ; exit 1 ; }

VER=$X
VER=${VER#lib/jena-}
VER=${VER%-tests.jar}
echo "Test: Jena version: $VER"

#CP="lib/jena-$VER.jar${S}lib/jena-$VER-tests.jar
CP=""

for jar in lib/*.jar
do
  # [ -e "$jar" ] || echo "No such jar: $jar" 1>&2

  if [ "$CP" == "" ]
  then
      CP="${jar}"
  else
      CP="$CP${S}${jar}"
      fi
  done

#echo $CP

#SOCKS=-DsocksProxyHost="<your socks server>"

java -version

java -classpath "$CP" $SOCKS junit.textui.TestRunner ${1:-com.hp.hpl.jena.test.TestPackage}
