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

package org.apache.jena.riot.out ;

import java.net.MalformedURLException ;

import org.apache.jena.atlas.io.AWriter ;
import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.iri.IRI ;
import org.apache.jena.iri.IRIFactory ;
import org.apache.jena.iri.IRIRelativize ;
import org.apache.jena.riot.out.NodeToLabel ;
import org.apache.jena.riot.system.PrefixMap ;
import org.apache.jena.riot.system.PrefixMapFactory ;
import org.apache.jena.riot.system.RiotChars ;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.Node ;

public class NodeFormatterTTL extends NodeFormatterNT
{
    private final NodeToLabel nodeToLabel ;
    private final PrefixMap   prefixMap ;
    private final String      baseIRI ;
    private final IRI         iriResolver ;

    public NodeFormatterTTL(String baseIRI, PrefixMap prefixMap) {
        this(baseIRI, prefixMap, NodeToLabel.createBNodeByLabelEncoded()) ;
    }

    public NodeFormatterTTL(String baseIRI, PrefixMap prefixMap, NodeToLabel nodeToLabel) {
        super(CharSpace.UTF8) ;
        this.nodeToLabel = nodeToLabel ;
        if ( prefixMap == null )
            prefixMap = PrefixMapFactory.create() ;
        this.prefixMap = prefixMap ;
        this.baseIRI = baseIRI ;
        this.iriResolver = 
            baseIRI != null ? IRIFactory.jenaImplementation().construct(baseIRI) : null ;
    }

    @Override
    public void formatURI(AWriter w, String uriStr) {
        Pair<String, String> pName = prefixMap.abbrev(uriStr) ;
        // Check if legal
        if ( pName != null ) {
            // Check legal - need to check legal, not for illegal.
            String pref = pName.getLeft() ;
            String ln = pName.getRight() ;
            if ( safeForPrefix(pref) && safeForPrefixLocalname(ln) ) {
                w.print(pName.getLeft()) ;
                w.print(':') ;
                w.print(pName.getRight()) ;
                return ;
            }
        }

        // Attempt base abbreviation.
        if ( iriResolver != null ) {
            String x = abbrevByBase(uriStr) ;
            if ( x != null ) {
                w.print('<') ;
                w.print(x) ;
                w.print('>') ;
                return ;
            }
        }

        // else
        super.formatURI(w, uriStr) ;
    }

    static private int relFlags = IRIRelativize.SAMEDOCUMENT | IRIRelativize.CHILD ;

    /** Abbreviate the URI */
    private String abbrevByBase(String uri) {
        IRI rel = iriResolver.relativize(uri, relFlags) ;
        String r = null ;
        try {
            r = rel.toASCIIString() ;
        } catch (MalformedURLException ex) {
            r = rel.toString() ;
        }
        return r ;
    }

    /* private-testing */
    static boolean safeForPrefix(String str) {
        int N = str.length() ;
        if ( N == 0 )
            return true ;
        int idx = 0 ;
        idx = skip1_PN_CHARS_BASE(str, idx) ;
        if ( idx == -1 )
            return false ;
        idx = skipAny_PN_CHARS_or_DOT(str, idx, N - 1) ;
        if ( idx == -1 )
            return false ;
        // Final char
        idx = skip1_PN_CHARS(str, idx) ;
        if ( idx == -1 )
            return false ;
        return (idx == N) ;
    }

    // @Override
    // public void formatVar(WriterI w, String name)

    // @Override
    // public void formatBNode(WriterI w, String label)

    @Override
    public void formatBNode(AWriter w, Node n) {
        String x = nodeToLabel.get(null, n) ;
        w.print(x) ;
    }

    // @Override
    // public void formatLitString(WriterI w, String lex)

    // @Override
    // public void formatLitLang(WriterI w, String lex, String langTag)

    /* private-testing */static boolean safeForPrefixLocalname(String str) {
        int N = str.length() ;
        if ( N == 0 )
            return true ;
        int idx = 0 ;
        idx = skip1_PN_CHARS_U_or_digit(str, idx) ;
        if ( idx == -1 )
            return false ;
        idx = skipAny_PN_CHARS_or_DOT(str, idx, N - 1) ;
        if ( idx == -1 )
            return false ;
        idx = skip1_PN_CHARS(str, idx) ;
        // Final char
        return (idx == N) ;
    }

    private static boolean is_PN_CHARS_BASE(int ch) {
        return RiotChars.isAlpha(ch) ;
    }

    private static boolean is_PN_CHARS_U(int ch) {
        return is_PN_CHARS_BASE(ch) || ch == '_' ;
    }

    private static boolean is_PN_CHARS(int ch) {
        return is_PN_CHARS_U(ch) || ch == '-' || RiotChars.isDigit(ch) || isCharsExtra(ch) ;
    }

    private static boolean isCharsExtra(int ch) {
        return ch == '\u00B7' || RiotChars.range(ch, '\u0300', '\u036F') || RiotChars.range(ch, '\u203F', '\u2040') ;
    }

