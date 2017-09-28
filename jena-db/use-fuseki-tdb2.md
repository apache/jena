There is a combined Fuseki server jar:

In addition, you will need a copy of Apache Jena Fuseki to get the web pages.

```
## Set the environment variable FUSEKI_HOME to the location of a
## Fuseki2 distribution to pick up the admin UI web pages.
export FUSEKI_HOME=???? 
export FUSEKI_BASE="$FUSEKI_HOME"
# Alternatively, copy or link "webapp" in this directory
# to the webapp of a distribution.

# Run:
java -jar target/fuseki-tdb2-server-X.Y.Z.jar --conf config.ttl 
```

where a sample `config.ttl` for a service `/data` storing the database
in `DB2` is:

```
@prefix :        <#> .
@prefix fuseki:  <http://jena.apache.org/fuseki#> .
@prefix rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#> .
@prefix tdb2:    <http://jena.apache.org/2016/tdb#> .
@prefix ja:      <http://jena.hpl.hp.com/2005/11/Assembler#> .

[] rdf:type fuseki:Server ;
   fuseki:services (
     <#service_tdb2>
   ) .

<#service_tdb2> rdf:type fuseki:Service ;
    rdfs:label                      "TDB2 Service (RW)" ;
    fuseki:name                     "data" ;
    fuseki:serviceQuery             "query" ;
    fuseki:serviceQuery             "sparql" ;
    fuseki:serviceUpdate            "update" ;
    fuseki:serviceUpload            "upload" ;
    fuseki:serviceReadWriteGraphStore      "data" ;
    # A separate read-only graph store endpoint:
    fuseki:serviceReadGraphStore       "get" ;
    fuseki:dataset           <#tdb_dataset_readwrite> ;
    .

<#tdb_dataset_readwrite> rdf:type      tdb2:DatasetTDB2 ;
    tdb2:location "DB2" ;
    ## This works: tdb2:unionDefaultGraph true ;
     .
```

The key difference is the declared `rdf:type` of the dataset.

Note that the Fuseki UI does not provide a way to create TDB2 databases;
a configuration file must be used. Once setup, upload, query and graph
editting will be routed to the TDB2 database.
