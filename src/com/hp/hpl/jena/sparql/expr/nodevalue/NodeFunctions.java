/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr.nodevalue;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.expr.ExprEvalException;
import com.hp.hpl.jena.sparql.expr.ExprTypeException;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.vocabulary.XSD;

/**
 * Implementation of node-centric functions.  
 * @author Andy Seaborne
 */
public class NodeFunctions
{
    private static final NodeValue xsdString = NodeValue.makeNode(XSD.xstring.asNode()) ;
    
    // -------- sameTerm
    
    public static NodeValue sameTerm(NodeValue nv1, NodeValue nv2)
    { return NodeValue.booleanReturn(sameTerm(nv1.asNode(), nv2.asNode())) ; }
    
    public static boolean sameTerm(Node n1, Node n2)
    { 
        if ( n1.equals(n2) )
            return true ;
        if ( n1.isLiteral() && n2.isLiteral() )
        {
            // But language tags are case insensitive.
            String lang1 =  n1.getLiteralLanguage() ;
            String lang2 =  n2.getLiteralLanguage() ;
            
            if ( ! lang1.equals("") && lang1.equalsIgnoreCase(lang2) )
            {
                // Two language tags, equal by case insensitivity.
                boolean b = n1.getLiteralLexicalForm().equals(n2.getLiteralLexicalForm()) ;
                if ( b )
                    return true ;
            }
        }
        return false ;
    }
//    
//    private static boolean sameTermLiterals(Node n1, Node n2)
//    {
//        // But language tags are case insensitive.
//        String lang1 =  n1.getLiteralLanguage() ;
//        String lang2 =  n2.getLiteralLanguage() ;
//        
//        if ( ! lang1.equals("") && lang1.equalsIgnoreCase(lang2) )
//        {
//            // Two language tags, equal by case insensitivity.
//            return n1.getLiteralLexicalForm().equals(n2.getLiteralLexicalForm()) ;
//            if ( b )
//                return true ;
//        }
//        return false ; 
//    }
        
    // -------- RDFterm-equals
    
    public static NodeValue rdfTermEquals(NodeValue nv1, NodeValue nv2)
    { return NodeValue.booleanReturn(rdfTermEquals(nv1.asNode(), nv2.asNode())) ; }
    
    // Exact as defined by SPARQL spec.
    public static boolean rdfTermEquals(Node n1, Node n2)
    { 
        if ( n1.equals(n2) )
            return true ;
        
        if ( n1.isLiteral() && n2.isLiteral() )
        {
            // Two literals, may be sameTerm by language tag case insensitivity.
            String lang1 =  n1.getLiteralLanguage() ;
            String lang2 =  n2.getLiteralLanguage() ;
                
            if ( ! lang1.equals("") && lang1.equalsIgnoreCase(lang2) )
            {
                // Two language tags, equal by case insensitivity.
                boolean b = n1.getLiteralLexicalForm().equals(n2.getLiteralLexicalForm()) ;
                if ( b )
                    return true ;
            }
            // Two literals, different terms, different language tags. 
            NodeValue.raise(new ExprEvalException("Mismatch in RDFterm-equals: "+n1+", "+n2)) ;
        }
        // One or both not a literal.
        return false ;
    }
    
    // -------- str
    public static NodeValue str(NodeValue nv) { return NodeValue.makeString(str(nv.asNode())) ; }
    public static String str(Node node)
    { 
        if ( node.isLiteral() ) return node.getLiteral().getLexicalForm() ;
        if ( node.isURI() )     return node.getURI() ;
//        if ( node.isBlank() )   return node.getBlankNodeId().getLabelString() ;
//        if ( node.isBlank() )   return "" ;
        if ( node.isBlank() )
            NodeValue.raise(new ExprTypeException("Blank node: "+node)) ;
        
        NodeValue.raise(new ExprEvalException("Not a string: "+node)) ;
        return "[undef]" ;
    }
    