    private static int skip1_PN_CHARS_U_or_digit(String str, int idx) {
        char ch = str.charAt(idx) ;
        if ( is_PN_CHARS_U(ch) )
            return idx + 1 ;
        if ( RiotChars.isDigit(ch) )
            return idx + 1 ;
        return -1 ;
    }

    private static int skip1_PN_CHARS_BASE(String str, int idx) {
        char ch = str.charAt(idx) ;
        if ( is_PN_CHARS_BASE(ch) )
            return idx + 1 ;
        return -1 ;
    }

    private static int skipAny_PN_CHARS_or_DOT(String str, int idx, int max) {
        for (int i = idx; i < max; i++) {
            char ch = str.charAt(i) ;
            if ( !is_PN_CHARS(ch) && ch != '.' )
                return i ;
        }
        return max ;
    }

    private static int skip1_PN_CHARS(String str, int idx) {
        char ch = str.charAt(idx) ;
        if ( is_PN_CHARS(ch) )
            return idx + 1 ;
        return -1 ;
    }

    private static final String dtDecimal = XSDDatatype.XSDdecimal.getURI() ;
    private static final String dtInteger = XSDDatatype.XSDinteger.getURI() ;
    private static final String dtDouble  = XSDDatatype.XSDdouble.getURI() ;
    private static final String dtBoolean = XSDDatatype.XSDboolean.getURI() ;

    @Override
    public void formatLitDT(AWriter w, String lex, String datatypeURI) {
        if ( dtDecimal.equals(datatypeURI) ) {
            if ( validDecimal(lex) ) {
                w.print(lex) ;
                return ;
            }
        } else if ( dtInteger.equals(datatypeURI) ) {
            if ( validInteger(lex) ) {
                w.print(lex) ;
                return ;
            }
        }
        if ( dtDouble.equals(datatypeURI) ) {
            if ( validDouble(lex) ) {
                w.print(lex) ;
                return ;
            }
        }
        // Boolean
        if ( dtBoolean.equals(datatypeURI) ) {
            // We leave "0" and "1" as-is assumign that if written like that,
            // there was a reason.
            if ( lex.equals("true") || lex.equals("false") ) {
                w.print(lex) ;
                return ;
            }
        }

        // else.
        super.formatLitDT(w, lex, datatypeURI) ;
    }

    private static boolean validInteger(String lex) {
        int N = lex.length() ;
        if ( N == 0 )
            return false ;
        int idx = 0 ;

        idx = skipSign(lex, idx) ;
        idx = skipDigits(lex, idx) ;
        return (idx == N) ;
    }

    private static boolean validDecimal(String lex) {
        // case : In N3, "." illegal, as is "+." and -." but legal in Turtle.
        int N = lex.length() ;
        if ( N <= 1 )
            return false ;
        int idx = 0 ;

        idx = skipSign(lex, idx) ;
        idx = skipDigits(lex, idx) ; // Maybe none.

        // DOT required.
        if ( idx >= N )
            return false ;

        char ch = lex.charAt(idx) ;
        if ( ch != '.' )
            return false ;
        idx++ ;
        // Digit required.
        if ( idx >= N )
            return false ;
        idx = skipDigits(lex, idx) ;
        return (idx == N) ;
    }

    private static boolean validDouble(String lex) {
        int N = lex.length() ;
        if ( N == 0 )
            return false ;
        int idx = 0 ;

        // Decimal part (except 12. is legal)

        idx = skipSign(lex, idx) ;

        int idx2 = skipDigits(lex, idx) ;
        boolean initialDigits = (idx != idx2) ;
        idx = idx2 ;
        // Exponent required.
        if ( idx >= N )
            return false ;
        char ch = lex.charAt(idx) ;
        if ( ch == '.' ) {
            idx++ ;
            if ( idx >= N )
                return false ;
            idx2 = skipDigits(lex, idx) ;
            boolean trailingDigits = (idx != idx2) ;
            idx = idx2 ;
            if ( idx >= N )
                return false ;
            if ( !initialDigits && !trailingDigits )
                return false ;
        }
        // "e" or "E"
        ch = lex.charAt(idx) ;
        if ( ch != 'e' && ch != 'E' )
            return false ;
        idx++ ;
        if ( idx >= N )
            return false ;
        idx = skipSign(lex, idx) ;
        if ( idx >= N )
            return false ; // At least one digit.
        idx = skipDigits(lex, idx) ;
        return (idx == N) ;
    }

    /**
     * Skip digits [0-9] and return the index just after the digits, which may
     * be beyond the length of the string. May skip zero.
     */
    private static int skipDigits(String str, int start) {
        int N = str.length() ;
        for (int i = start; i < N; i++) {
            char ch = str.charAt(i) ;
            if ( !RiotChars.isDigit(ch) )
                return i ;
        }
        return N ;
    }

    /** Skip any plus or minus */
    private static int skipSign(String str, int idx) {
        int N = str.length() ;
        char ch = str.charAt(idx) ;
        if ( ch == '+' || ch == '-' )
            return idx + 1 ;
        return idx ;
    }
}
