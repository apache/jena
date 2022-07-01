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

package org.apache.jena.util;

import org.apache.jena.graph.Node ;
import org.apache.jena.rdf.model.impl.Util ;
//import org.apache.jena.riot.system.RiotChars ;

/**
 * Code to split an URI or IRI into prefix and local part.
 * Historically, 'prefix' is referred to as 'namespace'
 * reflecting RDF/XML history.
 * <p>
 * For display, use {@link #localname} and {@link #namespace}.
 * This follows Turtle, adds some pragmatic rulesm but does not escape
 * any characters. A URI is split never split before the last {@code /}
 * or last {@code #}, if present.
 * See {@link #splitpoint} for more details.
 * <p>
 * This code form the machinery behind {@link Node#getLocalName}
 * {@link Node#getNameSpace} for URI Nodes.
 * <p>
 * {@link #localnameTTL} is strict Turtle; it is the same local name as
 * before, but escaped if necessary.
 * <p>
 * The functions {@link #namespaceXML} and {@link #localnameXML}
 * apply the rules for XML qnames.
 */
public class SplitIRI
{
    /** Return the 'namespace' (prefix) for a URI string.
     * Use with {@link #localname}
     */
    public static String namespace(String string) {
        int i = splitpoint(string) ;
        if ( i < 0 )
            return string ;
        return string.substring(0, i) ;
    }

    /** Calculate a localname - do not escape PN_LOCAL_ESC.
     * This is not guaranteed to be legal Turtle.
     * Use with {@link #namespace}
     */
    public static String localname(String string) {
        int i = splitpoint(string) ;
        if ( i < 0 )
            return "" ;
        return string.substring(i) ;
    }

    /** Return the 'namespace' (prefix) for a URI string,
     * legal for Turtle and goes with {@link #localnameTTL}
     */
    public static String namespaceTTL(String string) {
        return namespace(string) ;
    }

    /** Calculate a localname - enforce legal Turtle
     * escape PN_LOCAL_ESC, check for final '.'
     * Use with {@link #namespaceTTL}
     */
    public static String localnameTTL(String string) {
        String x = localname(string) ;
        if ( x.isEmpty())
            return x ;
        return escape_PN_LOCAL_ESC(x) ;
    }

    private static String escape_PN_LOCAL_ESC(String x) {
        // Assume that escapes are rare so scan once to make sure there
        // is work to do then scan again doing the work.
        //'\' ('_' | '~' | '.' | '-' | '!' | '$' | '&' | "'" | '(' | ')' | '*' | '+' | ',' | ';' | '=' | '/' | '?' | '#' | '@' | '%')

        int N = x.length() ;
        boolean escchar = false ;
        for ( int i = 0 ; i < N ; i++ ) {
            char ch = x.charAt(i) ;
            if ( needsEscape(ch, (i==N-1)) ) {
                escchar = true ;
                break ;
            }
        }
        if ( ! escchar )
            return x ;
        StringBuilder sb = new StringBuilder(N+10) ;
        for ( int i = 0 ; i < N ; i++ ) {
            char ch = x.charAt(i) ;
            // DOT only needs escaping at the end
            if ( needsEscape(ch, (i==N-1) )  )
                sb.append('\\') ;
            sb.append(ch) ;
        }
        return sb.toString() ;
    }

    private static boolean needsEscape(char ch, boolean finalChar) {
        if ( ch == '.' )
            return finalChar ;
        return isPN_LOCAL_ESC(ch) ;
    }

    // @formatter:off
    /* From the RDF 1.1 Turtle specification:
        [136s]  PrefixedName    ::=     PNAME_LN | PNAME_NS
        Productions for terminals

        [163s]  PN_CHARS_BASE   ::=     [A-Z] | [a-z] | [#x00C0-#x00D6] | [#x00D8-#x00F6] | [#x00F8-#x02FF] | [#x0370-#x037D] | [#x037F-#x1FFF] | [#x200C-#x200D] | [#x2070-#x218F] | [#x2C00-#x2FEF] | [#x3001-#xD7FF] | [#xF900-#xFDCF] | [#xFDF0-#xFFFD] | [#x10000-#xEFFFF]
        [164s]  PN_CHARS_U  ::=     PN_CHARS_BASE | '_'
        [166s]  PN_CHARS    ::=     PN_CHARS_U | '-' | [0-9] | #x00B7 | [#x0300-#x036F] | [#x203F-#x2040]
        [167s]  PN_PREFIX   ::=     PN_CHARS_BASE ((PN_CHARS | '.')* PN_CHARS)?

        [168s]  PN_LOCAL    ::=     (PN_CHARS_U | ':' | [0-9] | PLX) ((PN_CHARS | '.' | ':' | PLX)* (PN_CHARS | ':' | PLX))?
        [169s]  PLX     ::=     PERCENT | PN_LOCAL_ESC
        [170s]  PERCENT     ::=     '%' HEX HEX
        [171s]  HEX     ::=     [0-9] | [A-F] | [a-f]
        [172s]  PN_LOCAL_ESC    ::=     '\' ('_' | '~' | '.' | '-' | '!' | '$' | '&' | "'" | '(' | ')' | '*' | '+' | ',' | ';' | '=' | '/' | '?' | '#' | '@' | '%')
*/
    // @formatter:on

