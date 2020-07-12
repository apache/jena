/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.shacl.compact.reader;

import java.util.*;

import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.tokens.TokenizerText;
import org.apache.jena.shacl.ShaclException;
import org.apache.jena.shacl.engine.ShaclPaths;
import org.apache.jena.shacl.lib.ShLib;
import org.apache.jena.shacl.vocabulary.SHACL;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.lang.ParserBase;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;

/**
 * The engine for translating SHACL Compact Syntax into a SHACL graph of triples.
 */
public class ShaclCompactParser extends ParserBase {

    private StreamRDF outputStream;

    protected ShaclCompactParser() {
        setPrologue(new Prologue());
    }

    public void start(StreamRDF output) {
        if ( this.outputStream != null )
            throw new ShaclException("Output graph already set");
        this.outputStream = output;
    }

    // Once the whole document has been completed, produce a triple ?baseURI rdf:type
    // owl:Ontology using the final value of baseURI. Report an error if baseURI has
    // no value but imports is not empty. For each iri in imports, produce a triple
    // ?baseURI owl:imports ?iri.
    public void finish() {
        // A test (empty.shaclc) does not pass without this.
        // It is the only test without a BASE.
        if ( true /*baseSeen != null*/ ) {
            String base = getPrologue().getBaseURI();
            if ( base == null )
                throw new ShaclException("No BASE");

            Node s = iri(base);
            triple(outputStream, s, nRDFtype, OWL.Ontology.asNode());
            imports.forEach(iri -> triple(outputStream, s, OWL.imports.asNode(), iri(iri)));
        } else {
            // No BASE, but with imports. Record using a blank node.
            Node s = NodeFactory.createBlankNode();
            imports.forEach(iri -> triple(outputStream, s, OWL.imports.asNode(), iri(iri)));
        }

        if ( !currentNodeShape.isEmpty() )
            throw new InternalErrorException("Internal error: Node shape stack is not empty at end of parsing");
        if ( !currentPropertyShape.isEmpty() )
            throw new InternalErrorException("Internal error: Property shape stack is not empty at end of parsing");
    }

    // Stacks for node shape and property shape.

    // The node shape and property shape stacks.
    private Deque<Node>         currentNodeShape         = new ArrayDeque<>();
    private Deque<Node>         currentPropertyShape     = new ArrayDeque<>();

    // Collectors (node and property), gather up the elements of an "or"
    private Deque<List<Node>>   nodeShapeCollectors      = new ArrayDeque<>();
    private Deque<List<Node>>   propertyShapeCollectors  = new ArrayDeque<>();

    // Accumulators for triples of a constraint
    private Deque<List<Triple>> currentConstraintTriples = new ArrayDeque<>();

    // Record what is seen.
    private String baseSeen                     = null;
    private Map<String, String> prefixesSeen    = new HashMap<>();
    private List<String> imports                = new ArrayList<>();

    protected void startNodeShape() {}

    protected void beginNodeShape(Node node) {
        currentNodeShape.push(node);
    }

    protected void finishNodeShape() {
        currentNodeShape.pop();
    }

    private Node currentNodeShape() {
        if ( currentNodeShape.isEmpty() )
            throw new InternalErrorException("Internal error: no current node shape");
        return currentNodeShape.peek();
    }

    protected void startShapeClass() {
        startNodeShape();
    }

    protected void beginShapeClass(Node node) {
        beginNodeShape(node);
    }

    protected void finishShapeClass() {
        finishNodeShape();
    }

    protected void startPropertyShape() {}

    protected void beginPropertyShape(Node node) {
        currentPropertyShape.push(node);
    }

    protected void finishPropertyShape() {
        currentPropertyShape.pop();
    }

    private Node currentPropertyShape() {
        if ( currentPropertyShape.isEmpty() )
            throw new InternalErrorException("Internal error: no current property shape");
        return currentPropertyShape.peek();
    }

    // nodeShapeBody: Handle each constraint using the context shape ?shape.
    protected void startNodeShapeBody() {}

    protected void finishNodeShapeBody() {}

    protected void beginTripleAcc(List<Triple> acc) {
        currentConstraintTriples.push(acc);
    }

    protected void finishTripleAcc() {
        currentConstraintTriples.pop();
    }

    private List<Triple> currentTripleAcc() {
        if ( currentConstraintTriples.isEmpty() )
            throw new InternalErrorException("Internal error: no current tripel accumulator");
        return currentConstraintTriples.peek();
    }

    protected void startConstraint() {
        List<Triple> acc = new ArrayList<>();
        beginTripleAcc(acc);
    }

