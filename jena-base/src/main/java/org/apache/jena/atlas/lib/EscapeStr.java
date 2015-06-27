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

package org.apache.jena.atlas.lib;

import org.apache.jena.atlas.AtlasException ;
import org.apache.jena.atlas.io.AWriter ;
import org.apache.jena.atlas.io.IndentedLineBuffer ;
import org.apache.jena.atlas.io.OutputUtils ;

public class EscapeStr
{
    // Tests: TestOutput
    // See also OutputLangUtils.outputEsc.
//    private final boolean ascii ;
//
//    public EscapeStr(CharSpace charSpace) { this.ascii = ( charSpace == CharSpace.ASCII ) ; } 
//
//    public void writeURI(AWriter w, String s)
//    {
//        if ( ascii )
//            stringEsc(w, s, true, ascii) ;
//        else
//            // It's a URI - assume legal.
//            w.print(s) ;
//    }
//
//    public void writeStr(AWriter w, String s) 
//    {
//        stringEsc(w, s, true, ascii) ;
//    }
//
//    public void writeStrMultiLine(AWriter w, String s) 
//    {
//        // N-Triples does not have """
//        stringEsc(w, s, false, ascii) ;
//    }
//
    // Utility
    /*
     * Escape characters in a string according to Turtle rules. 
     */
    public static String stringEsc(String s)
    { return stringEsc(s, true, false) ; }

    private static String stringEsc(String s, boolean singleLineString, boolean asciiOnly)
    {
        IndentedLineBuffer sb = new IndentedLineBuffer() ;
        stringEsc(sb, s, singleLineString, asciiOnly) ;
        return sb.toString() ;
    }

    public static void stringEsc(AWriter out, String s, boolean singleLineString, boolean asciiOnly)
    {
        int len = s.length() ;
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);

            // \\ Escape always possible.
            if (c == '\\') 
            {
                out.print('\\') ;
                out.print(c) ;
                continue ;
            }
            if ( singleLineString )
            {
                if ( c == '"' )         { out.print("\\\""); continue ; }
                else if (c == '\n')     { out.print("\\n");  continue ; }
                else if (c == '\t')     { out.print("\\t");  continue ; }
                else if (c == '\r')     { out.print("\\r");  continue ; }
                else if (c == '\f')     { out.print("\\f");  continue ; }
            }
            // Not \-style esacpe. 
            if ( c >= 32 && c < 127 )
                out.print(c);
            else if ( !asciiOnly )
                out.print(c);
            else
            {
                // Outside the charset range.
                // Does not cover beyond 16 bits codepoints directly
                // (i.e. \U escapes) but Java keeps these as surrogate
                // pairs and will print as characters
                out.print( "\\u") ;
                OutputUtils.printHex(out, c, 4) ;
            }
        }
    }

    // Utilities to remove escapes

    /** Replace \ escapes (\\u, \t, \n etc) in a string */
    public static String unescapeStr(String s)
    { return unescapeStr(s, '\\') ; }
    
    /** Replace \ escapes (\\u, \t, \n etc) in a string */
    public static String unescapeStr(String s, char escapeChar)
    { return unescape(s, escapeChar, false) ; }

    // Main worker function for unescaping strings.
    public static String unescape(String s, char escape, boolean pointCodeOnly) {
        int i = s.indexOf(escape) ;
        
        if ( i == -1 )
            return s ;
        
        // Dump the initial part straight into the string buffer
        StringBuilder sb = new StringBuilder(s.substring(0,i)) ;
        
        for ( ; i < s.length() ; i++ )
        {
            char ch = s.charAt(i) ;

            if ( ch != escape )
            {
                sb.append(ch) ;
                continue ;
            }
                
            // Escape
            if ( i >= s.length()-1 )
                throw new AtlasException("Illegal escape at end of string") ;
            char ch2 = s.charAt(i+1) ;
            i = i + 1 ;
            
            // \\u and \\U
            if ( ch2 == 'u' )
            {
                // i points to the \ so i+6 is next character
                if ( i+4 >= s.length() )
                    throw new AtlasException("\\u escape too short") ;
                int x = hex(s, i+1, 4) ;
                sb.append((char)x) ;
                // Jump 1 2 3 4 -- already skipped \ and u
                i = i+4 ;
                continue ;
            }
            if ( ch2 == 'U' )
            {
                // i points to the \ so i+6 is next character
                if ( i+8 >= s.length() )
                    throw new AtlasException("\\U escape too short") ;
                int x = hex(s, i+1, 8) ;
                // Convert to UTF-16 codepoint pair.
                sb.append((char)x) ;
                // Jump 1 2 3 4 5 6 7 8 -- already skipped \ and u
                i = i+8 ;
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
                    throw new AtlasException("Unknown escape: \\"+ch2) ;
            }
            sb.append(ch3) ;
        }
        return sb.toString() ;
    }
    
    public static int hex(String s, int i, int len)
    {
//        if ( i+len >= s.length() )
//        {
//            
//        }
        int x = 0 ;
        for ( int j = i ; j < i+len ; j++ )
        {
           char ch = s.charAt(j) ;
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
                   throw new AtlasException("Illegal hex escape: "+ch) ;
           }
           x = (x<<4)+k ;
        }
        return x ;
    }

}
