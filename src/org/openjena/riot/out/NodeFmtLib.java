/*
 * (c) Copyright 2009 Talis Systems Ltd.
 * (c) Copyright 2010, 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.riot.out;

import org.openjena.atlas.lib.Bytes ;
import org.openjena.atlas.lib.Chars ;
import org.openjena.riot.RiotException ;
import org.openjena.riot.system.PrefixMap ;
import org.openjena.riot.system.Prologue ;
import org.openjena.riot.system.RiotChars ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Node_Literal ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.rdf.model.RDFNode ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;

public class NodeFmtLib
{
    // TODO Switch to using NodeFormatter.
    
    
    // FmtUtils: This writes abbreviated bnodes (_:b0 etc)
    // These utilities are lower level and reflect the bNodes label.
    
    
    public static String str(Triple t)
    {
        return 
            serialize(t.getSubject()) + " " +
            serialize(t.getPredicate()) + " " +
            serialize(t.getObject()) ; 
    }

    public static String str(Quad q)
    {
        return 
            serialize(q.getGraph()) + " " +
            serialize(q.getSubject()) + " " +
            serialize(q.getPredicate()) + " " +
            serialize(q.getObject()) ; 
    }

    private static final boolean onlySafeBNodeLabels = false ;

    public static String displayStr(Node n) { return FmtUtils.stringForNode(n) ; }

    public static String serialize(Node n)
    { return serialize(n, null, null) ; }

    public static String serialize(Node n, Prologue prologue)
    { return serialize(n, prologue.getBaseURI(), prologue.getPrefixMap()) ; }

    
    /** Encoding of a node so it can be reconstructed */ 
    public static String serialize(Node n, String base, PrefixMap prefixMap)
    {
        // See also Nodec.
        // See also OutputLangUtils - merge and this is a buffering call.
        
        if ( n == null )
            return "<<null>>" ;
        
        if ( n.isBlank() )
        {
            String str = n.getBlankNodeLabel() ;
            // c.f. OutputLangUtils
            if ( onlySafeBNodeLabels )
                str = encodeBNodeLabel(str) ;
            return "_:"+str ;
        }
        
        if ( n.isLiteral() )
            return FmtUtils.stringForLiteral((Node_Literal)n, null) ;

        if ( n.isURI() )
        {
            String uri = n.getURI() ;
            return stringForURI(uri, base, prefixMap) ;
        }
        
        // Safe name?
        if ( n.isVariable() )
            return "?"+n.getName() ;
//        
//        if ( n.equals(Node.ANY) )
//            return "ANY" ;

        throw new RiotException("Failed to turn a node into a string: "+n) ;
        //return null ;
    }
    
    // c.f. FmtUtils.stringForURI
    // Uses PrefixMap, not PrefixMapping
    static String stringForURI(String uri, String base, PrefixMap mapping)
    {
        if ( mapping != null )
        {
            String pname = mapping.abbreviate(uri) ;
            if ( pname != null )
                return pname ;
        }
        if ( base != null )
        {
            String x = FmtUtils.abbrevByBase(uri, base) ;
            if ( x != null ) 
                return "<"+x+">" ;
        }
        return FmtUtils.stringForURI(uri) ; 
    }
    
    // Strict N-triples only allows [A-Za-z][A-Za-z0-9]
    static char encodeMarkerChar = 'X' ;

    // These two form a pair to convert bNode labels to a safe (i.e. legal N-triples form) and back agains. 
    
    // Encoding is:
    // 1 - Add a Letter 
    // 2 - Hexify, as Xnn, anything outside ASCII A-Za-z0-9
    // 3 - X is encoded as XX
    
    private static char LabelLeadingLetter = 'B' ; 
    
    public static String encodeBNodeLabel(String label)
    {
        StringBuilder buff = new StringBuilder() ;
        // Must be at least one char and not a digit.
        buff.append(LabelLeadingLetter) ;
        
        for ( int i = 0 ; i < label.length() ; i++ )
        {
            char ch = label.charAt(i) ;
            if ( ch == encodeMarkerChar )
            {
                buff.append(ch) ;
                buff.append(ch) ;
            }
            else if ( RiotChars.isA2ZN(ch) )
                buff.append(ch) ;
            else
                Chars.encodeAsHex(buff, encodeMarkerChar, ch) ;
        }
        return buff.toString() ;
    }

    // Assumes that blank nodes only have characters in the range of 0-255
    public static String decodeBNodeLabel(String label)
    {
        StringBuilder buffer = new StringBuilder() ;

        if ( label.charAt(0) != LabelLeadingLetter )
            return label ;
        
        // Skip first.
        for ( int i = 1; i < label.length(); i++ )
        {
            char ch = label.charAt(i) ;
            
            if ( ch != encodeMarkerChar )
            {
                buffer.append(ch) ;
            }
            else
            {
                // Maybe XX or Xnn.
                char ch2 = label.charAt(i+1) ;
                if ( ch2 == encodeMarkerChar )
                {
                    i++ ;
                    buffer.append(ch) ;
                    continue ;
                }
                
                // Xnn
                i++ ;
                char hiC = label.charAt(i) ;
                int hi = Bytes.hexCharToInt(hiC) ;
                i++ ;
                char loC = label.charAt(i) ;
                int lo = Bytes.hexCharToInt(loC) ;

                int combined = ((hi << 4) | lo) ;
                buffer.append((char) combined) ;
            }
        }

        return buffer.toString() ;
    }

    
    //public static String safeBNodeLabel(String label)
    
    public static String displayStr(Triple t, PrefixMapping prefixMapping)
    {
        return FmtUtils.stringForTriple(t, prefixMapping) ;
    }

    public static String displayStr(RDFNode obj)
    {
        return FmtUtils.stringForRDFNode(obj) ;
    }
    
    //public static String str(Quad quad) {}
    
//    public static String stringEsc(String s)    { return FmtUtils.stringEsc(s) ; }
//    
//    public static String stringEsc(String s, boolean singleLineString)
//    { return FmtUtils.stringEsc(s, singleLineString) ; }
//    
//    public static void stringEsc(StringBuilder sbuff, String s)
//    { FmtUtils.stringEsc(sbuff, s) ; }
//
//    public static void stringEsc(StringBuilder sbuff, String s, boolean singleLineString)
//    { FmtUtils.stringEsc(sbuff, s, singleLineString) ; }
//
//    public static String unescapeStr(String s)    { return ParserBase.unescapeStr(s) ; }
    
}

/*
 * (c) Copyright 2009 Talis Systems Ltd.
 * (c) Copyright 2010, 2011 Epimorphics Ltd.
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