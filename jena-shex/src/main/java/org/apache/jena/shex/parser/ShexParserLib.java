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

package org.apache.jena.shex.parser;

import org.apache.jena.atlas.AtlasException;
import org.apache.jena.atlas.lib.Hex;

class ShexParserLib {

    // Nearly the same as EscapeStr.unescape.
    // Shex regex allows /\{\}/ -- single \ for a regular expression metacharacter.

    /*
     * <REGEXP              :    <SLASH>
     *                       ( ~["/","\\","\n", "\r"]
     *                       | "\\" [ "n", "r", "t", "\\", "|", "." , "?", "*", "+",
     *                               "(", ")", "{", "}", "$", "-", "[", "]", "^", "/" ]
     *                       | <UCHAR>
     *                       )+ <SLASH> (["s","m","i","x"])*
     * which is the usual suspects, and metacharacters in XML Schema regular.
     *
     * The \ is preserved.
     *   "|", "." , "?", "*", "+",
     *   "(", ")", "{", "}", "$", "-", "[", "]", "^", "/"
     *
     */

    // Main worker function for unescaping strings.
    /*package*/static String unescapeShexRegex(String s, char escape, boolean pointCodeOnly) {
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

            char ch3 = 0 ;
            switch (ch2)
            {
                // true escapes.
                case 'n': ch3 = '\n' ;  break ;
                case 't': ch3 = '\t' ;  break ;
                case 'r': ch3 = '\r' ;  break ;
                case 'b': ch3 = '\b' ;  break ;
                case 'f': ch3 = '\f' ;  break ;
                case '\'': ch3 = '\'' ; break ;
                case '\"': ch3 = '\"' ; break ;

                case '/': case '-':
                    ch3 = ch2;
                    break ;

                // Regex metacharacters
                case '\\':
                case  '|': case '.' : case '?': case '*': case '+':
                case '(': case ')': case '{': case '}': case '$':
                case '[': case ']': case '^':
                    sb.append('\\');
                    ch3 = ch2;
                    break;
                default:
                    throw new AtlasException("Unknown escape: \\"+ch2) ;
            }
            sb.append(ch3) ;
        }
        return sb.toString() ;
    }
}
