Testing
=======

Test sets can be run with the arq.qtest command line tool
(a wrapper round JUnit).

In the ARQ system test suite:

ARQ/              Tests of the ARQ system
DAWG/             DAWG formal test suite
sparql11-query/   SPARQL 1.1 WG query tests
sparql11-update/  SPARQL 1.1 WG update tests

"DAWG" is "Data Access Working Group"

The SPARQL 1.1 WG query tests are modified to run in ARQ in default mode with
extensions.  

* provides "+" for string concatenation
* grammar is compatible with Java character handling for Unicode
  surrogate pairs.  Broken surrogate pairs are illegal.
* supports CONSTRUCT for datasets (GRAPH in template)
* supports expression without AS in SELECT clause


