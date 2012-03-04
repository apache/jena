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

package org.openjena.riot.out;

import java.io.IOException ;
import java.io.Writer ;
import java.net.MalformedURLException ;

import org.openjena.atlas.io.IO ;
import org.openjena.atlas.lib.Pair ;
import org.openjena.riot.system.PrefixMap ;
import org.openjena.riot.system.RiotChars ;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.iri.IRI ;
import org.apache.jena.iri.IRIFactory ;
import org.apache.jena.iri.IRIRelativize ;

public class NodeFormatterTTL extends NodeFormatterNT
{
    private final NodeToLabel nodeToLabel = NodeToLabel.createBNodeByLabelEncoded() ;
    private final PrefixMap prefixMap ;
    private final String baseIRI ; 
    
   public NodeFormatterTTL(String baseIRI , PrefixMap prefixMap) //OutputPolicy outputPolicy)
   {
       super(false) ;
       if ( prefixMap == null )
           prefixMap = new PrefixMap() ;
       this.prefixMap = prefixMap ;
       this.baseIRI = baseIRI ;
   }
    
    @Override
    public void formatURI(Writer w, String uriStr)
    {
        try {
            Pair<String, String> pName = prefixMap.abbrev(uriStr) ;
            // Check if legal
            if ( pName != null )
            {
                // Check legal - need to check legal, not for illegal.
                String pref = pName.getLeft() ;
                String ln = pName.getRight() ;
                if ( safeForPrefix(pref) && safeForPrefixLocalname(ln) )
                {
                    w.write(pName.getLeft()) ;
                    w.write(':') ;
                    w.write(pName.getRight()) ;
                    return ;
                }
            }
        
            // Attemp base abbreviation.
            if ( baseIRI != null )
            {
                String x = abbrevByBase(uriStr, baseIRI) ;
                if ( x != null )
                {
                    w.write('<') ;
                    w.write(x) ;
                    w.write('>') ;
                    return ;
                }
            }
        } catch (IOException ex) { IO.exception(ex) ; }
        
        super.formatURI(w, uriStr) ;
    }
    
    static private int relFlags = IRIRelativize.SAMEDOCUMENT | IRIRelativize.CHILD ;
    static private String abbrevByBase(String uri, String base)
    {
        if ( base == null )
            return null ;
        IRI baseIRI = IRIFactory.jenaImplementation().construct(base) ;
        IRI rel = baseIRI.relativize(uri, relFlags) ;
        String r = null ;
        try { r = rel.toASCIIString() ; }
        catch (MalformedURLException  ex) { r = rel.toString() ; }
        return r ;
    }

    /*private-testing*/ 
    static boolean safeForPrefix(String str)
    {
        int N = str.length() ;
        if ( N == 0 ) return true ;
        int idx = 0 ;
        idx = skip1_PN_CHARS_BASE(str, idx) ;
        if ( idx == -1 ) return false ;
        idx = skipAny_PN_CHARS_or_DOT(str, idx) ;
        if ( idx == -1 ) return false ;
        if ( idx == N ) return true ;
        idx = skip1_PN_CHARS(str, idx) ;
        if ( idx == -1 ) return false ;
        return ( idx == N ) ;
    }

//    @Override
//    public void formatVar(Writer w, String name)

//    @Override
//    public void formatBNode(Writer w, String label)

//    @Override
//    public void formatLitString(Writer w, String lex)

//    @Override
//    public void formatLitLang(Writer w, String lex, String langTag)

    /* PN_CHARS_BASE includes escapes. 
     * 
     *  PN_CHARS_BASE ::= [A-Z] | [a-z] | [#00C0-#00D6] | [#00D8-#00F6] | [#00F8-#02FF] | [#0370-#037D] | [#037F-#1FFF]
     *                  | [#200C-#200D] | [#2070-#218F] | [#2C00-#2FEF] | [#3001-#D7FF] | [#F900-#FDCF] | [#FDF0-#FFFD] | [#10000-#EFFFF]
     *                  | UCHAR
     *  PN_CHARS_U    ::= PN_CHARS_BASE | "_"
     *  PN_CHARS      ::= PN_CHARS_U | '-' | [0-9] | #x00B7 | [#x0300-#x036F] | [#x203F-#x2040]
     *  PN_PREFIX     ::= PN_CHARS_BASE ( ( PN_CHARS | "." )* PN_CHARS )?
     *  PN_LOCAL      ::= ( PN_CHARS_U | [0-9] ) ( ( PN_CHARS | "." )* PN_CHARS )?
     */

    /*private-testing*/ static boolean safeForPrefixLocalname(String str)
    {
        int N = str.length() ;
        if ( N == 0 ) return true ;
        int idx = 0 ;
        idx = skip1_PN_CHARS_U_or_029(str, idx) ;
        if ( idx == -1 ) return false ;
        idx = skipAny_PN_CHARS_or_DOT(str, idx) ;
        if ( idx == -1 ) return false ;
        if ( idx == N ) return true ;
        idx = skip1_PN_CHARS(str, idx) ;
        return ( idx == N ) ;
        
//        int N = str.length();
//        if ( N == 0 )
//            return true ;
//        // Test first and last.
//        //char chFirst = str.charAt(0) ;
//        int startIdx = 0 ;
//        
//        char chLast = str.charAt(N-1) ;
//        if ( ! RiotChars.isA2ZN(chLast) &&
//             chLast != '_' )
//            return false ;
//        int lastIdx = N-2 ;
//        
//        for ( int i = startIdx ; i <= lastIdx ; i++ )
//        {
//            char ch = str.charAt(i) ;
//            if ( ! RiotChars.isA2ZN(ch) &&
//                 ch != '_' &&
//                 ch != '.' )
//                return false ;
//        }
//        return true ;
    }

