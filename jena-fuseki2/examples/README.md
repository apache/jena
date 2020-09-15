---
title: Fuseki: Configuration file examples.
---

This directory includes some examples - they will need to be adapted to
local requirements.

_Configuration: Named services_

* [config-1-mem.ttl](config-1-mem.ttl)
* [config-2-mem-old.ttl](config-2-mem-old.ttl)

_Configuration: Unnamed services_

* [config-3-dataset-endpoints.ttl](config-3-dataset-endpoints.ttl)

_Single endpoint "sparql" with multiple operations

* [config-4-endpoint-sparql.ttl](config-4-endpoint-sparql.ttl)

_TDB examples_

* [config-tdb1.ttl](config-tdb1.ttl)
* [config-tdb2.ttl](config-tdb2.ttl)

TDB with text index:

* [config-text-tdb2.ttl](config-text-tdb2.ttl)

Selecting one graph from a TDB dataset

tdb2-select-graphs-alt.ttl
tdb2-select-graphs.ttl

_SHACL service_

* [config-shacl.ttl](config-shacl.ttl)

_Query timeout_

* [config-timeout-dataset.ttl](config-timeout-dataset.ttl)
* [config-timeout-endpoint.ttl](config-timeout-endpoint.ttl)
* [config-timeout-server.ttl](config-timeout-server.ttl)

_Inference examples_

* [config-inference-1.ttl](config-inference-1.ttl)
* [config-inference-2.ttl](config-inference-2.ttl)

_Eclipse Jetty HTTPs setup._

A Jetty XML configuration file for running Jetty with HTTPS.
This file will require configuration and also installing a certificate

* [fuseki-jetty-https.xml](fuseki-jetty-https.xml)
