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

package org.apache.jena.graph.langtag;

import static org.apache.jena.atlas.lib.Lib.lowercase;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.graph.Node;

/** Functions on language tag strings */
public class LangTags {

    /**
     * Format language tag.
     * This is the system-wide policy for formatting language tags.
     */
    public static String formatLangtag(String input) {
        if ( input == null )
            return Node.noLangTag;
        if ( input.isEmpty() )
            return input;
        return basicFormat(input);
    }

//    /**
//     * Language tag formatter.
//     * <a href="https://datatracker.ietf.org/doc/html/rfc5646#section-2.1.1">RFC 5646 section 2.1.1</a>
//     */
//    public static String formatRFC5646(String string) {
//        return basicFormat(string);
//    }

    /**
     * Format an language tag assumed to be valid.
     * This code only deals with langtags by the string length of the subtags.
     * <a href="https://datatracker.ietf.org/doc/html/rfc5646#section-2.1.1">RFC 5646 section 2.1.1</a>
     */
    public static String basicFormat(String string) {
        // with the interpretation that "after singleton" means anywhere after the singleton.
        if ( string == null )
            return null;
        if ( string.isEmpty() )
            return string;
        List<String> strings = splitOnDash(string);
        if ( strings == null ) {
            return lowercase(string);
            //error("Bad language string: %s", string);
        }
        StringBuilder sb = new StringBuilder(string.length());
        boolean singleton = false;
        boolean first = true;

        for ( String s : strings ) {
            if ( first ) {
                // language
                sb.append(lowercase(s));
                first = false;
                continue;
            }
            first = false;
            // All subtags after language
            sb.append('-');
            if ( singleton )
                // Always lowercase
                sb.append(lowercase(s));
            else {
                // case depends on ;length
                sb.append(strcase(s));
                // is it the start of an extension or privateuse
                // XXX s.length()==1?
                if ( s.length() == 1 )
                    singleton = true;
            }
        }
        return sb.toString();
    }

    private static List<String> splitOnDash(String x) {
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
            // Ends in "-"
            return null;
            //error("Empty part: %s", x);
        }
        strings.add(strLast);
        return strings;
    }

    private static void error(String msg, Object ... args) {
        String x = String.format(msg, args);
        //throw new LangTagException(x);
        Log.warn("LangTag", x);
    }

    private static String strcase(String string) {
        if ( string == null )
            return null;
        if ( string.length() == 2 )
            return Lib.uppercase(string);
        if ( string.length() == 4 )
            return titlecase(string);
        return lowercase(string);
    }

    private static String titlecase(String string) {
        if ( string == null )
            return null;
        char ch1 = string.charAt(0);
        ch1 = Character.toUpperCase(ch1);
        string = lowercase(string.substring(1));
        return ch1 + string;
    }

    private static String str(char ch) {
        return String.format("'%s' U+%04X", Character.valueOf(ch), (int)ch);
    }

    /** ASCII A-Z */
    /*package*/ static boolean isA2Z(int ch) {
        return range(ch, 'a', 'z') || range(ch, 'A', 'Z');
    }

    /** ASCII A-Z or 0-9 */
    /*package*/ static boolean isA2ZN(int ch) {
        return range(ch, 'a', 'z') || range(ch, 'A', 'Z') || range(ch, '0', '9');
    }

    private static boolean range(int ch, char a, char b) {
        return (ch >= a && ch <= b);
    }
}
