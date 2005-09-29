@echo off

set CP=lib/antlr-2.7.5.jar;lib/arq.jar;lib/concurrent.jar;lib/icu4j.jar;lib/jakarta-oro-2.0.8.jar;lib/jena.jar;lib/jenatest.jar;lib/junit.jar;lib/log4j-1.2.12.jar;lib/commons-logging-api.jar;lib/commons-logging.jar;lib/xercesImpl.jar;lib/xml-apis.jar;lib/stax-1.1.1-dev.jar;lib/stax-api-1.0.jar

java -version

java -classpath %CP% junit.textui.TestRunner com.hp.hpl.jena.test.TestPackage
