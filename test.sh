#!/bin/bash
# run Jena tests; try and guess path separator.
export S=":"
if [ "$OSTYPE" == "cygwin" ]; then export S=";"; fi
java -classpath "lib/antlr.jar${S}lib/concurrent.jar${S}lib/db3-3-11.jar${S}lib/icu4j.jar${S}lib/jakarta-oro-2.0.5.jar${S}lib/jena.jar${S}lib/junit.jar${S}lib/log4j-1.2.7.jar${S}lib/rdf-api-2001-01-19.jar${S}lib/xercesImpl.jar${S}lib/xmlParserAPIs.jar" junit.textui.TestRunner com.hp.hpl.jena.test.TestPackage
