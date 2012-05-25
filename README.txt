ARQ : Jena SPARQL System
========================

ARQ is a complete implementation of SPARQL that can query any Jena model
or graph, including ones stored in databases.

Download
--------

Also available via SVN:
https://svn.apache.org/repos/asf/jena/trunk/jena-arq

Maven:
GroupId: org.apache.jena
ArtifactId: jena-arq

Development repository:
  https://repository.apache.org/content/repositories/snapshots/org/apache/jena/ 

Online demos and services
-------------------------

http://www.sparql.org/

including a SPARQL validator
http://www.sparql.org/validator.html

Documentation
-------------

http://jena.apache.org/documentation/query/index.html

Installation
------------

The apache-jena distribution includes ARQ and all it's dependencies.  It
also includes scripts to run the command line tools.

http://www.apache.org/dist/jena/

Unpack zip : it unpacks into a directory, including the version number.

Set environment variable ARQROOT to the path of this direrctory.

Maven Distribution
------------------
The maven repository contains the following files (for version VER)

jena-arq-VER.jar                 ARQ jar
jena-arq-VER-javadoc.jar         Javadoc
jena-arq-VER-sources.jar         Sources

jena-arq-VER-tests.jar           ARQ testing code
jena-arq-VER-test-sources.jar    Sourecs for test code
jena-arq-VER-tests.zip           Datafiles for tests, and test scripts.

jena-arq-VER.pom                 ARQ POM

jena-arq-VER.zip                 The complete distribution file (if available)
jena-arq-VER-source-release.zip  Complete copy of the codebase at the release.

To run tests, you will need 'jena-arq-VER.jar', 'jena-arq-VER-tests.jar' and all
dependences and to unpack 'jena-arq-VER-tests.zip' to get the "testing" data
directory and test scripts.

Or download the distribution 'jena-arq-VER.zip'. 

Running from the command line
-----------------------------

Some bash and bat scripts are provided to try out SPARQL queries.

In the apache-jena distribution, set JENAROOT.

    export JENAROOT=$PWD

Second, ensure all scripts are executable

    chmod u+x $JENAROOT/bin/*

Command line applications:

sparql   -- Run a SPARQL query.  A wrapper around 'query'.
arq      -- Run an ARQ query.  A wrapper around 'query'.
query    -- Run a query. 
qparse   -- Parse a query
qtest    -- Run tests
rset     -- Read and write result sets (RDF, XML, JSON, text (out only))

For the 'query' command, files ending .rq are assumed to be SPARQL queries
and files ending .arq are ARQ queries, unless the syntax is given
explicitly.  Unknown files are assumed to be SPARQL queries.

sparql --data <some data file> --query query 
sparql --data <some data file> 'Query string'

Default output is a text table for SELECT queries, an RDF model for
DESCRIBE and CONSTRUCT.

Run the script with no argument to get a usage message.

Running commands as Java
------------------------

Set the classpath to "$JENAROOT/lib/*" (Linux) or "$JENAROOT\lib\*;" (Windows).

The arq command line application is:
java -cp ... arq.query ....

ARQ.Net
-------

ARQ has been run under IKVM (http://www.ikvm.net/), using IKVM to
translate the bytecodes to IL and using IKVM as a JVM.

Changes
-------

See ChangeLog.txt

Support and Questions
---------------------

Comments and questions about ARQ to the Jena mailing list 
    users@jena.apache.org

General questions and comments on SPARQL to jena-dev or
    public-sparql-dev@w3.org
    
Formal comments specific to the SPARQL query language,
protocol or result set documents to:
	public-rdf-dawg-comments@w3.org