    protected void finishConstraint() {
        List<Triple> acc = currentTripleAcc();
        acc.forEach(outputStream::triple);
        finishTripleAcc();
    }

    // ---- Start grammar.

    protected void rBase(String baseURI) {
        getPrologue().setBaseURI(baseURI);
        outputStream.base(baseURI);
        baseSeen = baseURI;
    }

    protected void rPrefix(String prefix, String iriStr) {
        getPrologue().getPrefixMapping().setNsPrefix(prefix, iriStr);
        outputStream.prefix(prefix, iriStr);
        prefixesSeen.put(prefix, iriStr);
    }

    protected void rImports(String iri) {
        imports.add(iri);
    }

    // nodeShape: Produce a triple ?shape rdf:type sh:NodeShape where ?shape is
    // derived from the iri using iri. Use ?shape as context shape for the
    // targetClass and nodeShapeBody.
    protected void rNodeShape(String iri) {
        Node shape = iri(iri);
        triple(outputStream, shape, nRDFtype, SHACL.NodeShape);
        beginNodeShape(shape);
    }

    // shapeClass: Produce the triples ?shape rdf:type sh:NodeShape and ?shape
    // rdf:type rdfs:Class where ?shape is derived from the iri using iri. Use
    // ?shape as context shape for the nodeShapeBody.
    protected void rShapeClass(String iri) {
        Node s = iri(iri);
        beginShapeClass(s);
        triple(outputStream, s, nRDFtype, SHACL.NodeShape);
        triple(outputStream, s, nRDFtype, RDFS.Nodes.Class);
    }

    // targetClass: For each iri, produce a triple ?shape sh:targetClass ?iri where
    // ?iri is derived from iri.
    protected void rTargetClass(String iri) {
        Node s = currentNodeShape();
        Node n = iri(iri);
        triple(outputStream, s, SHACL.targetClass, n);
    }

    // nodeOr: If there is more than one nodeNot, then produce an RDF list ?or where
    // for each nodeNot, there is a new blank node, and that blank node is used as
    // context shape for the nodeNot. Then produce a triple ?shape sh:or ?or. If
    // there is only one nodeNot, handle the nodeNot using the context shape ?shape.

    protected void startNodeOr() {
        nodeShapeCollectors.add(new ArrayList<>());
    }

    protected void rNodeOr_pre() {
        Node blank = newBlankNode("nodeOr-elt");
        beginNodeShape(blank);
    }

    protected void rNodeOr_post() {
        Node n = currentNodeShape();
        nodeShapeCollectors.peek().add(n);
        finishNodeShape();
    }

    protected void finishNodeOr() {
        List<Node> elts = nodeShapeCollectors.pop();
        if ( elts.isEmpty() )
            throw new InternalErrorException("No elements in nodeOr");
        if ( elts.size() == 1 ) {
            // Pull up one level.
            rewrite(currentTripleAcc(), elts.get(0), currentNodeShape());
            return;
        }
        Node list = listToTriples(elts);
        triple(currentTripleAcc(), currentNodeShape(), SHACL.or, list);
    }

    // nodeNot: If there is a negation, produce a new blank node ?not and produce a
    // triple ?shape sh:not ?not. Then handle the nodeValue using ?not as context
    // shape. If there is no negation, handle the nodeValue using the context shape
    // ?shape.

    protected void startNodeNot() {}

    protected void beginNodeNot(boolean negation) {
        if ( !negation )
            return;
        Node blank = newBlankNode("nodeNot");
        beginNodeShape(blank);
    }

    protected void finishNodeNot(boolean negation) {
        if ( !negation )
            return;
        Node n = currentNodeShape();
        finishNodeShape();
        Node nodeShape = currentNodeShape();
        triple(currentTripleAcc(), nodeShape, SHACL.not, n);
    }

    // nodeValue: Produce a triple ?shape ?predicate ?object where ?predicate is the
    // IRI produced by concatenating the sh namespace with string value of nodeParam
    // (for example "minLength" becomes sh:minLength), and ?object is derived from
    // the iriOrLiteralOrArray.

    protected void rNodeValue(String s, Node n) {
        Node p = NodeFactory.createURI(SHACL.getURI() + s);
        triple(currentTripleAcc(), currentNodeShape(), p, n);
    }

    protected void rNodeValue(String s, List<Node> x) {
        Node n = nodeArrayToTriples(x);
        Node p = NodeFactory.createURI(SHACL.getURI() + s);
        triple(currentTripleAcc(), currentNodeShape(), p, n);
    }

    // propertyShape: Using a new blank node ?property, produce a triple ?shape
    // sh:property ?property. Produce a triple ?property sh:path ?path where
    // ?path is the result of path. Use ?property as context shape for
    // propertyCount and propertyOr.

