# Fuseki Logging

Fuseki logs separately from any web application server it is used in.  
Logging is enabaled by default.

The server uses a number of logs, 
which can be controlled with `log4j`.
A `log4j.properties` file in the FUSEKI_BASE directory overrides the built configuration.

| Full Log name | Usage |
|---------------|-------|
| org.apache.jena.fuseki.Fuseki   | The HTTP request log     |
| org.apache.jena.fuseki.Admin    | Administration operations |
| org.apache.jena.fuseki.Builder  | Dataset and service build operations |
| org.apache.jena.fuseki.Config   | Configuration            |
| org.apache.jena.fuseki.Server   | General Server Messages  |


## Logrotate

Below is an example logrotate(1) configuration (to go in `/etc/logrotate.d`) 
based on the default service mode deployment.
It rotates the logs once a month, compresses logs on rotation and keeps them for 6 months.

It uses `copytruncate`.  This may lead to at most one broken log file line.

    /etc/fuseki/logs/fuseki.log
    {
        compress
        monthly
        rotate 6
        create
        missingok
        copytruncate
        # Date in extension.
        dateext
        # No need
        # delaycompress
    }

