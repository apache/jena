/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.lang.sse;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.ARQInternalErrorException;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.core.VarAlloc;
import com.hp.hpl.jena.sparql.util.LabelToNodeMap;
import com.hp.hpl.jena.vocabulary.RDF;

public class ParserBase
{
    protected final Node XSD_TRUE   = Node.createLiteral("true", null, XSDDatatype.XSDboolean) ;
    protected final Node XSD_FALSE  = Node.createLiteral("false", null, XSDDatatype.XSDboolean) ;
    
    protected final Node nRDFtype       = RDF.type.asNode() ;
    
    protected final Node nRDFnil        = RDF.nil.asNode() ;
    protected final Node nRDFfirst      = RDF.first.asNode() ;
    protected final Node nRDFrest       = RDF.rest.asNode() ;
    
    protected final Node nRDFsubject    = RDF.subject.asNode() ;
    protected final Node nRDFpredicate  = RDF.predicate.asNode() ;
    protected final Node nRDFobject     = RDF.object.asNode() ;
    private PrefixMapping prefixMapping = null ;
    
    // label => bNode
    private LabelToNodeMap bNodeLabels = LabelToNodeMap.createBNodeMap();
    
//    // label => bNode / Var
//    LabelToNodeMap anonVarLabels = LabelToNodeMap.createVarMap() ;
    
    // Allocation for bNodes during parsing.
    private LabelToNodeMap activeLabelMap = bNodeLabels ;
    
    public ParserBase() {}
    public ParserBase(PrefixMapping pmap) { this.prefixMapping = pmap ; }

    protected int makeInteger(String lexicalForm)
    {
        if ( lexicalForm == null )
            return -1 ;
        
        return Integer.parseInt(lexicalForm) ;
    }
    
    protected Node makeNodeInteger(String lexicalForm)
    {
        return Node.createLiteral(lexicalForm, null, XSDDatatype.XSDinteger) ;
    }
    
    protected Node makeNodeDouble(String lexicalForm)
    {
        return Node.createLiteral(lexicalForm, null, XSDDatatype.XSDdouble) ;
    }
    
    protected Node makeNodeDecimal(String lexicalForm)
    {
        return Node.createLiteral(lexicalForm, null, XSDDatatype.XSDdecimal) ;
    }

    protected Node makeNode(String lexicalForm, String langTag, Node datatype)
    {
        String uri = (datatype==null) ? null : datatype.getURI() ;
        return makeNode(lexicalForm, langTag,  uri) ;
    }
    
    protected Node makeNode(String lexicalForm, String langTag, String datatypeURI)
    {
        Node n = null ;
        // Can't have type and lang tag.
        if ( datatypeURI != null)
        {
            RDFDatatype dType = TypeMapper.getInstance().getSafeTypeByName(datatypeURI) ;
            n = Node.createLiteral(lexicalForm, null, dType) ;
        }
        else
            n = Node.createLiteral(lexicalForm, langTag, null) ;
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
        
    protected Var createVariable(String s, int line, int column)
    {
        s = s.substring(1) ; // Drop the marker
        return Var.alloc(s) ;
    }
    
    VarAlloc varAlloc = new VarAlloc("") ;
    protected Var createVariable()
    {
        return varAlloc.allocVar() ;
    }
    
    protected Node createNodeFromPrefixedName(String s, int line, int column)
    {
        //s = fixupPrefixedName(s, line, column) ;
        return Node.createURI(s) ;
    }
    
    final static String bNodeLabelStart = "_:" ;
    //RefBoolean skolomizedBNodes = new RefBoolean(ARQ.constantBNodeLabels) ;
    boolean skolomizedBNodes = true ;
    
    protected Node createNodeFromURI(String s, int line, int column)
    {
        s = stripQuotes(s) ;
        //s = unescapeCodePoint(s, line, column) ;
        String uriStr = s ;     // Mutated
        
        // Is it a bNode label? i.e. <_:xyz>
        if ( skolomizedBNodes && s.startsWith(bNodeLabelStart) )
        {
            s = s.substring(bNodeLabelStart.length()) ;
            Node n = Node.createAnon(new AnonId(s)) ;
            return n ;
        }
        
        return Node.createURI(uriStr) ;
    }
    
    protected Node createNodeFromQName(String s, int line, int column)
    {
        s = fixupPrefixedName(s, line, column) ;
        return Node.createURI(s) ;
    }
    
    // Unlabelled bNode.
    protected Node createBNode() { return activeLabelMap.allocNode() ; }
    
    // Labelled bNode.
    protected Node createBNode(String label, int line, int column)
    { 
        return activeLabelMap.asNode(label) ;
    }
    
    private String fixupPrefixedName(String qname, int line, int column)
    {
        String s = qname ;
        if ( prefixMapping != null )
            s = prefixMapping.expandPrefix(qname) ;
        else
            s = ":"+s ;
//        if ( s == null )
//        {
//            String msg = "Line " + line + ", column " + column;
//            throw new QNameException(msg+": Unresolved prefixed name: "+qname, line, column) ; 
//        }
        return s ;
    }
    
//    protected String fixupPrefix(String prefix, int line, int column)
//    {
//        if ( prefix.endsWith(":") )
//            prefix = prefix.substring(0, prefix.length()-1) ;
//        return prefix ; 
//    }
    
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
        StringBuffer sb = new StringBuffer(s.substring(0,i)) ;
        
        for ( ; i < s.length() ; i++ )
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
            if ( i >= s.length()-1 )
                throw makeParseException("Illegal escape at end of string", line, column) ;
            char ch2 = s.charAt(i+1) ;
            column = column+1 ;
            i = i + 1 ;
            
            // \\u and \\U
            if ( ch2 == 'u' )
            {
                // i points to the \ so i+6 is next character
                if ( i+4 >= s.length() )
                    throw makeParseException("\\u escape too short", line, column) ;
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
                if ( i+8 >= s.length() )
                    throw makeParseException("\\U escape too short", line, column) ;
                int x = hex(s, i+1, 8, line, column) ;
                // Convert to UTF-16 codepoint pair.
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
                    throw makeParseException("Unknown escape: \\"+ch2, line, column) ;
            }
            sb.append(ch3) ;
        }
        return sb.toString() ;
    }

    // Line and column that started the escape
    static private int hex(String s, int i, int len, int line, int column)
    {
        if ( i+len >= s.length() )
            throw new ARQInternalErrorException("Hex sequnce trigger by : ["+line+", "+column+"]") ;
        
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
                   throw makeParseException("Illegal hex escape: "+ch, line, column) ;
           }
           x = (x<<4)+k ;
        }
        return x ;
    }
    
    protected static SSEParseException makeParseException(String msg, int line, int column)
    {
        return new SSEParseException("Line " + line + ", column " + column + ": " + msg,
                                      line, column) ;
    }
}
/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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