    protected void rPropertyShape(Path parsedPath) {
        Node path = pathToNode(parsedPath);
        Node b = newBlankNode("propertyShape");
        beginPropertyShape(b);
        triple(currentTripleAcc(), currentNodeShape(), SHACL.property, currentPropertyShape());
        if ( path == null )
            throw new ShaclException("Internal error: no path");
        triple(currentTripleAcc(), currentPropertyShape(), SHACL.path, path);
    }

    // propertyCount: If propertyMinCount is not "0", produce a triple ?property
    // sh:minCount ?minCount using the xsd:integer derived from propertyMinCount
    // as ?minCount. If propertyMaxCount is not "*", produce a triple ?property
    // sh:maxCount ?maxCount using the xsd:integer derived from propertyMaxCount
    // as ?maxCount.
    protected void rPropertyCount(String minStr, String maxStr) {
        int min = integer(minStr, 0);
        int max = integer(maxStr, -1);
        if ( min > 0 )
            triple(currentTripleAcc(), currentPropertyShape(), SHACL.minCount, NodeFactory.createLiteral(minStr, XSDDatatype.XSDinteger));
        if ( max > 0 )
            triple(currentTripleAcc(), currentPropertyShape(), SHACL.maxCount, NodeFactory.createLiteral(maxStr, XSDDatatype.XSDinteger));
    }

    // propertyOr: If there is more than one propertyNot, then produce an RDF list
    // ?or where
    // for each propertyNot, there is a new blank node, and that blank node is
    // used as context shape for the propertyNot. Then produce a triple ?property
    // sh:or ?or. If there is only one propertyNot, handle the propertyNot using
    // the context shape ?property.

    // We do this by assuming there will be multiple elements, then if (common case)
    // there is one, modifying the graph using "rewrite" - typically one triple to
    // remove, one to add. The root issue is that we either buffer and decide later
    // or alter the graph.

    protected void startPropertyOr() {
        propertyShapeCollectors.add(new ArrayList<>());
    }

    protected void rPropertyOr_pre() {
        Node blank = newBlankNode("propertyOr-elt");
        beginPropertyShape(blank);
    }

    protected void rPropertyOr_post() {
        Node n = currentPropertyShape();
        propertyShapeCollectors.peek().add(n);
        finishPropertyShape();
    }

    protected void finishPropertyOr() {
        List<Node> elts = propertyShapeCollectors.pop();
        if ( elts.isEmpty() )
            throw new InternalErrorException("No elements in propertyOr");
        if ( elts.size() == 1 ) {
            // Pull up one level.
            rewrite(currentTripleAcc(), elts.get(0), currentPropertyShape());
            return;
        }
        Node list = listToTriples(elts);
        triple(currentTripleAcc(), currentPropertyShape(), SHACL.or, list);
    }

    // propertyNot: If there is a negation, produce a new blank node ?not and produce
    // a triple ?property sh:not ?not. Then handle the propertyAtom using ?not as
    // context shape. If there is no negation, handle the propertyAtom using the
    // context shape ?property.
    protected void startPropertyNot() {}

    protected void beginPropertyNot(boolean negation) {
        if ( !negation )
            return;
        Node blank = newBlankNode("propertyNot");
        beginPropertyShape(blank);
    }

    protected void finishPropertyNot(boolean negation) {
        if ( !negation )
            return;
        Node n = currentPropertyShape();
        finishPropertyShape();
        Node propertyShape = currentPropertyShape();
        triple(currentTripleAcc(), propertyShape, SHACL.not, n);
    }

    // propertyAtom: Use ?property as context shape for any of the child elements.
    // For a nested nodeShapeBody, produce a new blank node ?node and use that as the
    // context shape ?shape. Then produce a triple ?property sh:node ?node.
    protected void startNestedPropertyAtom() {
        // For a nested nodeShapeBody (in a propertyAtom), produce a new blank node
        // ?node and use that as the context shape ?shape.
        Node b = newBlankNode("nested-shape");
        startNodeShape();
        triple(currentTripleAcc(), currentPropertyShape(), SHACL.node, b);
        beginNodeShape(b);
    }

    protected void finishNestedPropertyAtom() {
        finishNodeShape();
    }

    // propertyType: Let ?iri be the IRI derived from the propertyType using iri. If
    // ?iri is one of the RDF datatypes supported by SPARQL 1.1 (such as xsd:string)
    // then produce a triple ?property sh:datatype ?iri, otherwise ?property sh:class
    // ?iri.

    protected void rPropertyType(String iri) {
        Node p = ShLib.isDatatype(iri)
            ? SHACL.datatype
            : SHACL.class_;
        triple(currentTripleAcc(), currentPropertyShape(), p, iri(iri));
    }

