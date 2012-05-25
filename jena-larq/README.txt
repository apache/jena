LARQ - adding free text searches to SPARQL
------------------------------------------

LARQ is a combination of ARQ and Lucene. It gives ARQ the ability to perform 
free text searches. Lucene indexes are additional information for accessing 
the RDF graph, not storage for the graph itself.

To package LARQ and use the larq.larqbuilder and larq.larq commands run:

  mvn package -Pjar-with-dependencies
  java -cp .:target/jena-larq-1.0.1-SNAPSHOT-jar-with-dependencies.jar larq.larqbuilder --version

The . is to include the Log4j config file in the classpath: log4j.properties

See also:

 - http://jena.apache.org/documentation/larq/
 - http://jena.apache.org/documentation/query/extension.html#propertyFunctions
 - https://svn.apache.org/repos/asf/jena/trunk/jena-larq/
