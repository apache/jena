This README is for the W3C RDF 1.1 Working Group's RDF/XML test suite.
This test suite contains two kinds of tests:

  Evaluation (rdft:TestXMLEval) - a pair of an input RDF/XML file
  and reference ntriples file.

  Negative syntax (rdft:TestXMLNegativeSyntax) - an input RDF/XML
  file with at least one syntax error.

The manifest.ttl file in this directory lists all of the tests in the
RDF WG's RDF/XML test suite. Each test is one of the above tests. All
tests have a name (mf:name) and an input (mf:action). The Evaluation
tests have an expected result (mf:result).

• An implementation passes an Evaluation test if it parses the input
  into a graph, parses the expected result into another graph, and
  those two graphs are isomorphic (see
  <http://www.w3.org/TR/rdf11-concepts/#graph-isomorphism>).

• An implementation passes a positive syntax test if it parses the
  input.

• An implementation passes a negative syntax test if it fails to parse
  the input.

The home of the test suite is <https://w3c.github.io/rdf-tests/rdf/rdf11/rdf-xml/>.
Per RFC 3986 section 5.1.3, the base IRI for parsing each file is the
retrieval IRI for that file. For example, the tests rdf-containers-syntax-vs-schema-test004 and
xmlbase-test014 require relative IRI resolution against a base of
<https://w3c.github.io/rdf-tests/rdf/rdf11/rdf-xml/rdf-containers-syntax-vs-schema/test004.rdf> and
<https://w3c.github.io/rdf-tests/rdf/rdf11/rdf-xml/xmlbase/test014.rdf> respectively.

See http://www.w3.org/2011/rdf-wg/wiki/RDFXML_Test_Suite for more details.

Gregg Kellogg <gregg@greggkellogg.net> - 23 December 2013.