    /** Find the URI split point, return the index into the string that is the
     *  first character of a legal Turtle local name.
     * <p>
     * This is a pragmatic choice, not just finding the maximal point.
     * For example, with escaping '/' can be included but that means
     * {@code http://example/path/abc} could split to give {@code http://example/}
     * and {@code path/abc} .
     * <p>
     * Split URN's after ':'.
     *
     * @param uri URI string
     * @return The split point, or -1 for "not found".
     */
    public static int splitpoint(String uri) {
        boolean isURN = uri.startsWith("urn:") ;
        // Fast track.  Still need to check validity of the prefix part.
        int idx1 = uri.lastIndexOf('#') ;
        // Not so simple - \/ in local names
        int idx2 = isURN ? uri.lastIndexOf(':') : uri.lastIndexOf('/') ;

        // If absolute.
        int idx3 = uri.indexOf(':') ;

        // Note: local names can't end in "." in Turtle.
        // This is handled by escape_PN_LOCAL_ESC which will escape it as "\."

        // Cases
        //   "abc#def"
        //   "/abc"
        //   "/"
        //   "/path/path#frag
        //   "/path/path#abc/def" :: / in fragment, split is at the "#".

        int limit;
        if ( idx1 >= 0 && idx2 < 0 ) {
            // No path "/" (or ":" if a URN)
            limit = idx1;
        } else if ( idx1 < 0 && idx2 >= 0 ) {
            // No fragment
            limit = idx2;
        } else if ( idx1 >= 0 && idx2 >= 0 ) {
            // Fragment and path. Use fragment.
            // If "/" is in the fragment, it is not the split point.
            limit = idx1 ;
        } else {
            limit = -1;
        }

        // At least idx3, the case of no "/" and no "#" in an absolute IRI
        if ( idx3 >= 0 )
            limit = Math.max(limit, idx3) ;

        // Limit is our guess.
        // Now search end of URI to this guess checking the characters found.

        int splitPoint = -1 ;
        // Work backwards, checking for
        // ((PN_CHARS | '.' | ':' | PLX)*
        for ( int i = uri.length()-1 ; i > limit ; i-- ) {
            char ch = uri.charAt(i) ;
            if ( /*RiotChars.*/isPNChars_U_N(ch) || /*RiotChars.*/isPN_LOCAL_ESC(ch) || ch == ':' || ch == '-' || ch == '.' )
                continue ;
            splitPoint = i+1 ;
            break ;
        }
        // limit was at the end.  No split point (we could escape the limit point)
        if ( splitPoint == -1 )
            splitPoint = limit+1 ;
        // No split point.
        if ( splitPoint >= uri.length() )
            return -1 ;

        // Check the first character of the local name.
        // All characters are legal localname name characters but may not satisfy the additional
        // first character rule.  Move forward to first legal first character.
        int ch = uri.charAt(splitPoint) ;
        while ( ch == '.' || ch == '-' ) {
            splitPoint++ ;
            if ( splitPoint >= uri.length() )
                return -1 ;
            ch = uri.charAt(splitPoint) ;
        }

        // Checking the final '.' is done when checking for escapes.
        return splitPoint ;
    }

    private static boolean checkhex(String uri, int i) {
        return /*RiotChars.*/isHexChar(uri.charAt(i)) ;
    }

