/**
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

package org.apache.jena.http.auth;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for authentication header strings. More forgiving than necessary.
 */
class AuthStringTokenizer {

    // Terms:
    // "quoted string"
    // delimiters( , or =)
    // an unquoted string, no spaces.
    private static String regex = "(\"[^\"=]*\"|,|=|[^=, \"]+)";
    private static Pattern pattern = Pattern.compile(regex);
    private static String nullString = "";

    static Map<String, String> parse(String string) {
        try {
            return parse$(string);
        } catch (AuthStringException ex) {
            return null;
        }
    }

    private static Map<String, String> parse$(String string) {
        // Phase one - split into tokens.
        List<String> tokens = tokenize(string);

        Map<String, String> map = new HashMap<>();
        if ( !tokens.isEmpty() ) {
            String s = tokens.get(0);
            if ( "Digest".equalsIgnoreCase(s) )
                map.put(AuthChallenge.SCHEME, s);
            if ( "Basic".equalsIgnoreCase(s) )
                map.put(AuthChallenge.SCHEME, s);
        }

        // Phase two : assign to the map.
        String word1 = null;
        boolean seenEquals = false;
        for ( String s : tokens ) {
            if ( s == null )
                continue;
            if ( s.equals(",") ) {
                if ( word1 != null )
                    record(map, word1, null);
                word1 = null;
                continue;
            }

            if ( s.equals("=") ) {
                seenEquals = true;
                continue;
            }

            if ( word1 == null ) {
                if ( seenEquals )
                    // Two = =
                    throw new AuthStringException();
                word1 = s;
                continue;
            }

            // new word, word1 seen.
            // if ( word1 != null ) {
            if ( !seenEquals ) {
                record(map, word1, null);
                word1 = s;
            } else {
                record(map, word1, s);
                word1 = null;
                seenEquals = false;
                continue;
            }
        }

        if ( word1 != null )
            record(map, word1, null);

        return map;

    }

    /** Tokenize. Quoted strings retain the "" */
    /* package */ static List<String> tokenize(String string) {
        List<String> list = new ArrayList<String>();
        Matcher m = pattern.matcher(string);
        while (m.find()) {
            // First non-null match.
            for ( int i = 1 ; i <= m.groupCount() ; i++ ) {
                if ( m.group(i) != null ) {
                    list.add(m.group(i));
                    break;
                }
            }
        }
        return list;
    }

    private static boolean isQuoted(String string) {
        return string.startsWith("\"") && string.endsWith("\"");
    }

    private static boolean maybeQuoted(String string) {
        return string.startsWith("\"") || string.endsWith("\"");
    }

    private static void record(Map<String, String> map, String word1, String word2) {
        if ( word1 == null || word1.isEmpty() || maybeQuoted(word1) )
            throw new AuthStringException();
        word1 = word1.toLowerCase();
        if ( word2 == null )
            word2 = nullString;
        else if ( isQuoted(word2) )
            word2 = word2.substring(1, word2.length() - 1);

        map.put(word1, word2);
    }
}