    // nodeKind: Produce a triple ?property sh:nodeKind ?nodeKind where ?nodeKind is
    // the IRI produced by concatenating the sh namespace with the text value of
    // nodeKind (e.g., sh:Literal).
    protected void rNodeKind(String nodeKindName) {
        // Parser only produce legal values.
        Node nodeKind = iri(SHACL.getURI()+nodeKindName);
        triple(currentTripleAcc(), currentPropertyShape(), SHACL.nodeKind, nodeKind);
    }

    private Node pathToNode(Path parsedPath) {
        return ShaclPaths.pathToRDF(parsedPath, outputStream);
    }

    // shapeRef: Produce a triple ?property sh:node ?node where ?node is the IRI
    // derived from the substring of shapeRef after the '@' character using iri.
    protected void rShapeRef(String iriStr) {
        Node x = iri(iriStr);
        triple(currentTripleAcc(), currentPropertyShape(), SHACL.node, x);
    }

    // propertyValue: Produce a triple ?property ?predicate ?object where ?predicate
    // is the IRI produced by concatenating the sh namespace with the string value of
    // propertyParam, and ?object is derived from the iriOrLiteralOrArray.
    protected void rParamValue(String s, Node n) {
        Node p = NodeFactory.createURI(SHACL.getURI()+s);
        triple(currentTripleAcc(), currentPropertyShape(), p, n);
    }

    protected void rParamValue(String s, List<Node> x) {
        Node n = nodeArrayToTriples(x);
        Node p = NodeFactory.createURI(SHACL.getURI() + s);
        triple(currentTripleAcc(), currentPropertyShape(), p, n);
    }

    // iriOrLiteralOrArray: If there is an array, produce and return an RDF list
    // where each iriOrLiteral is a member. Otherwise, return iriOrLiteral for
    // iriOrLiteral.

    // With iriOrLiteral
    private Node nodeArrayToTriples(List<Node> x) {
        Node list = nRDFnil;

        ListIterator<Node> iter = x.listIterator(x.size());
        while (iter.hasPrevious()) {
            Node elt = iter.previous();
            // elt = iriOrLiteral(elt);
            Node z = newBlankNode("array");
            triple(currentTripleAcc(), z, nRDFfirst, elt);
            triple(currentTripleAcc(), z, nRDFrest, list);
            list = z;
        }
        return list;
    }

    // iriOrLiteral: If there is an iri, return the node derived from iri. Otherwise,
    // apply Turtle's parsing rules to turn the string literal into an RDF literal.
    // (from the test cases, this does not mean parse the node string again)

    private Node iriOrLiteral(Node x) {
        if ( x.isURI() )
            return x;
        String s = x.getLiteralLexicalForm();
        PrefixMap pmap = PrefixMapFactory.create(getPrologue().getPrefixMapping());
        Node n = TokenizerText.create().fromString(s).build().next().asNode(pmap);
        return n;
    }

    // ------------------------------

    private int integer(String str, int i) {
        if ( str == null || str.equals("*") )
            return i;
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException ex) {
            System.err.println("Number format exception");
            return i;
        }
    }

    // Java List<Node> to RDF list.
    private Node listToTriples(List<Node> x) {
        Node list = nRDFnil;
        ListIterator<Node> iter = x.listIterator(x.size());
        while (iter.hasPrevious()) {
            Node elt = iter.previous();
            Node z = newBlankNode("list");
            triple(currentTripleAcc(), z, nRDFfirst, elt);
            triple(currentTripleAcc(), z, nRDFrest, list);
            list = z;
        }
        return list;
    }

    private Node iri(String iriStr) {
        return NodeFactory.createURI(iriStr);
    }

    // private static int counter = 0;
    private Node newBlankNode(String label) {
        // Label is a debugging aid.
        // return iri("x:"+label+"-"+(++counter));
        return NodeFactory.createBlankNode();
    }

    private void rewrite(List<Triple> accumulator, Node node1, Node node2) {
        for ( int i = 0 ; i < accumulator.size() ; i++ ) {
            Triple t = accumulator.get(i);
            if ( t.getSubject().equals(node1) ) {
                Triple t2 = Triple.create(node2, t.getPredicate(), t.getObject());
                accumulator.set(i, t2);
            }
        }
    }

    private void triple(Collection<Triple> acc, Node s, Node p, Node o) {
        Triple triple = Triple.create(s, p, o);
        acc.add(triple);
    }

    private static void triple(StreamRDF out, Node s, Node p, Node o) {
        Triple triple = Triple.create(s, p, o);
        out.triple(triple);
    }
}
