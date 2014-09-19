# Fuseki Quickstart

This page describes how to achieve certain common tasks in the most direct way possible.

_CHECK_



## Running with Apache Tomcat and loading a file.

1. Unpack the distribution.
2. Copy the WAR file into the Aapche tomcat webass dietory, under the name 'fuseki'
3. In a browser, go to `[http://localhost:8080/fuseki/](http://localhost:8080/fuseki)` (detail ssuch as port number depend on the Tomcat setup).
4. Press "", choose "in-memory".
5. Go to "add data" and load the file (single graph).

## Publish an RDF file as a SPARQL endpoint.

1. Unpack the distribution.
2. Run `fuseki-server --file FILE /name`

## Explore a TDB database

1. Unpack the distribution.
2. Run `fuseki-server --loc=DATABASE /name`
3/ In a browser, go to `http://localhost:3030//query.html`

More details on running Fuseki can be found [nearby](fuseki-run.html), including runnign as an operating system service and in 