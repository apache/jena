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

To build the whole project, quickly and dirtily, i.e, skipping tests (but building them because that's required) and skipping javadoc generation (because it might fail):

    mvn -DskipTests -Dmaven.javadoc.skip=true clean install

Build only a specific module (e.g. `jena-arq`) and its dependencies

    mvn -pl :jena-arq -am install

Also useful:

`-Denforcer.skip` If the maven version is too old (e.g. server deployment), skipping the enforcer may still result in a successful build

`-Drat.skip` Skips checking for license headers; useful during development.


## Build Notes

The TDB tests on Microsoft Windows use a large amount of temporary disk space.
This is because databases can not be completely deleted until the JVM exits on
this operating system.
