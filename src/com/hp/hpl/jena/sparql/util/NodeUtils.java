/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.util;

import java.util.Iterator;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.ARQInternalErrorException;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.MapFilter;
import com.hp.hpl.jena.util.iterator.MapFilterIterator;
import com.hp.hpl.jena.util.iterator.WrappedIterator;


/** Node utilities. 
 * @author Andy Seaborne
 */ 


public class NodeUtils
{
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
            public String accept(Node x)
            {
                return x.getURI() ;  
            }} ;
        
        ExtendedIterator<Node> eIter = WrappedIterator.create(iter) ;
        Iterator<String> conv = new MapFilterIterator<Node, String>(mapper, eIter) ;
        return conv ;
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
        // Blank nodes < URIs < Literals
        
        if ( node1.isBlank() )
        {
            if ( node2.isBlank() )
            {
                String s1 = node1.getBlankNodeId().getLabelString() ;
                String s2 = node2.getBlankNodeId().getLabelString() ;
                return StringUtils.strCompare(s1, s2) ;
            }
            // bNodes before anything else.
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
                return StringUtils.strCompare(s1, s2) ; 
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
        
        int x = StringUtils.strCompare(lex1, lex2) ;
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
              x = StringUtils.strCompareIgnoreCase(lang1, lang2) ;
              if ( x != Expr.CMP_EQUAL )
                  return x ;
              x = StringUtils.strCompare(lang1, lang2) ;
              if ( x != Expr.CMP_EQUAL )
                  return x ;
              throw new ARQInternalErrorException("compareLiteralsBySyntax: lexical form and languages tags identical on non.equals literals");
        }
        
        // Two datatypes.
        return StringUtils.strCompare(dt1, dt2) ;
    }
    
    private static boolean simpleLiteral(Node node)
    {
        return  node.getLiteralDatatypeURI() == null && 
                node.getLiteralLanguage().equals("") ; 
    }
}

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */