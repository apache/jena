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
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.datatypes.RDFDatatype ;
import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.iri.IRI ;
import org.apache.jena.rdf.model.impl.Util ;
import org.apache.jena.sparql.ARQInternalErrorException ;
import org.apache.jena.sparql.expr.Expr ;
import org.apache.jena.sparql.expr.ExprEvalException ;
import org.apache.jena.sparql.expr.NodeValue ;
import org.apache.jena.sparql.expr.nodevalue.NodeFunctions ;
import org.apache.jena.util.iterator.ExtendedIterator ;
import org.apache.jena.util.iterator.MapFilter ;
import org.apache.jena.util.iterator.MapFilterIterator ;
import org.apache.jena.util.iterator.WrappedIterator ;

/** Node utilities */ 
public class NodeUtils
{
    public interface EqualityTest {
        default boolean equal(Node n1, Node n2) {
			return Objects.equals(n1, n2) ;
		}
    }

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

    /** Get lexical for of anything that looks like a string literal.
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

    /** Convert IRI Nodes to strings.  Skip other kinds of Node */  
    public static Iterator<String> nodesToURIs(Iterator<Node> iter) {
        MapFilter<Node, String> mapper = new MapFilter<Node, String>() {
            @Override
            public String accept(Node x) {
                return x.getURI() ;
            }
        } ;

        ExtendedIterator<Node> eIter = WrappedIterator.create(iter) ;
        Iterator<String> conv = new MapFilterIterator<>(mapper, eIter) ;
        return conv ;
    }
    
    /** Convert a collection of strings to a collection of {@link Node Nodes}. */ 
    public static Collection<Node> convertToNodes(Collection<String> namedGraphs) {
        List<Node> nodes = ListUtils.toList(
            namedGraphs.stream().map(NodeFactory::createURI)
            );
        return nodes;
    }

    /** Convert strings to a collection of {@link Node Nodes}. */ 
    public static Collection<Node> convertToNodes(String... namedGraphs) {
        List<Node> nodes = ListUtils.toList(
            Arrays.stream(namedGraphs).map(NodeFactory::createURI)
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
        // Variables < Blank nodes < URIs < Literals

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

        // No URIs, no blanks nodes by this point
        // And a pair of literals was filterd out first.

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

    // This is term comparison.
    public static EqualityTest sameTerm = new EqualityTest() {
        @Override
        public boolean equal(Node n1, Node n2)
        {
            return NodeFunctions.sameTerm(n1, n2) ;
        }
    } ;
    // This is value comparison
    public static EqualityTest sameValue = new EqualityTest() {
        @Override
        public boolean equal(Node n1, Node n2)
        {
            NodeValue nv1 = NodeValue.makeNode(n1) ;
            NodeValue nv2 = NodeValue.makeNode(n2) ;
            try {
                return NodeValue.sameAs(nv1, nv2) ;
            } catch(ExprEvalException ex)
            {
                // Incomparible as values - must be different for our purposes.
                return false ; 
            }
        }
    } ;
}
