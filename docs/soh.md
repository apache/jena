# SOH - SPARQL over HTTP

**SOH** (SPARQL Over HTTP) is a set of command-line scripts for
working with SPARQL 1.1. SOH is server-independent and will work
with any compliant SPARQL 1.1 system offering HTTP access.

SOH is written in ruby.

Commands:

-   `s-http` – SPARQL 1.1 HTTP Protocol
-   `s-get`, `s-put`, `s-delete`, `s-post`, `s-head` – abbreviation for
    `s-http get ...` etc.
-   `s-query` – SPARQL 1.1 Query, both GET and POST of queries.
-   `s-update` – SPARQL 1.1 Update
-   `s-update-form` – SPARQL 1.1 Update using the HTML form and a
    parameter of `request=`.

Each command supports the `-v` flag to print out details of the
HTTP interaction.

## Contents

-   [SOH SPARQL Query](#soh-sparql-query)
-   [SOH SPARQL HTTP](#soh-sparql-http)
-   [SOH SPARQL Update](#soh-sparql-update)
-   [Service endpoints](#service-endpoints)


## SOH SPARQL Query

    s-query --service=endpointURL 'query string'

    s-query --service=endpointURL --query=queryFile.rq

## SOH SPARQL HTTP

The [SPARQL Graph Store Protocol](http://www.w3.org/TR/sparql11-http-rdf-update/)
is a way to read, create and update whole graphs in an RDF Dataset.
It is useful for data management and building into
automated processes because it is easy to script with
tools like `curl` or `wget`.

SOH provides commands that simplify the use HTTP further by setting HTTP
headers based on the operation performed.

The syntax of the commands is:

    s-http VERB datasetURI graphName [file]

where graph name is a URI or the word *default* for the default
graph.

`s-get`, `s-put`, `s-delete`, `s-post` are abbreviations for `s-http get`,
`s-http put`, `s-http delete` and `s-http post` respectively.

`file` is needed for PUT and POST. The file name extension determines
the HTTP content type.

     s-put http://localhost:3030/dataset default data.ttl

     s-get http://localhost:3030/dataset default

     s-put http://localhost:3030/dataset http://example/graph data.ttl

     s-get http://localhost:3030/dataset http://example/graph

## SOH SPARQL Update

    s-update --service=endpointURL 'update string'

    s-update --service=endpointURL --update=updateFile.ru

## Service endpoints

SOH is a general purpose set of scripts that work with any SPARQL
1.1. server. Different servers offer different naming conventions
for HTTP REST, query and update. This section provides summary
information about using SOH with some servers. See the
documentation for each server for authoritative information.

If you have details for other servers, [get involved](/getting_involved/index.html)

### Fuseki

If a [Fuseki](index.html) server is run with the
command:

    fuseki-server --update --mem /MyDataset

then the service endpoints are:

-   HTTP: `http://localhost:3030/MyDataset/data`
-   Query: `http://localhost:3030/MyDataset/query` or `http://localhost:3030/MyDataset/sparql`
-   Update: `http://localhost:3030/MyDataset/update`
