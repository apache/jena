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

package com.hp.hpl.jena.n3.turtle;

import com.hp.hpl.jena.datatypes.RDFDatatype ;
import com.hp.hpl.jena.datatypes.TypeMapper ;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.n3.IRIResolver ;
import com.hp.hpl.jena.n3.JenaURIException ;
import com.hp.hpl.jena.rdf.model.AnonId ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl ;
import com.hp.hpl.jena.vocabulary.RDF ;

@SuppressWarnings("deprecation")
public class ParserBase
{
    // Should be the same as ARQ ParserBase and Prologues.
    protected final Node XSD_TRUE   = NodeFactory.createLiteral("true", null, XSDDatatype.XSDboolean) ;
    protected final Node XSD_FALSE  = NodeFactory.createLiteral("false", null, XSDDatatype.XSDboolean) ;
    
    protected final Node nRDFtype       = RDF.type.asNode() ;
    
    protected final Node nRDFnil        = RDF.nil.asNode() ;
    protected final Node nRDFfirst      = RDF.first.asNode() ;
    protected final Node nRDFrest       = RDF.rest.asNode() ;
    
    protected final Node nRDFsubject    = RDF.subject.asNode() ;
    protected final Node nRDFpredicate  = RDF.predicate.asNode() ;
    protected final Node nRDFobject     = RDF.object.asNode() ;

    protected final String SWAP_NS      = "http://www.w3.org/2000/10/swap/" ;
    protected final String SWAP_LOG_NS  = "http://www.w3.org/2000/10/swap/log#" ;
    protected final Node nLogImplies    = NodeFactory.createURI(SWAP_LOG_NS+"implies") ;
    
    protected final Node nOwlSameAs     = NodeFactory.createURI("http://www.w3.org/2002/07/owl#sameAs") ;
    
    protected boolean strictTurtle = true ;
    protected boolean skolomizedBNodes = true ; 
    
    public ParserBase() {}
    
    PrefixMapping prefixMapping = new PrefixMappingImpl() ;
    IRIResolver resolver = new IRIResolver() ;
    
    protected String getBaseURI()       { return resolver.getBaseIRI() ; }
    public void setBaseURI(String u)
    {
        resolver = new IRIResolver(u) ;
    }
    
    protected void setBase(String iriStr , int line, int column)
    {
        // Already resolved.
        setBaseURI(iriStr) ;
    }
    
    public PrefixMapping getPrefixMapping() { return prefixMapping ; }
    
    // label => bNode for construct templates patterns
    LabelToNodeMap bNodeLabels = new LabelToNodeMap() ;
    
    TurtleEventHandler handler = null ; 
    public void setEventHandler(TurtleEventHandler h) { handler = h ; }
    
    protected void emitTriple(int line, int col, Triple triple)
    {
        handler.triple(line, col, triple) ;
    }
    
    protected void startFormula(int line, int col)
    { handler.startFormula(line, col) ; }
    
    protected void endFormula(int line, int col)
    {handler.endFormula(line, col) ; }
    
    protected void setPrefix(int line, int col, String prefix, String uri)
    {
        prefixMapping.setNsPrefix(prefix, uri) ;
        handler.prefix(line, col, prefix, uri) ;
    }
    
    protected int makePositiveInteger(String lexicalForm)
    {
        if ( lexicalForm == null )
            return -1 ;
        
        return Integer.parseInt(lexicalForm) ;
    }
    
    protected Node createLiteralInteger(String lexicalForm)
    {
        return NodeFactory.createLiteral(lexicalForm, null, XSDDatatype.XSDinteger) ;
    }
    
    protected Node createLiteralDouble(String lexicalForm)
    {
        return NodeFactory.createLiteral(lexicalForm, null, XSDDatatype.XSDdouble) ;
    }
    
    protected Node createLiteralDecimal(String lexicalForm)
    {
        return NodeFactory.createLiteral(lexicalForm, null, XSDDatatype.XSDdecimal) ;
    }

    protected Node createLiteral(String lexicalForm, String langTag, Node datatype)
    {
        String uri = (datatype==null) ? null : datatype.getURI() ;
        return createLiteral(lexicalForm, langTag,  uri) ;
    }
    
    protected Node createLiteral(String lexicalForm, String langTag, String datatypeURI)
    {
        Node n = null ;
        // Can't have type and lang tag.
        if ( datatypeURI != null)
        {
            RDFDatatype dType = TypeMapper.getInstance().getSafeTypeByName(datatypeURI) ;
            n = NodeFactory.createLiteral(lexicalForm, null, dType) ;
        }
        else
            n = NodeFactory.createLiteral(lexicalForm, langTag, null) ;
        return n ;
    }
    
