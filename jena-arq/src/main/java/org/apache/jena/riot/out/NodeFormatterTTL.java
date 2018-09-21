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
import org.apache.jena.atlas.lib.CharSpace ;
import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.graph.Node ;
import org.apache.jena.iri.IRI ;
import org.apache.jena.iri.IRIRelativize ;
import org.apache.jena.riot.system.IRIResolver ;
import org.apache.jena.riot.system.PrefixMap ;
import org.apache.jena.riot.system.PrefixMapFactory ;
import static org.apache.jena.riot.system.RiotChars.* ;

/** Node formatter for Turtle using single line strings */ 
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
            baseIRI != null ? IRIResolver.iriFactory().construct(baseIRI) : null ;
    }

    @Override
    public void formatURI(AWriter w, String uriStr) {
        Pair<String, String> pName = prefixMap.abbrev(uriStr) ;
        // Check if legal
        if ( pName != null ) {
            // Check legal - need to check its legal, not for illegal.
            // The splitter in "abbrev" only has a weak rule.
            String prefix = pName.getLeft() ;
            String localname = pName.getRight() ;
            
            if ( safePrefixName(prefix, localname) ) {
                w.print(prefix) ;
                w.print(':') ;
                w.print(localname) ;
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

    @Override
    public void formatBNode(AWriter w, Node n) {
        String x = nodeToLabel.get(null, n) ;
        w.print(x) ;
    }

    // From NodeFormatterNT:
    
    // @Override
    // public void formatVar(WriterI w, String name)

    // @Override
    // public void formatLitString(WriterI w, String lex)

    // @Override
    // public void formatLitLang(WriterI w, String lex, String langTag)

    static boolean safePrefixName(String prefix, String localname) {
        return safeForPrefix(prefix) && safeForPrefixLocalname(localname) ;    
    }
    
    // [139s]  PNAME_NS        ::=     PN_PREFIX? ':'
    // [140s]  PNAME_LN        ::=     PNAME_NS PN_LOCAL
    
    // [167s]  PN_PREFIX       ::=     PN_CHARS_BASE ((PN_CHARS | '.')* PN_CHARS)?
    // [168s]  PN_LOCAL        ::=     (PN_CHARS_U | ':' | [0-9] | PLX) ((PN_CHARS | '.' | ':' | PLX)* (PN_CHARS | ':' | PLX))?

    // [163s]  PN_CHARS_BASE   ::=     [A-Z] | [a-z] | [#x00C0-#x00D6] | [#x00D8-#x00F6] | [#x00F8-#x02FF] | [#x0370-#x037D] | [#x037F-#x1FFF] | [#x200C-#x200D] | [#x2070-#x218F] | [#x2C00-#x2FEF] | [#x3001-#xD7FF] | [#xF900-#xFDCF] | [#xFDF0-#xFFFD] | [#x10000-#xEFFFF]
    // [164s]  PN_CHARS_U      ::=     PN_CHARS_BASE | '_'
    // [166s]  PN_CHARS        ::=     PN_CHARS_U | '-' | [0-9] | #x00B7 | [#x0300-#x036F] | [#x203F-#x2040]
    
    // [169s]  PLX             ::=     PERCENT | PN_LOCAL_ESC
    // [170s]  PERCENT         ::=     '%' HEX HEX
    // [171s]  HEX             ::=     [0-9] | [A-F] | [a-f]
    // [172s]  PN_LOCAL_ESC    ::=     '\' ('_' | '~' | '.' | '-' | '!' | '$' | '&' | "'" | '(' | ')' | '*' | '+' | ',' | ';' | '=' | '/' | '?' | '#' | '@' | '%')
    
    /* private-testing */
    static boolean safeForPrefix(String str) {
        // PN_PREFIX ::= PN_CHARS_BASE ((PN_CHARS | '.')* PN_CHARS)?
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
        idx = skip1_PN_CHARS(str, idx) ;
        if ( idx == -1 )
            return false ;
        return (idx == N) ;
    }

    /* private-testing */static boolean safeForPrefixLocalname(String str) {
        // PN_LOCAL ::=  (PN_CHARS_U | ':' | [0-9] | PLX) ((PN_CHARS | '.' | ':' | PLX)* (PN_CHARS | ':' | PLX))?
        // This code does not consider PLX (which is more than one character).
        int N = str.length() ;
        if ( N == 0 )
            return true ;
        int idx = 0 ;
        idx = skip1_PN_CHARS_U_or_digit_or_COLON(str, idx) ;
        if ( idx == -1 )
            return false ;
        idx = skipAny_PN_CHARS_or_DOT_or_COLON(str, idx, N - 1) ;
        if ( idx == -1 )
            return false ;
        // Final char
        idx = skip1_PN_CHARS_or_COLON(str, idx) ;
        if ( idx == -1 )
            return false ;
        // We got to the end.
        return (idx == N) ;
    }

    // ---- Prefix name : prefix part
    
    private static int skip1_PN_CHARS_BASE(String str, int idx) {
        char ch = str.charAt(idx) ;
        if ( isPNCharsBase(ch) )
            return idx + 1 ;
        return -1 ;
    }

    private static int skipAny_PN_CHARS_or_DOT(String str, int idx, int max) {
        for (int i = idx; i < max; i++) {
            char ch = str.charAt(i) ;
            if ( !isPNChars(ch) && ch != '.' )
                return i ;
        }
        return max ;
    }

    private static int skip1_PN_CHARS(String str, int idx) {
        char ch = str.charAt(idx) ;
        if ( isPNChars(ch) )
            return idx + 1 ;
        return -1 ;
    }

    // ---- Prefix name : local part

    private static int skip1_PN_CHARS_U_or_digit_or_COLON(String str, int idx) {
        char ch = str.charAt(idx) ;
        if ( isPNChars_U(ch) )
            return idx + 1 ;
        if ( isDigit(ch) )
            return idx + 1 ;
        if ( ch == ':' )
            return idx + 1 ;
        return -1 ;
    }
    
    private static int skipAny_PN_CHARS_or_DOT_or_COLON(String str, int idx, int max) {
        for (int i = idx; i < max; i++) {
            char ch = str.charAt(i) ;
            if ( !isPNChars(ch) && ch != '.' && ch != ':' )
                return i ;
        }
        return max ;
    }

    private static int skip1_PN_CHARS_or_COLON(String str, int idx) {
        char ch = str.charAt(idx) ;
        if ( isPNChars(ch) )
            return idx + 1 ;
        if ( ch == ':' )
            return idx + 1 ;
        return -1 ;
    }

    // ---- 
    
    private static final String dtDecimal = XSDDatatype.XSDdecimal.getURI() ;
    private static final String dtInteger = XSDDatatype.XSDinteger.getURI() ;
    private static final String dtDouble  = XSDDatatype.XSDdouble.getURI() ;
    private static final String dtBoolean = XSDDatatype.XSDboolean.getURI() ;

    @Override
    public void formatLitDT(AWriter w, String lex, String datatypeURI) {
        boolean b = writeLiteralAbbreviated(w, lex, datatypeURI) ;
        if ( b ) return ;
        writeLiteralLongForm(w, lex, datatypeURI) ;
    }
    
    protected void writeLiteralLongForm(AWriter w, String lex, String datatypeURI) {
        writeLiteralOneLine(w, lex, datatypeURI);
    }

    protected void writeLiteralOneLine(AWriter w, String lex, String datatypeURI) {
        super.formatLitDT(w, lex, datatypeURI) ;
    }

    /** Write in a short form, e.g. integer.
     * @return True if a short form was output else false. 
     */
    protected boolean writeLiteralAbbreviated(AWriter w, String lex, String datatypeURI) {
        if ( dtDecimal.equals(datatypeURI) ) {
            if ( validDecimal(lex) ) {
                w.print(lex) ;
                return true ;
            }
        } else if ( dtInteger.equals(datatypeURI) ) {
            if ( validInteger(lex) ) {
                w.print(lex) ;
                return true ;
            }
        } else if ( dtDouble.equals(datatypeURI) ) {
            if ( validDouble(lex) ) {
                w.print(lex) ;
                return true ;
            }
        } else if ( dtBoolean.equals(datatypeURI) ) {
            // We leave "0" and "1" as-is assumign that if written like that,
            // there was a reason.
            if ( lex.equals("true") || lex.equals("false") ) {
                w.print(lex) ;
                return true ;
            }
        }
        return false ;
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
            if ( ! isDigit(ch) )
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
