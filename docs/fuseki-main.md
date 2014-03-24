## Apache Jena Fuseki

Apache Jena Fuseki is a SPARQL server.  It can run as a operating system service,
as a Java web application (WAR file), and as a standalone server.  It provides security
(using [Apache Shiro](https://shiro.apache.org/)) and has a user interface for server
monitoring and administration.

It provides the SPARQL 1.1 
[protocols for query and update](http://www.w3.org/TR/sparql11-protocol/)
as well as the [SPARQL Graph Store protocol](http://www.w3.org/TR/sparql11-http-rdf-update/).

Fuseki is tightly integrated with [TDB](..tdb/index.html) to provide a robust,
transactional persistent storage layer, and incorporates [Jena text query](../query/text-query.html)
and [Jena spatial query](query/spatial-query.html).  It can be used to provide the protocol engine
for other RDF query and storage systems.

## Contents

- [Download](#download-fuseki)
- [Getting Started](#getting-started-with-fuseki)
- [Security](fuseki-security.html)
- [Running Fuseki](fuseki-run.html)
  - [As a web application](fuseki-run.html#fuseki-as-a-web-application)
  - [As a service](fuseki-run.html#fuseki-as-a-service)
  - [As a standalone server](fuseki-run.html#fuseki-as-a-standalone server)
- Architecture
  - [Server URI scheme](fuseki-data-services.html)
  - [Server Protocol](fuseki-server-protocol.html)
- [Fuseki Configuration](fuseki-configuration.html)
- [Logging](fuseki-logging.html)
- [How to Contribute](#how-to-contribute)
- Client access
  - [Use from Java](#sparql-java-clients)
  - [SPARQL Over HTTP](soh.html) - scripts to help with data management.
- [Links to Standards](rdf-sparql-standards.html)

The Jena users mailing is the place to get help with Fuseki.  

[Email support lists](/help_and_support/#email-support-lists)

## Download Fuseki

Releases of Apache Jena Fuseki can be downloaded from one of the mirror sites:

[Jena Downloads](/download)

and previous releases are available from [the archive](http://archive.apache.org/dist/jena/).
We strongly recommend that users use the latest official Apache releases of Jena Fuseki in
preference to any older versions or of development builds.

Fuseki requires Java7.

**Fuseki download files**

Filename | Description
--------- | -----------
`fuseki-*VER*.distribution.zip` | Fuseki download, includes everything.
`fuseki-*VER*-server.jar`  | Fuseki server, as an executable jar.
`fuseki-*VER*-server.war`  | Fuseki server, as a web application archive (.war) file.

> _@@ Not ready yet_
It is also available as a Java web application WAR file via maven.

    <dependency>
       <groupId>org.apache.jena</groupId>
       <artifactId>jena-fuseki-webapp</artifactId>
       <type>war</type>
       <version>X.Y.Z</version>
    </dependency>

### Previous releases

While previous releases are available, we strongly recommend that wherever
possible users use the latest official Apache releases of Jena in
preference to using any older versions of Jena.

Previous Apache Jena releases can be found in the Apache archive area
at [http://archive.apache.org/dist/jena](http://archive.apache.org/dist/jena/)

### Development Builds

Regular developement builds of all of Jena are available (these are not formal releases)
from the [Apache snapshots maven repository](https://repository.apache.org/snapshots/org/apache/jena).
This includes packaged builds of Fuseki.

## Getting Started With Fuseki

This section serves as a basic guide to getting a Fuskei server running on your local machine.
See [other sections](fuseki-run.html) for complete coverage of all the deployment methods for Fuseki.

## How to Contribute

We welcome contributions towards making Jena a better platform for semantic web and linked data applications.
We appreciate feature suggestions, bug reports and patches for code or documentation.

See "[Getting Involved](/getting_involved/index.html)" for ways to contribute to Jena and Fuseki, 
including patches and making github pull-requests.

### Source code

The development codebase is available from SVN.

[https://svn.apache.org/repos/asf/jena/](https://svn.apache.org/repos/asf/jena/)

This is mirrored as a git repository on github:

[https://github.com/apache/jena](https://github.com/apache/jena)

