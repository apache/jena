@echo off

@REM Fix me.
## set CP=lib/slf4j-api-1.5.6.jar;lib/slf4j-log4j12-1.5.6.jar;lib/icu4j_3_4.jar;lib/iri.jar;lib/jena.jar;lib/jenatest.jar;lib/json.jar;lib/junit-4.5.jar;lib/log4j-1.2.12.jar;lib/lucene-core-2.3.1.jar;lib/stax-api-1.0.jar;lib/wstx-asl-3.0.0.jar;lib/xercesImpl.jar
#Java 6.
set CP=lib/*

java -classpath %CP% junit.textui.TestRunner com.hp.hpl.jena.test.TestPackage
