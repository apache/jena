This README is for the W3C RDF-star Working Group's XML test suite.
This test suite contains three kinds of tests:

*  Positive evaluation (`rdft:TestXMLEval`) — a pair of an input XML file and referenced N-Triples file..
*  Negative syntax (`rdft:TestXMLNegativeSyntax`) — an input XML file with at least one syntax error.

The `manifest.ttl` files in this directory lists tests in the RDF-star WG's XML test suite.
All tests have a name (`mf:name`) and an input (`mf:action`).

• An implementation passes an Evaluation test if it parses the input
  into a graph, parses the expected result into another graph, and
  those two graphs are isomorphic (see
  <http://www.w3.org/TR/rdf11-concepts/#graph-isomorphism>).
* An implementation passes a negative syntax test if it fails to parse the input.

The home of the test suite is <https://w3c.github.io/rdf-tests/rdf/rdf12/rdf-xml/>.
