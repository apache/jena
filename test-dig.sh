#!/bin/bash
# Test script for running the DIG test suite using the Pellet
# reasoner as a default
# CVS $Id: test-dig.sh,v 1.1 2005-10-10 11:08:25 ian_dickinson Exp $

if [ "$1" != "-nostart" ]; then
    # use default location for pellet unless env PELLET_HOME is set
	pHome=${PELLET_HOME:-../pellet-1.3-beta}
	echo "Starting reasoner in $pHome ..."
	java -Xss4m -Xms30m -Xmx200m -classpath $pHome/lib/pellet.jar org.mindswap.pellet.dig.PelletDIGServer -port 8081 &
	pJob=`jobs -p`
	if [ "$pJob" == "" ]; then
		echo "Pellet background process does not appear to have started"
		echo Aborting
		exit 1
	fi
	sleep 2
fi

S=":"
if [ "$OSTYPE" == "cygwin" ]; then S=";"; fi

CP=""
for jar in ./lib/*.jar
do
  if [ "$CP" == "" ]
  then
      CP="${jar}"
  else
      CP="$CP${S}${jar}"
      fi
  done

#echo $CP

java -version
java -classpath "$CP" junit.textui.TestRunner ${1:-com.hp.hpl.jena.reasoner.dig.test.TestPackage}

if [ "$1" != "-nostart" ]; then
	echo Terminating pellet job $pJob ...
	kill $pJob
fi
