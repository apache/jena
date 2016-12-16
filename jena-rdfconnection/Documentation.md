# RDF Connection : SPARQL operations API

`RDFConnection` provides a unified set of operations for working on RDF
with SPARQL operations. It provides <a
href="http://www.w3.org/TR/sparql11-query/">SPARQL Query</a>, <a
href="http://www.w3.org/TR/sparql11-update/">SPARQL Update</a> and the <a
href="http://www.w3.org/TR/sparql11-http-rdf-update/">SPARQL Graph
Store</a> operations.  The interface is uniform - the same interface
applies to local data and to remote data using HTTP and the SPARQL
protocols ( <a href="http://www.w3.org/TR/sparql11-protocol/">SPARQL
protocol</a> and <a
href="http://www.w3.org/TR/sparql11-http-rdf-update/">SPARQL Graph Store
Protocol</a>).

## Outline

`RDFConnection` provides a number of different styles for working with RDF
data in Java.  It provides support for try-resource and functional code
passing styles, as well the more basic sequence of methods calls.

`try-resources` to manage the connection, and two operations, one to load
some data, and one to make a query:

```
try ( RDFConnection conn = RDFConnectionFactory.connect(...) ) {
    conn.load("data.ttl") ;
    conn.querySelect("SELECT DISTINCT ?s { ?s ?p ?o }", (qs)->
       Resource subject = qs.getResource("s") ;
       System.out.println("Subject: "+subject) ;
    }) ;
}
```
This could have been written as (approximately -- the error handling is better
in the example above):

```
RDFConnection conn = RDFConnectionFactory.connect(...)
conn.load("data.ttl") ;
QueryExecution qExec = conn.query("SELECT DISTINCT ?s { ?s ?p ?o }") ;
ResultSet rs = qExec.execSelect() ;
while(rs.hasNext()) {
    QuerySolution qs = rs.next() ;
    Resource subject = qs.getResource("s") ;
    System.out.println("Subject: "+subject) ;
}
qExec.close() ;
conn.close() ;
```

Jena also provides a separate
[SPARQL over JDBC driver](http://jena.staging.apache.org/documentation/jdbc/index.html)
library.

## Transactions

Transactions are the preferred way to work with RDF data.
Operations on an `RDFConnection` outside of an application-controlled
transaction will cause the system to add one for the duration of the
operation. This "autocommit" feature may lead to inefficient operations due
to excessive overhead.

The `Txn` class provides a Java8-style transaction API.  Transactions are
code passed in the `Txn` library that handles the transaction lifecycle.

```
try ( RDFConnection conn = RDFConnectionFactory.connect(...) ) {
    Txn.execWrite(conn, ()-> {
        conn.load("data1.ttl") ;
        conn.load("data2.ttl") ;
        conn.querySelect("SELECT DISTINCT ?s { ?s ?p ?o }", (qs)->
           Resource subject = qs.getResource("s") ;
           System.out.println("Subject: "+subject) ;
        }) ;
    }) ;
}
```

The traditional style of explicit `begin`, `commit`, `abort` is also available.

```
try ( RDFConnection conn = RDFConnectionFactory.connect(...) ) {
    conn.begin(ReadWrite.WRITE) ;
    try {
        conn.load("data1.ttl") ;
        conn.load("data2.ttl") ;
        conn.querySelect("SELECT DISTINCT ?s { ?s ?p ?o }", (qs)->
           Resource subject = qs.getResource("s") ;
           System.out.println("Subject: "+subject) ;
        }) ;
        conn.commit() ;
    } finally { conn.end() ; }
}
```

The use of `try-finally` ensures that transactions are properly finished.
The `conn.end()` provides an abort in case an exception occurs in the
transaction and a commit has not been issued.  The use of `try-finally`
ensures that transactions are properly finished.

`Txn` is wrapping these steps up and calling the application supplied code
for the transaction body.

### Remote Transactions

SPARQL does not define a remote transaction standard protocol. Each remote
operation shuld be atomic (all happens or nothing happens) - this is the
responsibility of the remote server.

An `RDFConnection` will at least provide the client-side locking features.
This means that overlapping operations that change data are naturally
handled by the transaction pattern within a single JVM.

## Graph Store Protocol

The <a href="http://www.w3.org/TR/sparql11-http-rdf-update/">SPARQL Graph
Store Protocol</a> is a set of operations to work on whole graphs in a
dataset.  It provides a standardised way to manage the data in a dataset.

The operations are to fetch a graph, set the RDF data in a graph,
add more RDF data into a graph, and delete a graph from a dataset.

For example: load two files:
```
  try ( RDFConnection conn = RDFConnectionFactory.connect(...) ) {
    conn.load("data1.ttl") ;
    conn.load("data2.nt") ;
  } 
```
The file extension is used to determine the syntax.

There is also a set of scripts to help do these operations from the command
line with <a href="http://jena.apache.org/documentation/fuseki2/soh.html"
>SOH</a>. It is possible to write curl scripts as well.  The SPARQL Graph
Store Protocol provides a standardised way to manage the data in a dataset.

In addition, `RDFConnection` provides an extension to give the same style
of operation to work on a whole dataset (deleting the dataset is not
provided).

```
    conn.loadDataset("data-complete.trig") ;
```

## Query Usage

`RDFConnection` provides methods for each of the SPARQL query forms (`SELECT`,
`CONSTRUCT`, `DESCRIBE`, `ASK`) as well as a way to get the lower level
`QueryExecution` for specialized configuration.

When creating an `QueryExecution` explicitly, care shoud be taken to close
it. If the application wishes to capture the result set from a SELECT query and
retain it across the lifetime of the transaction or `QueryExecution`, then
the application should create a copy which is not attached to any external system
with `ResultSetFactory.copyResults`.

```
  try ( RDFConnection conn = RDFConnectionFactory.connect("foo") ) {
      ResultSet safeCopy =
          Txn.execReadReturn(conn, ()-> {
              // Process results by row:
              conn.querySelect("SELECT DISTINCT ?s { ?s ?p ?o }", (qs)->{
                  Resource subject = qs.getResource("s") ;
                  System.out.println("Subject: "+subject) ;
              }) ;
              ResultSet rs = conn.query("SELECT * { ?s ?p ?o }").execSelect() ;
              return ResultSetFactory.copyResults(rs) ;
          }) ;
  }
```

## Update Usage

SPARQL Update opertions can be performed and mixed with other operations.

```
  try ( RDFConnection conn = RDFConnectionFactory.connect(...) ) {
      Txn.execWrite(conn, ()-> {
         conn.update("DELETE DATA { ... }" ) ;
         conn.load("data.ttl") ;
         }) ;
```

## Dataset operations

In addition to the SPARQL Graph Store Protocol, operations on whole
datasets are provided for fetching (HTTP GET), adding data (HTTP POST) and
setting the data (HTTP PUT) on a dataset URL.  This assumes the remote
server supported these REST-style operations.  Apache Jena Fuseki does
provide these.

## Subinterfaces

To help structure code, the `RDFConnection` consists of a number of
different interfaces.  An `RDFConnection` can be passed to application code
as one of these interfaces so that only certain subsets of the full
operations are visible to the called code.

* query via `SparqlQueryConnection`
* update via `SparqlUpdateConnection`
* graph store protocol `RDFDatasetAccessConnection` (read operations),
   and `RDFDatasetConnection` (read and write operations).

## Examples

https://github.com/apache/jena/jena-rdfconnection/tree/master/src/main/java/rdfconnection/examples
