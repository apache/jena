Apache Jena : Build from Source
===============================

It is not normally necessary to build from source. Apache Jena provides already-built maven artifacts, available from the central maven repositories.  

## Binaries

See http://jena.apache.org/download/

For most usage, there is no requirement to build from source. Use maven
or other build system that can download from the central repositories.

    <dependency>
       <groupId>org.apache.jena</groupId>
       <artifactId>apache-jena-libs</artifactId>
       <type>pom</type>
       <version>X.Y.Z</version>
    </dependency>

The latest Apache Jena Fuseki can be obtained via http://jena.apache.org/download/.

There is also a package of libraries for offline installation.

## Source

Building Jena requires a Java8 JDK, Maven 3, and a network connection.

### Obtain the source

You can get the current development code by cloning:

    git clone https://github.com/apache/jena/

or the Apache primary code repository for Jena:

    git clone https://git-wip-us.apache.org/repos/asf/jena.git

For the signed source of the latest release, go to:

http://apache.org/dist/jena/source/

with previous versions available at:

http://archive.apache.org/dist/jena/source/

These are the formal files that define an Apache Jena release.

### Build the source

Apache Jena uses maven as its build system.

    mvn clean install

A faster, but abbreviated, build of the main modules, including ARQ,
TDB, command line tools and Fuseki2.

    mvn clean install -Pdev

Once the whole of Jena has been built once, individual modules can be incrementally
built using maven in their module directory.

To quickly build the whole project, skipping tests (but building them because that's required) and skipping javadoc generation:

    mvn -DskipTests -Dmaven.javadoc.skip=true clean install

Build only a specific module (e.g. `jena-arq`) and its dependencies

    mvn -pl :jena-arq -am install

Also useful:

`-Drat.skip` Skips checking for license headers; useful during development.

## IDE setup

### Avoid multiple imports

To work with the Jena source in an IDE, it is only necessary to import modules
of interest.

When importing modules, avoid including modules twice. This happens when
including both a module and also its parent POM because the parent POM includes
the module within its directory tree.

This happens if the top level POM is included, as well as `jena-db` and
`jena-fuseki2` as well as `jena-extras` and `jena-jdbc`.

### Avoid output modules

Artifacts that provide shaded jars should not normally be imported, especially
`jena-shaded-guava`. Other shared jars included `jena-fuseki-server` and
`jena-fuseki-fulljar`.

`jena-shaded-guava` will be obtained from a maven repository or from a local run
of mvn.

## Build Notes

The TDB tests on Microsoft Windows use a large amount of temporary disk space.
This is because databases can not be completely deleted until the JVM exits on
this operating system.
