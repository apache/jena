@echo off

set CP=lib/antlr-2.7.5.jar;lib/arq.jar;lib/arq-extra.jar;lib/concurrent.jar;lib/iri.jar;lib/icu4j_3_4.jar;lib/jena.jar;lib/jenatest.jar;lib/json.jar;lib/junit.jar;lib/log4j-1.2.12.jar;lib/lucene-core-2.0.0.jar;lib/commons-logging-1.1.jar;lib/xercesImpl.jar;lib/xml-apis.jar;lib/wstx-asl-3.0.0.jar;lib/stax-api-1.0.jar

java -version

java -classpath %CP% junit.textui.TestRunner com.hp.hpl.jena.test.TestPackage
