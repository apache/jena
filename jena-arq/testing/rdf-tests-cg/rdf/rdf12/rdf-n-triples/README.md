This README is for the W3C RDF & SPARQL Working Group's N-Triples test suite.
This test suite contains three kinds of tests:

*  Positive syntax (`rdft:TestNTriplesPositiveSyntax`) — an input N-Triples file with no syntax errors.
*  Negative syntax (`rdft:TestNTriplesNegativeSyntax`) — an input N-Triples file with at least one syntax error.
*  Positive canonicalization (`rdft:TestNTriplesPositiveC14N`) — an input N-Triples file with no syntax errors.

The `manifest.ttl` files in this directory lists tests in the RDF-star WG's N-Triples test suite.
All tests have a name (`mf:name`) and an input (`mf:action`).

* An implementation passes a positive syntax test if it parses the
  input, and signals no errors or warnings.
* An implementation passes a negative syntax test if it fails to parse
  the input, or signals an error or warning.
* An implementation passes a positive canonicalization test if it parses the
  input, and generates the expected result (`mf:result`) compared as text when generating the canonical form of triples.

The home of the test suite is <https://w3c.github.io/rdf-tests/rdf/rdf12/rdf-n-triples/>.