    // -------- datatype
    public static NodeValue datatype(NodeValue nv) { return NodeValue.makeNode(datatype(nv.asNode())) ; }
    public static Node datatype(Node node)
    {
        if ( ! node.isLiteral() )
        {
            NodeValue.raise(new ExprTypeException("datatype: Not a literal: "+node) );
            return null ;
        }

        String s = node.getLiteralDatatypeURI() ;
        boolean plainLiteral = (s == null || s.equals("") ) ;
        
        if ( plainLiteral )
        {
            boolean simpleLiteral = (node.getLiteralLanguage() == null || node.getLiteralLanguage().equals("") ) ;
            if ( ! simpleLiteral )
                NodeValue.raise(new ExprTypeException("datatype: Literal has language tag: "+node) );
            return XSD.xstring.asNode() ;
        }
        return Node.createURI(s) ;
    }
    
    // -------- lang
    
    public static NodeValue lang(NodeValue nv)
    { return NodeValue.makeString(lang(nv.asNode())) ; }
    
    public static String lang(Node node)
    {
        if ( ! node.isLiteral() )
            NodeValue.raise(new ExprTypeException("lang: Not a literal: "+FmtUtils.stringForNode(node))) ;

        String s = node.getLiteralLanguage() ;
        if ( s == null )
            s = "" ;
        return s ;
    }
    
    // -------- langMatches
    public static NodeValue langMatches(NodeValue nv, NodeValue nvPattern)
    { return langMatches(nv, nvPattern.getString()) ; }
    
    public static NodeValue langMatches(NodeValue nv, String langPattern)
    {
        Node node = nv.asNode() ;
        if ( ! node.isLiteral() )
        {
            NodeValue.raise(new ExprTypeException("langMatches: not a literal: "+node)) ;
            return null ;
        }

        String nodeLang = node.getLiteralLexicalForm() ;
        
        if ( langPattern .equals("*") )
        {
            if ( nodeLang == null || nodeLang.equals("") ) 
                return NodeValue.FALSE ;
            return NodeValue.TRUE ;
        }
        
        // See RFC 3066 (it's "tag (-tag)*)"

        String[] langElts = nodeLang.split("-") ;
        String[] langRangeElts = langPattern.split("-") ;
        

        /*
         * Here is the logic to compare language code.
         * There is a match if the language matches the
         * parts of the pattern - the language may be longer than
         * the pattern.
         */
        
        /* RFC 4647 basic filtering.
         * 
         * To do extended:
         * 1. Remove any -*- (but not *-)
         * 2. Compare primary tags.
         * 3. Is the remaining range a subsequence of the remaining language tag? 
         */
        
//        // Step one: remove "-*-" (but not "*-")
//        int j = 1 ;
//        for ( int i = 1 ; i < langRangeElts.length ; i++ )
//        {
//            String range = langRangeElts[i] ;
//            if ( range.equals("*") ) 
//                continue ;
//            langRangeElts[j] = range ;
//            j++ ;
//        }
//
//        // Null fill any free space.
//        for ( int i = j ; i < langRangeElts.length ; i++ )
//            langRangeElts[i] = null ;
        
        // This is basic specific.
        
        if ( langRangeElts.length > langElts.length )
            // Lang tag longer than pattern tag => can't match
            return NodeValue.FALSE ;
        for ( int i = 0 ; i < langRangeElts.length ; i++ )
        {
            String range = langRangeElts[i] ;
            if ( range == null )
                break ;
            // Language longer than range
            if ( i >= langElts.length )
                break ;
            String lang = langElts[i] ;
            if ( range.equals("*") )
                continue ;
            if ( ! range.equalsIgnoreCase(lang) )
                return NodeValue.FALSE ;
        }
        return NodeValue.TRUE ;
    }
    
    // -------- isURI/isIRI
    
    public static NodeValue isIRI(NodeValue nv) { return NodeValue.booleanReturn(isIRI(nv.asNode())) ; }   
        
    public static boolean isIRI(Node node)
    {
        if ( node.isURI() )
            return true ;
        return false ;
    }

    public static NodeValue isURI(NodeValue nv) { return NodeValue.booleanReturn(isIRI(nv.asNode())) ; }   
    public static boolean isURI(Node node) { return isIRI(node) ; }
    
    // -------- isBlank
    public static NodeValue isBlank(NodeValue nv) { return NodeValue.booleanReturn(isBlank(nv.asNode())) ; }
    public static boolean isBlank(Node node) { return node.isBlank() ; }
    
    // -------- isLiteral
    public static NodeValue isLiteral(NodeValue nv) { return NodeValue.booleanReturn(isLiteral(nv.asNode())) ; }
    public static boolean isLiteral(Node node) { return node.isLiteral() ; }
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