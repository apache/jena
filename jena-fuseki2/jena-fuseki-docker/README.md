# Apache Jena Fuseki Docker Tools

This package contains a Dockerfile, docker-compose file, and helper scripts to
create a docker container for Apache Jena Fuseki.

The docker container is based on 
[Fuseki main](https://jena.apache.org/documentation/fuseki2/fuseki-main)
for running a SPARQL server.

There is no UI - all configuration is by command line and all usage by via the
network protocols.

Databases can be mounted outside the docker container so they are preserved when
the container terminates.

This build system allows the user to customize the docker image.

The docker build downloads the server binary from 
[Maven central](https://repo1.maven.org/maven2/org/apache/jena/jena-fuseki-server/),
checking the download against the SHA1 checksum.

## Database

There is a volume mapping "./databases" in the current directory into the server.
This can be used to contain databases outside, but accessible to, the container
that do not get deleted when the container exits.

See examples below.

## Build

Choose the version number of Apache Jena release you wish to use. This toolkit
defaults to the version of the overall Jena release it was part of. It is best
to use the release of this set of tools from the same release of the desired
server.

    docker-compose build --build-arg JENA_VERSION=3.16.0

Note the build command must provide the version number.

## Test Run

`docker-compose run` cam be used to test the build from the previous section.

Examples:

Start Fuseki with an in-memory, updatable dataset at http://<i>host</i>:3030/ds

    docker-compose run --rm --service-ports fuseki --mem /ds

Load a TDB2 database, and expose, read-only, via docker:

    mkdir -p databases/DB2
    tdb2.tdbloader --loc databases/DB2 MyData.ttl
    # Publish read-only
    docker-compose run --rm --name MyServer --service-ports fuseki --tdb2 --loc databases/DB2 /ds

To allow update on the database, add `--update`. Updates are persisted.

    docker-compose run --rm --name MyServer --service-ports fuseki --tdb2 --update --loc databases/DB2 /ds

See
[fuseki-configuration](https://jena.apache.org/documentation/fuseki2/fuseki-configuration.html)
for more information on command line arguments.

To use `docker-compose up`, edit the `docker-compose.yaml` to set the Fuseki
command line arguments appropriately.

## Layout

The default layout in the container is:

| Path  | Use | 
| ----- | --- |
| /opt/java-minimal | A reduced size Java runtime                      |
| /fuseki | The Fuseki installation                                    |
| /fuseki/log4j2.properties | Logging configuration                    |
| /fuseki/databases/ | Directory for a volume for persistent databases |

## Setting JVM arguments

Use `JAVA_OPTIONS`:

    docker-compose run --service-ports --rm -e JAVA_OPTIONS="-Xmx1048m -Xms1048m" --name MyServer fuseki --mem /ds

## Docker Commands

If you prefer to use `docker` directly:

Build:

    docker build --force-rm --build-arg JENA_VERSION=3.16.0 -t fuseki .

Run:

    docker run -i --rm -p "3030:3030" --name MyServer -t fuseki --mem /ds

With databases on a bind mount to host filesystem directory:

    MNT="--mount type=bind,src=$PWD/databases,dst=/fuseki/databases"
    docker run -i --rm -p "3030:3030" $MNT --name MyServer -t fuseki --tdb2 --update --loc databases/DB2 /ds

## Version specific notes:

* Versions of Jena up to 3.14.0 use Log4j1 for logging. The docker will build will ignore
   the log4j2.properties file
* Version 3.15.0: When run, a warning will be emitted.  
  `WARNING: sun.reflect.Reflection.getCallerClass is not supported. This will impact performance.`  
  This can be ignored.
