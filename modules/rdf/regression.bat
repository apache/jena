@echo off
cd ..\..
cmd /K java -cp .;lib\sax2.jar;lib\xerces.jar;lib\rdffilter.jar;lib\jena.jar com.hp.hpl.mesa.rdf.jena.mem.MemRegression