    protected long integerValue(String s)
    {
        if ( s.startsWith("+") )
            s = s.substring(1) ;
        if ( s.startsWith("0x") )
        {
            // Hex
            s = s.substring(2) ;
            return Long.parseLong(s, 16) ;
        }
        return Long.parseLong(s) ;
    }
    
    protected double doubleValue(String s)
    {
        if ( s.startsWith("+") )
            s = s.substring(1) ;
        double valDouble = Double.parseDouble(s) ;
        return valDouble ; 
    }
    
    protected String stripQuotes(String s)
    {
        return s.substring(1,s.length()-1)  ;
    }
    
    protected String stripQuotes3(String s)
    {
        return s.substring(3,s.length()-3)  ;
    }
    
    protected String stripChars(String s, int n)
    {
        return s.substring(n, s.length())  ;
    }

    protected String resolveQuotedIRI(String iriStr ,int line, int column)
    {
        iriStr = stripQuotes(iriStr) ;
        return resolveIRI(iriStr, line, column) ;
    }
    
    protected String resolveIRI(String iriStr , int line, int column)
    {
        if ( isBNodeIRI(iriStr) )
            return iriStr ;
        
        if ( resolver != null )
            iriStr = _resolveIRI(iriStr, line, column) ;
        return iriStr ;
    }
    
    private String _resolveIRI(String iriStr , int line, int column)
    {
        try { iriStr = resolver.resolve(iriStr) ; }
        catch (JenaURIException ex) { throwParseException(ex.getMessage(), line, column) ; }
        return iriStr ;
    }
    
    protected String resolvePName(String qname, int line, int column)
    {
        String s = myExpandPrefix(prefixMapping, qname) ;
        if ( s == null )
            throwParseException("Unresolved prefixed name: "+qname, line, column) ;
        return s ;
    }

    private static String myExpandPrefix(PrefixMapping prefixMapping, String qname)
    {
        String s = prefixMapping.expandPrefix(qname) ;
        if ( s == null )
            return null ;
        if ( s.equals(qname) )
        {
            // The contract of expandPrefix is to return the original name if
            // there is no prefix but what s the expanded and original form are
            // actually the same character string ?
            int colon = qname.indexOf( ':' );
            if (colon < 0) 
                return null ;
            String prefix = qname.substring( 0, colon ) ;
            if ( prefixMapping.getNsPrefixURI(prefix) != null )
                // The original and resolved forms are the same.
                return s ;
            return null ;
        }
        return s ;
    }
    
    final static String bNodeLabelStart = "_:" ;
    
    protected Node createListNode() { return createBNode() ; }

    // Unlabelled bNode.
    protected Node createBNode() { return bNodeLabels.allocNode() ; }
    
    //  Labelled bNode.
    protected Node createBNode(String label, int line, int column)
    { 
        return bNodeLabels.asNode(label) ;
    }
    protected Node createVariable(String s, int line, int column)
    {
        s = s.substring(1) ; // Drop the marker
        return NodeFactory.createVariable(s) ;
    }
    
    protected Node createNode(String iri)
    {
        // Is it a bNode label? i.e. <_:xyz>
        if ( isBNodeIRI(iri) )
        {
            String s = iri.substring(bNodeLabelStart.length()) ;
            Node n = NodeFactory.createAnon(new AnonId(s)) ;
            return n ;
        }
        return NodeFactory.createURI(iri) ;
    }
    
    protected boolean isBNodeIRI(String iri)
    {
        return skolomizedBNodes && iri.startsWith(bNodeLabelStart) ;
    }
    

    
//    protected Node createNodeFromURI(String s, int line, int column)
//    {
//        s = stripQuotes(s) ;
//        String uriStr = s ;     // Mutated
//        
//        try {
//            uriStr = resolver.resolve(uriStr) ;
//        } catch (JenaURIException ex)
//        {
//            throw new TurtleParseException(exMsg(ex.getMessage(), line, column)) ;
//        }
//        return Node.createURI(uriStr) ;
//    }
    
    protected void throwParseException(String s , int line, int column)
    {
        throw new TurtleParseException(exMsg(s, line, column)) ;
    }
    
    protected String fixupPrefix(String prefix, int line, int column)
    {
        if ( prefix.endsWith(":") )
            prefix = prefix.substring(0, prefix.length()-1) ;
        return prefix ; 
    }
    
    // Utilities to remove escapes
    
