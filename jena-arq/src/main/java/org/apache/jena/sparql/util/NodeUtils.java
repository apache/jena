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
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.datatypes.RDFDatatype ;
import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.graph.Triple;
import org.apache.jena.iri.IRI ;
import org.apache.jena.rdf.model.impl.Util ;
import org.apache.jena.sparql.ARQInternalErrorException ;
import org.apache.jena.sparql.expr.Expr ;
import org.apache.jena.sparql.expr.ExprEvalException ;
import org.apache.jena.sparql.expr.NodeValue ;
import org.apache.jena.sparql.expr.nodevalue.NodeFunctions ;
import org.apache.jena.util.iterator.ExtendedIterator ;
import org.apache.jena.util.iterator.WrappedIterator ;

/** Node utilities */
public class NodeUtils
{
    /** IRI to Node */
    public static Node asNode(IRI iri) {
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

    /** Compare two Nodes, based on their RDF terms forms, not value */
    public static int compareRDFTerms(Node node1, Node node2) {
        if ( node1 == null ) {
            if ( node2 == null )
                return Expr.CMP_EQUAL ;
            return Expr.CMP_LESS ;
        }

        if ( node2 == null )
            return Expr.CMP_GREATER ;

        // No nulls.
        if ( node1.isLiteral() && node2.isLiteral() )
            return compareLiteralsBySyntax(node1, node2) ;

        // One or both not literals
        // Variables < Blank nodes < URIs < Literals < Triple Terms

        //-- Variables
        if ( node1.isVariable() ) {
            if ( node2.isVariable() ) {
                return StrUtils.strCompare(node1.getName(), node2.getName()) ;
            }
            // Variables before anything else
            return Expr.CMP_LESS ;
        }

        if ( node2.isVariable() ) {
            // node1 not variable
            return Expr.CMP_GREATER ;
        }

        //-- Blank nodes
        if ( node1.isBlank() ) {
            if ( node2.isBlank() ) {
                String s1 = node1.getBlankNodeId().getLabelString() ;
                String s2 = node2.getBlankNodeId().getLabelString() ;
                return StrUtils.strCompare(s1, s2) ;
            }
            // bNodes before anything but variables
            return Expr.CMP_LESS ;
        }

        if ( node2.isBlank() )
            // node1 not blank.
            return Expr.CMP_GREATER ;

        // Not blanks. 2 URI or one URI and one literal

        //-- URIs
        if ( node1.isURI() ) {
            if ( node2.isURI() ) {
                String s1 = node1.getURI() ;
                String s2 = node2.getURI() ;
                return StrUtils.strCompare(s1, s2) ;
            }
            return Expr.CMP_LESS ;
        }

        if ( node2.isURI() )
            return Expr.CMP_GREATER ;

        // -- Two literals already done just leaving ...
        if ( node2.isLiteral() )
            return Expr.CMP_GREATER;

        // Because triple terms are after literals ...
        if ( node1.isLiteral() )
            return Expr.CMP_LESS;

        // -- Triple nodes.
        if ( node1.isNodeTriple() ) {
            if ( node2.isNodeTriple() ) {
                Triple t1 = node1.getTriple();
                Triple t2 = node2.getTriple();
                int x1 = compareRDFTerms(t1.getSubject(), t2.getSubject());
                if ( x1 != Expr.CMP_EQUAL )
                    return x1;
                int x2 = compareRDFTerms(t1.getPredicate(), t2.getPredicate());
                if ( x2 != Expr.CMP_EQUAL )
                    return x2;
                int x3 = compareRDFTerms(t1.getObject(), t2.getObject());
                if ( x3 != Expr.CMP_EQUAL )
                    return x3;
                return Expr.CMP_EQUAL;
            }
        }

        if ( node2.isNodeTriple() )
            return Expr.CMP_GREATER;

        // No URIs, no blanks, no literals, no triple terms nodes by this point

        // Should not happen.
        throw new ARQInternalErrorException("Compare: " + node1 + "  " + node2) ;
    }

    /** Compare literals by kind - not by value.
     *  Gives a deterministic, stable, arbitrary ordering between unrelated literals.
     *
     * Ordering:
     *  <ol>
     *  <li>By lexical form</li>
     *  <li> For same lexical form:
     *       <ul>
     *       <li>  RDF 1.0 : simple literal < literal by lang < literal with type
     *       <li>  RDF 1.1 : xsd:string < rdf:langString < other dataypes.<br/>
     *             This is the closest to SPARQL 1.1: treat xsd:string as a simple literal</ul></li>
     *  <li> Lang by sorting on language tag (first case insensitive then case sensitive)
     *  <li> Datatypes by URI
     *  </ol>
     */

    private static int compareLiteralsBySyntax(Node node1, Node node2) {
        if ( node1 == null || !node1.isLiteral() || node2 == null || !node2.isLiteral() )
            throw new ARQInternalErrorException("compareLiteralsBySyntax called with non-literal: (" + node1 + "," + node2 + ")") ;

        if ( node1.equals(node2) )
            return Expr.CMP_EQUAL ;

        String lex1 = node1.getLiteralLexicalForm() ;
        String lex2 = node2.getLiteralLexicalForm() ;

        int x = StrUtils.strCompare(lex1, lex2) ;
        if ( x != Expr.CMP_EQUAL )
            return x ;

        // Same lexical form. Not .equals()
        if ( isSimpleString(node1) ) // node2 not a simple string because they
                                     // would be .equals
            return Expr.CMP_LESS ;
        if ( isSimpleString(node2) )
            return Expr.CMP_GREATER ;
        // Neither simple string / xsd:string(RDF 1.1)

        // Both language strings?
        if ( isLangString(node1) && isLangString(node2) ) {
            String lang1 = node1.getLiteralLanguage() ;
            String lang2 = node2.getLiteralLanguage() ;
            x = StrUtils.strCompareIgnoreCase(lang1, lang2) ;
            if ( x != Expr.CMP_EQUAL )
                return x ;
            x = StrUtils.strCompare(lang1, lang2) ;
            if ( x != Expr.CMP_EQUAL )
                return x ;
            throw new ARQInternalErrorException("compareLiteralsBySyntax: lexical form and languages tags identical on non.equals literals") ;
        }

        // One a language string?
        if ( isLangString(node1) )
            return Expr.CMP_LESS ;
        if ( isLangString(node2) )
            return Expr.CMP_GREATER ;

        // Both have other datatypes. Neither simple nor language tagged.
        String dt1 = node1.getLiteralDatatypeURI() ;
        String dt2 = node2.getLiteralDatatypeURI() ;
        // Two datatypes.
        return StrUtils.strCompare(dt1, dt2) ;
    }

    /**
     * A Node is a simple string if:
     * <li>(RDF 1.0) No datatype and no language tag
     * <li>(RDF 1.1) xsd:string
     */
    public static boolean isSimpleString(Node n) { return Util.isSimpleString(n) ; }

    /**
     * A Node is a language string if it has a language tag.
     * (RDF 1.0 and RDF 1.1)
     */
    public static boolean isLangString(Node n) { return Util.isLangString(n) ; }


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
        try { return NodeValue.sameAs(nv1, nv2); }
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
}
