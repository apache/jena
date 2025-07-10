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

package org.apache.jena.langtag;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * LangTag processing support.
 */
class InternalLangTag {

    static List<String> splitOnDash(String x) {
        List<String> strings = new ArrayList<>(6);
        // Split efficiently(?) based on [a-z][A-Z][0-9] units separated by "-"s
        StringBuilder sb = new StringBuilder();

        boolean start = true;
        for ( int idx = 0; idx < x.length(); idx++ ) {
            char ch = x.charAt(idx);
            if ( isA2ZN(ch) ) {
                sb.append(ch);
                continue;
            }
            if ( ch == '-' ) {
                String str = sb.toString();
                strings.add(str);
                sb.setLength(0);
                continue;
            }
            error("Bad character: (0x%02X) '%c' index %d", (int)ch, str(ch), idx);
        }
        String strLast = sb.toString();
        if ( strLast.isEmpty() ) {
            return null;
            //throw new LangTagException("Empty part: "+x);
        }
        strings.add(strLast);
        return strings;
    }

    /*package*/ static String strcase(String string) {
        if ( string == null )
            return null;
        if ( string.length() == 2 )
            return uppercase(string);
        if ( string.length() == 4 )
            return titlecase(string);
        return lowercase(string);
    }

    /*package*/static String lowercase(String string) {
        if ( string == null )
            return null;
        return string.toLowerCase(Locale.ROOT);
    }

    /*package*/static String uppercase(String string) {
        if ( string == null )
            return null;
        return string.toUpperCase(Locale.ROOT);
    }

    /*package*/static String titlecase(String string) {
        if ( string == null )
            return null;
        char ch1 = string.charAt(0);
        ch1 = Character.toUpperCase(ch1);
        string = lowercase(string.substring(1));
        return ch1 + string;
    }

    /** ASCII A-Z */
    /*package*/ static boolean isA2Z(int ch) {
        return range(ch, 'a', 'z') || range(ch, 'A', 'Z');
    }

    /** ASCII A-Z or 0-9 */
    /*package*/ static boolean isA2ZN(int ch) {
        return range(ch, 'a', 'z') || range(ch, 'A', 'Z') || range(ch, '0', '9');
    }

    static void checkDigits(String string, int N, int start, int end) {
        for ( int i = start ; i < end ; i++ ) {
            char ch = string.charAt(i);
            if ( ! isNum(ch) )
                error("Not a DIGIT (%s, posn = %s) in '%s'", str(ch), (i+1), string);
        }
    }

    static void checkAlpha(String string, int N, int start, int end) {
        for ( int i = start ; i < end ; i++ ) {
            char ch = string.charAt(i);
            if ( ! isAlpha(ch) )
                // 1-based error message
                error("Not an ALPHA (%s, posn = %s) in '%s'", str(ch), (i+1), string);
        }
    }

    static boolean isAlpha(String string, int start, int end) {
        for ( int i = start ; i < end ; i++ ) {
            char ch = string.charAt(i);
            if ( ! isAlpha(ch) )
                return false;
        }
        return true;
    }

    static boolean isAlphaNum(String string, int start, int end) {
        for ( int i = start ; i < end ; i++ ) {
            char ch = string.charAt(i);
            if ( ! isAlphaNum(ch) )
                return false;
        }
        return true;
    }

    static void checkAlphaMinus(String string, int N, int start, int end) {
        for ( int i = start ; i < end ; i++ ) {
            char ch = string.charAt(i);
            if ( ! isAlpha(ch) && ! isMinus(ch) )
                error("Not an ALPHA or MINUS (%s, posn = %s) in '%s'", str(ch), (i+1), string);
        }
    }

    static void checkAlphaNum(String string, int N, int start, int end) {
        for ( int i = start ; i < end ; i++ ) {
            char ch = string.charAt(i);
            if ( ! isAlpha(ch) && ! isNum(ch) )
                error("Not an ALPHA or DIGITS (%s, posn = %s) in '%s'", str(ch), (i+1), string);
        }
    }

    static void checkAlphaNumMinus(String string, int N, int start, int end) {
        for ( int i = start ; i < end ; i++ ) {
            char ch = string.charAt(i);
            if ( ! isAlpha(ch) && ! isNum(ch) && ! isMinus(ch) )
                error("Not an ALPHA, DIGITS or MINUS (%s, posn = %s) in '%s'", str(ch), (i+1), string);
        }
    }

    static String str(char ch) {
        return String.format("'%s' U+%04X", Character.valueOf(ch), (int)ch);
    }

    static boolean isAlpha(char ch) {
        return ( ch >= 'a' && ch <= 'z' ) || ( ch >= 'A' && ch <= 'Z' );
    }

    static boolean isNum(char ch) {
        return ( ch >= '0' && ch <= '9' );
    }

    static boolean isAlphaNum(char ch) {
        return isAlpha(ch) || isNum(ch);
    }

    static boolean isMinus(char ch) {
        return ( ch == '-' );
    }

    /*package*/ static void error(String msg, Object...args) {
        String x = String.format(msg, args);
        throw new LangTagException(x);
    }

    private static boolean range(int ch, char a, char b) {
        return (ch >= a && ch <= b);
    }

    /** Case insensitive test of whether a string has a prefix. */
    static boolean caseInsensitivePrefix(String string, String prefix) {
        return string.regionMatches(true, 0, prefix, 0, prefix.length());
    }
}