    // Assuming legal URIs, there is no work to be done
    // for %XX.  If illegal (e.g. %X), the best we can do
    // is not mess them up.
    /*
        // %  - just need to check that it is followed by two hex.
        if ( ch == '%' ) {
            if ( i+2 >= uri.length() ) {
                // Too short
                return -1 ;
            }
            if ( ! checkhex(uri, i+1) || ! checkhex(uri, i+2) )
                return -1 ;
        }

     */
    /** Split point, according to XML qname rules.
     * This is the longest NCName at the end of the uri.
     * See {@link Util#splitNamespaceXML}.
     */
    public static int splitXML(String string) { return Util.splitNamespaceXML(string) ; }

    /** Namespace, according to XML qname rules.
     * Use with {@link #localnameXML}.
     */
    public static String namespaceXML(String string) {
        int i = splitXML(string) ;
        return string.substring(0, i) ;
    }

    /** Localname, according to XML qname rules. */
    public static String localnameXML(String string) {
        int i = splitXML(string) ;
        return string.substring(i) ;
    }

    // Extracted from RiotChars
    // When/if RIOT becomes accessible to this code, then refactor

    private static boolean /*RiotChars.*/isPN_LOCAL_ESC(char ch) {
        switch (ch) {
            case '\\': case '_':  case '~': case '.': case '-': case '!': case '$':
            case '&':  case '\'': case '(': case ')': case '*': case '+': case ',':
            case ';':  case '=':  case '/': case '?': case '#': case '@': case '%':
                return true ;
            default:
                return false ;
        }
    }

    /** ASCII 0-9 */
    private static boolean isDigit(int ch) {
        return range(ch, '0', '9') ;
    }

    private static boolean isPNCharsBase(int ch) {
        // PN_CHARS_BASE ::= [A-Z] | [a-z] | [#x00C0-#x00D6] | [#x00D8-#x00F6] | [#x00F8-#x02FF] |
        //                   [#x0370-#x037D] | [#x037F-#x1FFF] | [#x200C-#x200D] | [#x2070-#x218F] |
        //                   [#x2C00-#x2FEF] | [#x3001-#xD7FF] | [#xF900-#xFDCF] | [#xFDF0-#xFFFD] |
        //                   [#x10000-#xEFFFF]
        return
            r(ch, 'a', 'z') || r(ch, 'A', 'Z') || r(ch, 0x00C0, 0x00D6) || r(ch, 0x00D8, 0x00F6) || r(ch, 0x00F8, 0x02FF) ||
            r(ch, 0x0370, 0x037D) || r(ch, 0x037F, 0x1FFF) || r(ch, 0x200C, 0x200D) || r(ch, 0x2070, 0x218F) ||
            r(ch, 0x2C00, 0x2FEF) || r(ch, 0x3001, 0xD7FF) ||
            // Surrogate pairs
            r(ch, 0xD800, 0xDFFF) ||
            r(ch, 0xF900, 0xFDCF) || r(ch, 0xFDF0, 0xFFFD) ||
            r(ch, 0x10000, 0xEFFFF) ; // Outside the basic plane.
    }

    private static boolean isPNChars_U(int ch) {
        //PN_CHARS_BASE | '_'
        return isPNCharsBase(ch) || ( ch == '_' ) ;
    }

    private static boolean isPNChars_U_N(int ch) {
        // PN_CHARS_U | [0-9]
        return isPNCharsBase(ch) || ( ch == '_' ) || isDigit(ch) ;
    }

    private static boolean isPNChars(int ch) {
        // PN_CHARS ::=  PN_CHARS_U | '-' | [0-9] | #x00B7 | [#x0300-#x036F] | [#x203F-#x2040]
        return isPNChars_U(ch) || isDigit(ch) || ( ch == '-' ) || ch == 0x00B7 || r(ch, 0x300, 0x036F) || r(ch, 0x203F, 0x2040) ;
    }

    /** Hexadecimal character */
    private static boolean isHexChar(int ch) {
        return range(ch, '0', '9') || range(ch, 'a', 'f') || range(ch, 'A', 'F') ;
    }

    private static int valHexChar(int ch) {
        if ( range(ch, '0', '9') )
            return ch - '0' ;
        if ( range(ch, 'a', 'f') )
            return ch - 'a' + 10 ;
        if ( range(ch, 'A', 'F') )
            return ch - 'A' + 10 ;
        return -1 ;
    }

    private static boolean r(int ch, int a, int b) { return ( ch >= a && ch <= b ) ; }

    private static boolean range(int ch, char a, char b) {
        return (ch >= a && ch <= b) ;
    }
}

