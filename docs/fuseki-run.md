# Running Fuseki

Fuseki can be run in three ways

* [As a standalone server](#fuseki-as-a-standalone-server)
* [As a Web Application](#fuseki-as-a-web-application) inside a container such as Apache Tomcat or Jetty.
* [As a service](#fuseki-as-a-service) run by the operation system, for example, started when the machine boots.

@@caution: it is possible that the way these three forms are delivered will change
internally but the scripts used here will retain the same interface.

See "[Fuseki Configuration](fuseki-configuration.html)" for information on how to provides datasetsand configure services.

## Fuseki as a Standalone Server

This is running Fuseki from the command line.

    fuseki-server [--mem | --loc=DIR] [[--update] /NAME]

    # Fuseki v1 style compatibility
    fuseki-server --config=CONFIG

where `/NAME` is the dataset publishing name at this server in URI space.

See `fuseki-server --help` for details of more arguments.

`FUSEKI_BASE`, the runtime area for the server instance, defaults to the `run/` directory of the current directory.

Fuseki v2 supports the same style of configuration file as Fuseki v1 but it is better to separate the data service definitions from the server configuration with one definition per file in `FUSEKI_BASE/configuration`; see "[Fuseki Configuration](fuseki-configuration.html)". 

If you get the error message `Can't find jarfile to run` then you either need to put a copy of `fuseki-server.jar` in the current directory or set the environment variable `FUSEKI_HOME` to point to an unpacked Fuseki distribution.

Unlike Fuseki v1, starting with no dataset and no configuration is possible.
Datasets can be added from the admin UI to a running server.

## Fuseki as a Web Application

Fuseki can run from a [WAR](http://en.wikipedia.org/wiki/WAR_%28file_format%29) file.

`FUSEKI_HOME` is not applicable.

`FUSEKI_BASE` defaults to `/etc/fuseki` which must be a writeable directory.  It is initialised the first time Fuseki runs, including a [Apache Shiro](http://shiro.apache.org/) security file but
this is only intended as a starting point.  It restricts use of the admin UI to the local machine.

## Fuseki as a Service

Fuseki can run as an operating system service, started when the server machine boots.
The script `fuseki` is a Linux `init.d` with the common secondary arguments of `start` and `stop`.

Process arguments are read from `/etc/default/fuseki` including `FUSEKI_HOME` and `FUSEKI_BASE`.
`FUSEKI_HOME` should be the directory where the distribution was unpacked.
