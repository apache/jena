ARQ : Jena SPARQL System
========================

Apache Jena is an effort undergoing incubation at The Apache Software Foundation (ASF), 
sponsored by the Incubator PMC. Incubation is required of all newly accepted projects 
until a further review indicates that the infrastructure, communications, and decision 
making process have stabilized in a manner consistent with other successful ASF projects.
While incubation status is not necessarily a reflection of the completeness or stability 
of the code, it does indicate that the project has yet to be fully endorsed by the ASF.


Website: http://incubator.apache.org/jena/ARQ
Old ARQ website: http://openjena.org/ARQ

ARQ is a complete implementation of SPARQL that can query any Jena model
or graph, including ones stored in databases.

Download
--------

Old Releases : SourceForge, Project Jena : Package ARQ
http://sourceforge.net/project/showfiles.php?group_id=40417&package_id=143808

Also available via SVN:
https://svn.apache.org/repos/asf/incubator/jena/

Maven:
GroupId: com.hp.hpl.jena
ArtifactId: arq

(migrating to Aapche)
Release repository: http://openjena.org/repo
  Mirrored to http://repo1.maven.org/
Development repo: http://openjena.org/repo-dev

See below of maven repository contents.

Online demos and services
-------------------------

http://www.sparql.org/

including a SPARQL validator
http://www.sparql.org/validator.html

Documentation
-------------

Included in the download in doc/index.html and online at http://openjena.org/ARQ

Installation
------------

Unpack zip : it unpacks into a directory, including the version number.
Set environment variable ARQROOT to the path of this direrctory.

ARQ includes all the libraries necessary in lib/
ARQ includes a copy of Jena, the one it is tested against.

Put each and every the jar file in lib/ on your classpath.

Maven Distribution
------------------
The maven repository contains the following files (for version VER)

arq-VER.jar                 ARQ jar
arq-VER-javadoc.jar         Javadoc
arq-VER-sources.jar         Sources

arq-VER-tests.jar           ARQ testing code
arq-VER-test-sources.jar    Sourecs for test code
arq-VER-tests.zip           Datafiles for tests, and test scripts.

arq-VER.pom                 ARQ POM

arq-VER.zip                 The complete distribution file

To run tests, you will need 'arq-VER.jar', 'arq-VER-tests.jar' and all
dependences and to unpack 'arq-VER-tests.zip' to get the "testing" data
directory and test scripts.

Or download the distribution 'arq-VER.zip'. 

Running from the command line
-----------------------------

Some bash and bat scripts are provided to try out SPARQL queries:

First, set environment variable ARQROOT to the root of the unzipped distibution.

    export ARQROOT=$PWD

Second, ensure all scripts are executable

    chmod u+x $ARQROOT/bin/*

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

See doc/cmds.html for details.  Check the suitablity of scripts before running them.

Running commands as Java
------------------------

Put all the jars files in lib/ on your classpath.  All of them, including
the version of jena.jar included.

The arq command line application is:
java -cp ... arq.query "$@"

Running the test suite:
java -cp ... arq.qtest --all

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
    jena-users@incubator.apache.org

General questions and comments on SPARQL to jena-dev or
    public-sparql-dev@w3.org
    
Formal comments specific to the SPARQL query language,
protocol or result set docuemnts to:
	public-rdf-dawg-comments@w3.org
