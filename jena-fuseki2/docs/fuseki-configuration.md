# Configuring Fuseki

Configuration consists of defining the data services (data and actions available on the data) together with configuring the server.  Explicitly configuring the server is often unnecessary.

The data services configuration can come from:

1. The directory `FUSEKI_BASE/configuration/` with one data service assembler per file (includes endpoint details and the dataset description.)
2. The system database. This includes uploaded assembler files.  It also keeps the state of each data service (whether it's active or offline).
3. The service configuration file.  For compatibility, the service configuration file can also have data services. [See below](#relationship-to-fuseki-1-configuration).
4. The command line, if not running as a web application from a .war file.

`FUSEKI_BASE` is the location of the [Fuseki run area](#fuseki-layout.html).

## Data Service assembler

See [Fuseki Data Services](fuseki-data-services.html) for the architecture of data services.

See [Fuseki Security](fuseki-security.html) for more information on security.

## Fuseki Configuration File

A Fuseki server can be set up using a configuration file. The
command-line arguments for publishing a single dataset are a short
cut that, internally, builds a default configuration based on the
dataset name given.

The configuration is an RDF graph. One graph consists of one server
description, with a number of services, and each service offers a
number of endpoints over a dataset.

The example below is all one file (RDF graph in Turtle syntax)
split to allow for commentary.

### Prefix declarations

Some useful prefix declarations:

    @prefix fuseki:  <http://jena.apache.org/fuseki#> .
    @prefix rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
    @prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#> .
    @prefix tdb:     <http://jena.hpl.hp.com/2008/tdb#> .
    @prefix ja:      <http://jena.hpl.hp.com/2005/11/Assembler#> .
    @prefix :        <#> .

### Assembler Initialization

All datasets are described by 
[assembler descriptions](../assembler/index.html).
Assemblers provide an extensible way of describing many kinds of
objects. 

### Defining the service name and endpoints available 

Each data service assembler defines:

* The base name
* The operations and endpoint names
* The dataset for the RDF data.

This example offers SPARQL Query, SPARQL Update and SPARQL Graph
Store protocol, as well as file upload.

The base name is `/ds`.

    ## Updateable in-memory dataset.

    <#service1> rdf:type fuseki:Service ;
        fuseki:name                       "ds" ;       # http://host:port/ds
        fuseki:serviceQuery               "sparql" ;   # SPARQL query service
        fuseki:serviceQuery               "query" ;    # SPARQL query service (alt name)
        fuseki:serviceUpdate              "update" ;   # SPARQL update service
        fuseki:serviceUpload              "upload" ;   # Non-SPARQL upload service
        fuseki:serviceReadWriteGraphStore "data" ;     # SPARQL Graph store protocol (read and write)
        # A separate read-only graph store endpoint:
        fuseki:serviceReadGraphStore      "get" ;      # SPARQL Graph store protocol (read only)
        fuseki:dataset                   <#dataset> ;
        .

`<#dataset>` refers to a dataset description in the same file.

### Read-only service

This example offers only read-only endpoints (SPARQL Query and HTTP GET
SPARQl Graph Store protocol).

This service offers read-only access to a dataset with a single
graph of data.

    <#service2> rdf:type fuseki:Service ;
        fuseki:name                     "/ds-ro" ;   # http://host:port/da-ro
        fuseki:serviceQuery             "query" ;    # SPARQL query service
        fuseki:serviceReadGraphStore    "data" ;     # SPARQL Graph store protocol (read only)
        fuseki:dataset           <#dataset> ;
        .

### Dataset

#### In-memory

An in-memory dataset, with data in the default graph taken from a local file.

    <#books>    rdf:type ja:RDFDataset ;
        rdfs:label "Books" ;
        ja:defaultGraph
          [ rdfs:label "books.ttl" ;
            a ja:MemoryModel ;
            ja:content [ja:externalContent <file:Data/books.ttl> ] ;
          ] ;
        .

#### TDB

    <#dataset> rdf:type      tdb:DatasetTDB ;
        tdb:location "DB" ;
        # Query timeout on this dataset (1s, 1000 milliseconds)
        ja:context [ ja:cxtName "arq:queryTimeout" ;  ja:cxtValue "1000" ] ;
        # Make the default graph be the union of all named graphs.
        ## tdb:unionDefaultGraph true ;
         .

#### Inference

> @@

## Server Configuration

If you need to load additional classes, or set global parameters, then these go in
`FUSEKI_BASE/config.ttl`.

Additional classes can not be loaded if running as a `.war` file.  You will
need to create a custom `.war` file consisting of the contents of the Fuseki
web application and the additional classes

### Server Section

    [] rdf:type fuseki:Server ;
       # Server-wide context parameters can be given here.
       # For example, to set query timeouts: on a server-wide basis:
       # Format 1: "1000" -- 1 second timeout
       # Format 2: "10000,60000" -- 10s timeout to first result, then 60s timeout to for rest of query.
       # See java doc for ARQ.queryTimeout
       # ja:context [ ja:cxtName "arq:queryTimeout" ;  ja:cxtValue "10000" ] ;

       # Load custom code (rarely needed)
       # ja:loadClass "your.code.Class" ;
       .

## Compatibility with Fuseki 1 configuration

Configurations from Fuseki 1, where all dataset and server setup is in a
single configuration file, will still work.  It is less flexible
(you can't restart these services after stopping them in a running server)
and user should plan to migrate to the new layout.

To convert a Fuseki 1 configuration setup to Fuseki 2 style, move each data service assembler and put in it's own file under `FUSEKI_BASE/configuration/`
