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

package org.apache.jena.sparql.util;

import java.util.*;

import org.apache.jena.atlas.lib.ListUtils;
import org.apache.jena.atlas.lib.SetUtils;
import org.apache.jena.datatypes.RDFDatatype ;
import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.irix.IRIx;
import org.apache.jena.rdf.model.impl.Util;
import org.apache.jena.sparql.expr.ExprEvalException ;
import org.apache.jena.sparql.expr.NodeValue ;
import org.apache.jena.sparql.expr.nodevalue.NodeFunctions ;
import org.apache.jena.util.iterator.ExtendedIterator ;
import org.apache.jena.util.iterator.WrappedIterator ;

/**
 *  Node utilities.
 *  See {@link NodeCmp} for node comparison operations.
 */
public class NodeUtils
{
    /**
     * IRI to Node
     * @deprecated Do not use org.apache.jena.iri.IRI. Use {@link IRIx}.
     */
    @Deprecated
    public static Node asNode(org.apache.jena.iri.IRI iri) {
        return NodeFactory.createURI(iri.toString()) ;
    }

    /** IRI string to Node */
    public static Node asNode(String iri) {
        return NodeFactory.createURI(iri) ;
    }

    /** Return true if the node is a literal and has a language tag */
    public static boolean hasLang(Node node) {
        if ( !node.isLiteral() )
            return false ;
        String x = node.getLiteralLanguage() ;
        if ( x == null )
            return false ;
        if ( x.equals("") )
            return false ;
        return true ;
    }

    /** Get lexical form of anything that looks like a string literal.
     * Returns the string value of plain literal (simple literal
     * or lang string) or XSD string.
     */
    public static String stringLiteral(Node literal) {
        if ( !literal.isLiteral() )
            return null ;
        RDFDatatype dType = literal.getLiteralDatatype() ;
        String langTag = literal.getLiteralLanguage() ;

        // Language?
        if ( langTag != null && !langTag.equals("") )
            return literal.getLiteralLexicalForm() ;

        if ( dType == null || dType.equals(XSDDatatype.XSDstring) )
            return literal.getLiteralLexicalForm() ;

        return null ;
    }

    public static Node nullToAny(Node n) {
        return n == null ? Node.ANY : n;
    }

    /** Convert IRI Nodes to strings.  Skip other kinds of Node */
    public static Iterator<String> nodesToURIs(Iterator<Node> iter) {
        ExtendedIterator<Node> eIter = WrappedIterator.create(iter) ;
        Iterator<String> conv = eIter.filterKeep(Node::isURI).mapWith(Node::getURI);
        return conv ;
    }

    /** Convert a collection of strings to a set of {@link Node Nodes}. */
    public static Set<Node> convertToSetNodes(Collection<String> namedGraphs) {
        Set<Node> nodes = SetUtils.toSet(
            namedGraphs.stream().map(NodeFactory::createURI)
            );
        return nodes;
    }

    /** Convert a collection of strings to a set of {@link Node Nodes}. */
    public static Set<Node> convertToSetNodes(String... namedGraphs) {
        return convertToSetNodes(Arrays.asList(namedGraphs));
    }

    /** Convert strings to a List of {@link Node Nodes}. */
    public static List<Node> convertToListNodes(String... namedGraphs) {
        return convertToListNodes(Arrays.asList(namedGraphs));
    }

    /** Convert strings to a List of {@link Node Nodes}. */
    public static List<Node> convertToListNodes(List<String> namedGraphs) {
        List<Node> nodes = ListUtils.toList(
            namedGraphs.stream().map(NodeFactory::createURI)
            );
        return nodes;
    }

    /**
     * @deprecated Use {@link NodeCmp#compareRDFTerms(Node, Node)}
     */
    @Deprecated
    public static int compareRDFTerms(Node node1, Node node2) {
        return NodeCmp.compareRDFTerms(node1, node2);
    }

    // --- Equality tests.

    /** Both null or same node : {@code Node.equals} */
    public static EqualityTest sameNode  = (n1,n2) -> Objects.equals(n1, n2);

    /**
     * Term comparison. Node.equals or lang tags are case insensitive
     */
    public static EqualityTest sameRdfTerm  = (n1,n2) -> NodeFunctions.sameTerm(n1,n2);

    /** sameValue by SPARQL rules */
    public static EqualityTest sameValue = (n1,n2) -> {
        if ( Objects.equals(n1, n2) )
            return true;
        if ( ! n1.isLiteral() || ! n2.isLiteral() )
            return false;
        // 2 literals.
        NodeValue nv1 = NodeValue.makeNode(n1);
        NodeValue nv2 = NodeValue.makeNode(n2);
        try { return NodeValue.sameValueAs(nv1, nv2); }
        catch(ExprEvalException ex)
        {
            // Incomparable as values - must be different for our purposes.
            return false;
        }
    };

    static Set<RDFDatatype> numericDatatypes = new HashSet<>();
    static {
        numericDatatypes.add(XSDDatatype.XSDdecimal) ;
        numericDatatypes.add(XSDDatatype.XSDinteger) ;

        numericDatatypes.add(XSDDatatype.XSDlong) ;
        numericDatatypes.add(XSDDatatype.XSDint) ;
        numericDatatypes.add(XSDDatatype.XSDshort) ;
        numericDatatypes.add(XSDDatatype.XSDbyte) ;

        numericDatatypes.add(XSDDatatype.XSDnonPositiveInteger) ;
        numericDatatypes.add(XSDDatatype.XSDnegativeInteger) ;

        numericDatatypes.add(XSDDatatype.XSDnonNegativeInteger) ;
        numericDatatypes.add(XSDDatatype.XSDpositiveInteger) ;
        numericDatatypes.add(XSDDatatype.XSDunsignedLong) ;
        numericDatatypes.add(XSDDatatype.XSDunsignedInt) ;
        numericDatatypes.add(XSDDatatype.XSDunsignedShort) ;

        numericDatatypes.add(XSDDatatype.XSDdouble) ;
        numericDatatypes.add(XSDDatatype.XSDfloat) ;
    }

    /**
     * Return true if the node is a literal and has an XSD numeric datatype.
     */
    public static boolean isXSDNumeric(Node node) {
        if ( ! node.isLiteral() )
            return false;
        return numericDatatypes.contains(node.getLiteralDatatype());
    }

    /**
     * A Node is a simple string if:
     * <ul>
     * <li>(RDF 1.0) No datatype and no language tag
     * <li>(RDF 1.1) xsd:string
     * </ul>
     */
    public static boolean isSimpleString(Node n) { return Util.isSimpleString(n) ; }

    /**
     * A Node is a language string if it has a language tag.
     * (RDF 1.0 and RDF 1.1)
     */
    public static boolean isLangString(Node n) { return Util.isLangString(n) ; }

}
