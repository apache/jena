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

package org.apache.jena.riot.lang.extra;

import org.apache.jena.atlas.AtlasException;
import org.apache.jena.atlas.lib.EscapeStr;
import org.apache.jena.riot.RiotParseException;

/** Support function for language parsing. */
public class LangParserLib {

    /** Remove the first n characters from the string */
    public static String stripChars(String s, int n) {
        return s.substring(n, s.length()) ;
    }

    /** Remove first and last characters (e.g. ' or "") from a string */
    public static String stripQuotes(String s) {
        return s.substring(1, s.length() - 1) ;
    }

    /** Remove first 3 and last 3 characters (e.g. ''' or """) from a string */
    public static String stripQuotes3(String s) {
        return s.substring(3, s.length() - 3) ;
    }

    /** Unescape \t, \n etc.*/
    public static String unescapeStr(String s, int line, int column)
    { return unescape(s, '\\', false, line, column) ; }

    // Worker function
    private static String unescape(String s, char escape, boolean pointCodeOnly, int line, int column) {
        try {
            return EscapeStr.unescape(s, escape, pointCodeOnly) ;
        } catch (AtlasException ex) {
            throw new RiotParseException(ex.getMessage(), line, column) ;
        }
    }

    /** Unescape \t, \n etc. and also  unicode \ u and \U */
    public static String unescapeUnicode(String s, int line, int column) {
        return unescape(s, '\\', true, line, column);
    }

    /** Unescape a prefix name (or part of). This applies the Turtle/SPARQL PLX rule */
    public static String unescapePName(String s, int line, int column) {
        char escape = '\\' ;
        int idx = s.indexOf(escape) ;

        if ( idx == -1 )
            return s ;

        int len = s.length() ;
        StringBuilder sb = new StringBuilder() ;

        for ( int i = 0 ; i < len ; i++ ) {
            char ch = s.charAt(i) ;
            // Keep line and column numbers.
            switch (ch) {
                case '\n' :
                case '\r' :
                    line++ ;
                    column = 1 ;
                    break ;
                default :
                    column++ ;
                    break ;
            }

            if ( ch != escape ) {
                sb.append(ch) ;
                continue ;
            }

            // Escape
            if ( i >= s.length() - 1 )
                throwParseException("Illegal escape at end of string", line, column) ;
            char ch2 = s.charAt(i + 1) ;
            column = column + 1 ;
            i = i + 1 ;

            switch (ch2) {   // PN_LOCAL_ESC
                case '_' :
                case '~' :
                case '.' :
                case '-' :
                case '!' :
                case '$' :
                case '&' :
                case '\'' :
                case '(' :
                case ')' :
                case '*' :
                case '+' :
                case ',' :
                case ';' :
                case '=' :
                case ':' :
                case '/' :
                case '?' :
                case '#' :
                case '@' :
                case '%' :
                    sb.append(ch2) ;
                    break ;
                default :
                    throwParseException("Illegal prefix name escape: " + ch2, line, column) ;
            }
        }
        return sb.toString() ;
    }

    public static void throwParseException(String msg, int line, int column) {
        throw new RiotParseException("Line " + line + ", column " + column + ": " + msg, line, column) ;
    }

    public static void throwParseException(String msg) {
        throw new RiotParseException(msg, -1, -1) ;
    }
}
