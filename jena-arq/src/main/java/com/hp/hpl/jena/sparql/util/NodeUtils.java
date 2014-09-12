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

package com.hp.hpl.jena.sparql.util;

import java.util.Collection ;
import java.util.HashSet ;
import java.util.Iterator ;
import java.util.Set ;


import com.hp.hpl.jena.datatypes.RDFDatatype ;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;

import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.iri.IRI ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprEvalException ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeFunctions ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;
import com.hp.hpl.jena.util.iterator.MapFilter ;
import com.hp.hpl.jena.util.iterator.MapFilterIterator ;
import com.hp.hpl.jena.util.iterator.WrappedIterator ;


/** Node utilities */ 


public class NodeUtils
{
    public interface EqualityTest { boolean equal(Node n1, Node n2) ; }

    public static Node asNode(IRI iri)  { return NodeFactory.createURI(iri.toString()) ; }
    public static Node asNode(String iri)  { return NodeFactory.createURI(iri) ; }
    
    public static boolean isStringLiteral(Node literal)
    {
        if ( ! literal.isLiteral() )
            return false ;
        RDFDatatype dType = literal.getLiteralDatatype() ;  
        String langTag = literal.getLiteralLanguage() ;
        
        // Language?
        if ( langTag == null || ! langTag.equals("") ) return false ;
        
        // Datatype
        if ( dType != null && ! dType.equals(XSDDatatype.XSDstring) )
            return false ;
        
        return true ;
    }
    
    public static boolean hasLang(Node node)
    {
        if ( ! node.isLiteral() ) return false ;
        String x = node.getLiteralLanguage() ;
        if ( x == null ) return false ;
        if ( x.equals("") ) return false ;
        return true ;
    }
    
    // Get the string value of plain literal or XSD string.  
    
    public static String stringLiteral(Node literal)
    {
        if ( ! isStringLiteral(literal) ) return null ;
        return literal.getLiteralLexicalForm() ; 
    }
    
    public static Iterator<String> nodesToURIs(Iterator<Node> iter)
    { 
        MapFilter<Node, String> mapper = new MapFilter<Node, String>(){
            @Override
            public String accept(Node x)
            {
                return x.getURI() ;  
            }} ;
        
        ExtendedIterator<Node> eIter = WrappedIterator.create(iter) ;
        Iterator<String> conv = new MapFilterIterator<>(mapper, eIter) ;
        return conv ;
    }
    
    public static Set<Node> convertToNodes(Collection<String> uris)
    {
        Set<Node> nodes = new HashSet<>() ;
        for ( String x : uris )
            nodes.add(NodeFactory.createURI(x)) ;
        return nodes ;
    }
    
    
    /** Compare two Nodes, based on their RDF terms forms, not value */
    public static int compareRDFTerms(Node node1, Node node2)
    {
        if ( node1 == null )
        {
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
        
        if ( node1.isVariable() )
        {
            if ( node2.isVariable() )
            {
                return StrUtils.strCompare(node1.getName(), node2.getName()) ;
            }
            // Variables before anything else
            return Expr.CMP_LESS;
        }
        
        if ( node2.isVariable() ) {
            // node1 not variable
            return Expr.CMP_GREATER ;
        }
        
        if ( node1.isBlank() )
        {
            if ( node2.isBlank() )
            {
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
        
        // Not blanks.  2 URI or one URI and one literal
        
        if ( node1.isURI() )
        {
            if ( node2.isURI() )
            {
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
        throw new ARQInternalErrorException("Compare: "+node1+"  "+node2) ;
    }

    // Compare literals by kind - not by value.
    // public for testing - otherwise call compareRDFTerms
    // Ordering:
    //  1/ By lexical form
    //  2/ For same lexical form: 
    //         simple literal < literal by lang < literal with type
    //  3/ Lang by sorting on language tag (first case insensistive then case sensitive)
    //  4/ Datatypes by URI
    
    private static int compareLiteralsBySyntax(Node node1, Node node2)
    {
        if ( node1 == null || ! node1.isLiteral() ||
        node2 == null || ! node2.isLiteral() )
            throw new ARQInternalErrorException("compareLiteralsBySyntax called with non-literal: ("+node1+","+node2+")") ;

        if ( node1.equals(node2) )
            return Expr.CMP_EQUAL ;

        String lex1 = node1.getLiteralLexicalForm() ;
        String lex2 = node2.getLiteralLexicalForm() ;
        
        int x = StrUtils.strCompare(lex1, lex2) ;
        if ( x != Expr.CMP_EQUAL )
            return x ;
 
        // Same lexical form. Not .equals()
        
        String lang1 = node1.getLiteralLanguage() ;
        String lang2 = node2.getLiteralLanguage() ;
        
        String dt1 = node1.getLiteralDatatypeURI() ;
        String dt2 = node2.getLiteralDatatypeURI() ;

        if ( lang1 == null )
            throw new ARQInternalErrorException("Language tag is null: "+node1) ; 
        if ( lang2 == null )
            throw new ARQInternalErrorException("Language tag is null: "+node2) ; 
        
        if ( simpleLiteral(node1) )
            // Node 2 can't be simple because they'd be the same 
            return Expr.CMP_LESS ;

        if ( simpleLiteral(node2) )
            return Expr.CMP_GREATER ;
        
        // Neither simple.
        
        // Language before datatypes.
        // Can't both be no lang, no datatype
        // because they are already same lexcial form
        // so they'd be same simple literal.
        
        if ( ! lang1.equals("") && dt2 != null )
            return Expr.CMP_LESS ;
        
        if ( dt1 != null && ! lang2.equals("") )
            return Expr.CMP_GREATER ;
        
        // Both language tags, or both datatypes
        
        if ( dt1 == null && dt2 == null )
        {
              // Syntactic - lang tags case considered
              // case sensitive if necessary
              x = StrUtils.strCompareIgnoreCase(lang1, lang2) ;
              if ( x != Expr.CMP_EQUAL )
                  return x ;
              x = StrUtils.strCompare(lang1, lang2) ;
              if ( x != Expr.CMP_EQUAL )
                  return x ;
              throw new ARQInternalErrorException("compareLiteralsBySyntax: lexical form and languages tags identical on non.equals literals");
        }
        
        // Two datatypes.
        return StrUtils.strCompare(dt1, dt2) ;
    }
    
    private static boolean simpleLiteral(Node node)
    {
        return  node.getLiteralDatatypeURI() == null && 
                node.getLiteralLanguage().equals("") ; 
    }

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
