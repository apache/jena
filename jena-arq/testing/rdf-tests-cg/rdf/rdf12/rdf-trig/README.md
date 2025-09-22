This README is for the W3C RDF & SPARQL Working Group's TrigG test suite.
This test suite contains three kinds of tests:

*  Positive evaluation (`rdft:TestTrigEval`) — a pair of an input TriG file and referenced N-Quads file..
*  Positive evaluation (`rdft:TestTrigNegativeEval`) — a pair of an input TriG file and referenced N-Quads file..
*  Positive syntax (`rdft:TestTriGPositiveSyntax`) — an input TriG file with no syntax errors.
*  Negative syntax (`rdft:TestTriGNegativeSyntax`) — an input TriG file with at least one syntax error.

The `manifest.ttl` files in this directory lists tests in the RDF-star WG's TriG test suite.
All tests have a name (`mf:name`) and an input (`mf:action`).

• An implementation passes an Evaluation test if it parses the input
  into a dataset, parses the expected result into another dataset, and
  those two dataset are isomorphic (see
  <https://www.w3.org/TR/rdf12-concepts/#dfn-dataset-isomorphism>).
• An implementation passes an Negative Evaluation test if it parses the input
  into a dataset, parses the expected result into another dataset, and
  those two dataset are _not_ isomorphic (see
  <https://www.w3.org/TR/rdf12-concepts/#dfn-dataset-isomorphism>).
* An implementation passes a positive syntax test if it parses the input.
* An implementation passes a negative syntax test if it fails to parse the input.

The home of the test suite is <https://w3c.github.io/rdf-tests/rdf/rdf12/rdf-trig/>.
