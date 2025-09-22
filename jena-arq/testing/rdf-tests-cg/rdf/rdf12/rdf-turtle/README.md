This README is for the W3C RDF & SPARQL Working Group's Turtle test suite.
This test suite contains three kinds of tests:

*  Positive evaluation (`rdft:TestTurtleEval`) — a pair of an input Turtle file and referenced N-Quads file..
*  Positive evaluation (`rdft:TestTurtleNegativeEval`) — a pair of an input Turtle file and referenced N-Quads file..
*  Positive syntax (`rdft:TestTurtlePositiveSyntax`) — an input Turtle file with no syntax errors.
*  Negative syntax (`rdft:TestTurtleNegatitveSyntax`) — an input Turtle file with at least one syntax error.

The `manifest.ttl` files in this directory lists tests in the RDF-star WG's Turtle test suite.
All tests have a name (`mf:name`) and an input (`mf:action`).

• An implementation passes an Evaluation test if it parses the input
  into a graph, parses the expected result into another graph, and
  those two graphs are isomorphic (see
  <http://www.w3.org/TR/rdf11-concepts/#graph-isomorphism>).
• An implementation passes a Negative Evaluation test if it parses the input
  into a graph, parses the expected result into another graph, and
  those two graphs are _not_ isomorphic (see
  <http://www.w3.org/TR/rdf11-concepts/#graph-isomorphism>).
* An implementation passes a positive syntax test if it parses the input.
* An implementation passes a negative syntax test if it fails to parse the input.

The home of the test suite is <https://w3c.github.io/rdf-tests/rdf/rdf12/rdf-turtle/>.