    private static boolean is_PN_CHARS_BASE(int ch)    { return RiotChars.isAlpha(ch) ; }
    private static boolean is_PN_CHARS_U(int ch)       { return is_PN_CHARS_BASE(ch) || ch == '_' ; }
    private static boolean is_PN_CHARS(int ch)         { return is_PN_CHARS_U(ch) || ch == '-' || RiotChars.isDigit(ch) || isCharsExtra(ch) ; }
    
    public static boolean isCharsExtra(int ch)
    {
        return ch == '\u00B7' || RiotChars.range(ch, '\u0300', '\u036F') || RiotChars.range(ch, '\u203F', '\u2040')  ;  
    }
    
    private static int skip1_PN_CHARS_U_or_029(String str, int idx)
    {
        char ch = str.charAt(idx) ;
        if ( is_PN_CHARS_U(ch) ) return idx+1 ;
        if ( RiotChars.isDigit(ch) ) return idx+1 ;
        return -1 ;
    }

    private static int skip1_PN_CHARS_BASE(String str, int idx)
    {
        char ch = str.charAt(idx) ;
        if ( is_PN_CHARS_BASE(ch) ) return idx+1 ;
        return -1 ;
    }

    private static int skipAny_PN_CHARS_or_DOT(String str, int idx)
    {
        int N = str.length() ;
        for ( int i = idx ; i < N ; i++ )
        {
            char ch = str.charAt(i) ;
            if ( ! is_PN_CHARS(ch) && ch != '.' ) return i ;
        }
        return N ;
    }

    private static int skip1_PN_CHARS(String str, int idx)
    {
        char ch = str.charAt(idx) ;
        if ( is_PN_CHARS(ch) ) return idx+1 ;
        return -1 ;
    }

    @Override
    public void formatLitDT(Writer w, String lex, String datatypeURI)
    {
        try {
            String dtDecimal = XSDDatatype.XSDdecimal.getURI() ;
            String dtInteger = XSDDatatype.XSDinteger.getURI() ;
            String dtDouble = XSDDatatype.XSDdouble.getURI() ;

            if ( dtDecimal.equals(datatypeURI) )
            {
                if ( validDecimal(lex) )
                {
                    w.write(lex) ;
                    return ;
                }
            }
            else if ( dtInteger.equals(datatypeURI) )
            {
                if ( validInteger(lex) )
                {
                    w.write(lex) ;
                    return ;
                }
            }
            if ( dtDouble.equals(datatypeURI) )
            {
                if ( validDouble(lex) )
                {
                    w.write(lex) ;
                    return ; 
                }
            }
        } catch (IOException ex) { IO.exception(ex) ; } 

        super.formatLitDT(w, lex, datatypeURI) ;
    }


    private static boolean validInteger(String lex)
    {
        int N = lex.length() ;
        if ( N == 0 ) return false ;
        int idx = 0 ;
        
        idx = skipSign(lex, idx) ;
        idx = skipDigits(lex, idx) ;
        return ( idx == N ) ;
    }
    
    private static boolean validDecimal(String lex)
    {
        // case : "." illegal.
        int N = lex.length() ;
        if ( N <= 1 ) return false ;
        int idx = 0 ;
        
        idx = skipSign(lex, idx) ;
        idx = skipDigits(lex, idx) ;    // Maybe none.
        
        // DOT required.
        if ( idx >= N ) return false ;
        
        char ch = lex.charAt(idx) ;
        if ( ch != '.' ) return false ;
        idx++ ;
        // Digit required.
        if ( idx >= N ) return false ;
        idx = skipDigits(lex, idx) ;
        return ( idx == N )  ;
    }
    
    private static boolean validDouble(String lex)
    {
        int N = lex.length() ;
        if ( N == 0 ) return false ;
        int idx = 0 ;
        
        // Decimal part (except 12. is legal)
        
        idx = skipSign(lex, idx) ;
        
        int idx2 = skipDigits(lex, idx) ;
        boolean initialDigits = ( idx != idx2) ;
        idx = idx2 ;
        // Exponent required.
        if ( idx >= N ) return false ;
        char ch = lex.charAt(idx) ;
        if ( ch == '.' )
        {
            idx++ ;
            if ( idx >= N ) return false ;
            idx2 = skipDigits(lex, idx) ;
            boolean trailingDigits = ( idx != idx2 ) ;
            idx = idx2 ;
            if ( idx >= N ) return false ;
            if ( !initialDigits && !trailingDigits ) return false ;
        }
        // "e" or "E"
        ch = lex.charAt(idx) ;
        if ( ch != 'e' && ch != 'E' ) return false ;
        idx++ ;
        if ( idx >= N ) return false ;
        idx = skipSign(lex, idx) ;
        if ( idx >= N ) return false ;  // At least one digit.
        idx = skipDigits(lex, idx) ;
        return ( idx == N )  ;
    }
    
    /** Skip digits [0-9] and return the index just after the digits,
     * which may be beyond the length of the string.
     * May skip zero.
     */
    private static int skipDigits(String str, int start)
    {
        int N = str.length() ;
        for ( int i = start ; i < N ; i++ )
        {
            char ch = str.charAt(i) ;
            if ( ! RiotChars.isDigit(ch) )
                return i ;
        } 
        return N ;
    }
    
    /** Skip any plus or minus */
    private static int skipSign(String str, int idx)
    {
        int N = str.length() ;
        char ch = str.charAt(idx) ;
        if ( ch == '+' || ch == '-' )
            return idx + 1 ;
        return idx ;
    }
}
