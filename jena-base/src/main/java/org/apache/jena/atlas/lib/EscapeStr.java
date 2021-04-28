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
import org.apache.jena.atlas.io.OutputUtils ;
import org.apache.jena.atlas.io.StringWriterI ;

/** String escape utilities */
public class EscapeStr
{
    /*
     * Escape characters in a string according to Turtle rules.
     */
    public static String stringEsc(String s) {
        AWriter w = new StringWriterI() ;
        stringEsc(w, s, Chars.CH_QUOTE2, true, CharSpace.UTF8) ;
        return w.toString() ;
    }

    /** Write a string - basic escaping, no quote escaping. */
    public static void stringEsc(AWriter out, String s, boolean asciiOnly) {
        int len = s.length() ;
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            // \\ Escape always possible.
            if (c == '\\') {
                out.print('\\') ;
                out.print(c) ;
                continue ;
            }
            switch(c) {
                case '\n':  out.print("\\n"); continue;
                case '\t':  out.print("\\t"); continue;
                case '\r':  out.print("\\r"); continue;
                case '\f':  out.print("\\f"); continue;
                default:    // Drop through
            }
            if ( !asciiOnly )
                out.print(c);
            else
                writeCharAsASCII(out, c) ;
        }
    }

    public static void stringEsc(AWriter out, String s, char quoteChar, boolean singleLineString) {
        stringEsc(out, s, quoteChar, singleLineString, CharSpace.UTF8);
    }

    public static void stringEsc(AWriter out, String s, char quoteChar, boolean singleLineString, CharSpace charSpace) {
        boolean ascii = ( CharSpace.ASCII == charSpace ) ;
        int len = s.length() ;
        int quotesInARow = 0 ;
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            // \\ Escape always possible.
            if (c == '\\') {
                out.print('\\') ;
                out.print(c) ;
                continue ;
            }
            if ( ! singleLineString ) {
                // Multiline string.
                if ( c == quoteChar ) {
                    quotesInARow++ ;
                    if ( quotesInARow == 3 ) {
                        out.print("\\");
                        out.print(quoteChar);
                        quotesInARow = 0;
                        continue;
                    }
                } else {
                    quotesInARow = 0 ;
                }
            } else {
                if ( c == quoteChar ) {
                    out.print("\\"); out.print(c) ; continue ;
                }
                switch(c) {
                    case '\n':  out.print("\\n"); continue;
                    case '\t':  out.print("\\t"); continue;
                    case '\r':  out.print("\\r"); continue;
                    case '\f':  out.print("\\f"); continue;
                    default:    // Drop through
                }
            }

            if ( !ascii )
                out.print(c);
            else
                writeCharAsASCII(out, c) ;
        }
    }

    /** Write a string with Unicode to ASCII conversion using \-u escapes */
    public static void writeASCII(AWriter out, String s) {
        int len = s.length() ;
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            writeCharAsASCII(out, c);
        }
    }

    /** Write a character with Unicode to ASCII conversion using \-u escapes */
    public static void writeCharAsASCII(AWriter out, char c) {
        if ( c >= 32 && c < 127 )
            out.print(c);
        else {
            // Outside the charset range.
            // Does not cover beyond 16 bits codepoints directly
            // (i.e. \U escapes) but Java keeps these as surrogate
            // pairs and will print as characters
            out.print("\\u") ;
            OutputUtils.printHex(out, c, 4) ;
        }
    }

    // Utilities to remove escapes

    /** Replace \ escapes (\\u, \t, \n etc) in a string */
    public static String unescapeStr(String s)
    { return unescapeStr(s, '\\') ; }

    /** Replace \ escapes (\\u, \t, \n etc) in a string */
    public static String unescapeStr(String s, char escapeChar)
    { return unescape(s, escapeChar, false) ; }


    /** Unicode escapes  \-u and \-U only */
    public static String unescapeUnicode(String s) {
        return  unescape(s, '\\', true) ;
    }

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
                if ( i+4 >= s.length() )
                    throw new AtlasException("\\u escape too short") ;
                int x4 = Hex.hexStringToInt(s, i+1, 4) ;
                sb.append((char)x4) ;
                // Jump 1 2 3 4 -- already skipped \ and u
                i = i+4 ;
                continue ;
            }
            if ( ch2 == 'U' )
            {
                if ( i+8 >= s.length() )
                    throw new AtlasException("\\U escape too short") ;
                int ch8 = Hex.hexStringToInt(s, i+1, 8) ;
                if ( Character.charCount(ch8) == 1 )
                    sb.append((char)ch8);
                else {
                    // See also TokenerText.insertCodepoint and TokenerText.readUnicodeEscape
                    // Convert to UTF-16. Note that the rest of any system this is used
                    // in must also respect codepoints and surrogate pairs.
                    if ( !Character.isDefined(ch8) && !Character.isSupplementaryCodePoint(ch8) )
                        throw new AtlasException(String.format("Illegal codepoint: 0x%04X", ch8));
                    if ( ch8 > Character.MAX_CODE_POINT )
                        throw new AtlasException(String.format("Illegal code point in \\U sequence value: 0x%08X", ch8));
                    char[] chars = Character.toChars(ch8);
                    sb.append(chars);
                }
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
}