    // Testing interface
    public static String unescapeStr(String s)
    { return unescape(s, '\\', false, 1, 1) ; }

//    public static String unescapeCodePoint(String s)
//    { return unescape(s, '\\', true, 1, 1) ; }
//
//    protected String unescapeCodePoint(String s, int line, int column)
//    { return unescape(s, '\\', true, line, column) ; }

    
    protected String unescapeStr(String s, int line, int column)
    { return unescape(s, '\\', false, line, column) ; }
    
    // Worker function
    private static String unescape(String s, char escape, boolean pointCodeOnly, int line, int column)
    {
        int i = s.indexOf(escape) ;
        
        if ( i == -1 )
            return s ;
        
        // Dump the initial part straight into the string buffer
        StringBuilder sb = new StringBuilder(s.substring(0,i)) ;
        int len = s.length() ;
        for ( ; i < len ; i++ )
        {
            char ch = s.charAt(i) ;
            // Keep line and column numbers.
            switch (ch)
            {
                case '\n': 
                case '\r':
                    line++ ;
                    column = 1 ;
                    break ;
                default:
                    column++ ;
                    break ;
            }

            if ( ch != escape )
            {
                sb.append(ch) ;
                continue ;
            }
                
            // Escape
            if ( i >= len-1 )
                throw new TurtleParseException(exMsg("Illegal escape at end of string", line, column)) ;
            char ch2 = s.charAt(i+1) ;
            column = column+1 ;
            i = i + 1 ;
            
            // \\u and \\U
            if ( ch2 == 'u' )
            {
                // i points to the \ so i+6 is next character
                if ( i+4 >= len )
                    throw new TurtleParseException(exMsg("\\u escape too short", line, column)) ;
                int x = hex(s, i+1, 4, line, column) ;
                sb.append((char)x) ;
                // Jump 1 2 3 4 -- already skipped \ and u
                i = i+4 ;
                column = column+4 ;
                continue ;
            }
            if ( ch2 == 'U' )
            {
                // i points to the \ so i+6 is next character
                if ( i+8 >= len )
                    throw new TurtleParseException(exMsg("\\U escape too short", line, column)) ;
                int x = hex(s, i+1, 8, line, column) ;
                sb.append((char)x) ;
                // Jump 1 2 3 4 5 6 7 8 -- already skipped \ and u
                i = i+8 ;
                column = column+8 ;
                continue ;
            }
            
            // Are we doing just point code escapes?
            // If so, \X-anything else is legal as a literal "\" and "X" 
            
            if ( pointCodeOnly )
            {
                sb.append('\\') ;
                sb.append(ch2) ;
                i = i + 1 ;
                continue ;
            }
            
            // Not just codepoints.  Must be a legal escape.
            char ch3 = 0 ;
            switch (ch2)
            {
                case 'n': ch3 = '\n' ;  break ; 
                case 't': ch3 = '\t' ;  break ;
                case 'r': ch3 = '\r' ;  break ;
                case 'b': ch3 = '\b' ;  break ;
                case 'f': ch3 = '\f' ;  break ;
                case '\'': ch3 = '\'' ; break ;
                case '\"': ch3 = '\"' ; break ;
                case '\\': ch3 = '\\' ; break ;
                default:
                    throw new TurtleParseException(exMsg("Unknown escape: \\"+ch2, line, column)) ;
            }
            sb.append(ch3) ;
        }
        return sb.toString() ;
    }

    // Line and column that started the escape
    static private int hex(String s, int i, int len, int line, int column)
    {
//        if ( i+len >= s.length() )
//        {
//            
//        }
        int x = 0 ;
        for ( int j = i ; j < i+len ; j++ )
        {
           char ch = s.charAt(j) ;
           column++ ;
           int k = 0  ;
           switch (ch)
           {
               case '0': k = 0 ; break ; 
               case '1': k = 1 ; break ;
               case '2': k = 2 ; break ;
               case '3': k = 3 ; break ;
               case '4': k = 4 ; break ;
               case '5': k = 5 ; break ;
               case '6': k = 6 ; break ;
               case '7': k = 7 ; break ;
               case '8': k = 8 ; break ;
               case '9': k = 9 ; break ;
               case 'A': case 'a': k = 10 ; break ;
               case 'B': case 'b': k = 11 ; break ;
               case 'C': case 'c': k = 12 ; break ;
               case 'D': case 'd': k = 13 ; break ;
               case 'E': case 'e': k = 14 ; break ;
               case 'F': case 'f': k = 15 ; break ;
               default:
                   throw new TurtleParseException(exMsg("Illegal hex escape: "+ch, line, column)) ;
           }
           x = (x<<4)+k ;
        }
        return x ;
    }
    
    protected static String exMsg(String msg, int line, int column)
    {
        return "Line " + line + ", column " + column + ": " + msg ;
    }
}
