This README is for the W3C RDF & SPARQL Working Group's N-Quads test suite.
This test suite contains three kinds of tests:

*  Positive syntax (`rdft:TestNQuadsPositiveSyntax`) — an input N-Quads file with no syntax errors.
*  Negative syntax (`rdft:TestNQuadsNegativeSyntax`) — an input N-Quads file with at least one syntax error.
*  Positive canonicalization (`rdft:TestNQuadsPositiveC14N`) — an input N-Quads file with no syntax errors.

The `manifest.ttl` files in this directory lists tests in the RDF-star WG's N-Quads test suite.
All tests have a name (`mf:name`) and an input (`mf:action`).

* An implementation passes a positive syntax test if it parses the
  input.
* An implementation passes a negative syntax test if it fails to parse
  the input.
* An implementation passes a positive canonicalization test if it parses the
  input, and generates the expected result (`mf:result`) compared as text when generating the canonical form of triples.

The home of the test suite is <https://w3c.github.io/rdf-tests/rdf/rdf12/rdf-n-quads/>.
