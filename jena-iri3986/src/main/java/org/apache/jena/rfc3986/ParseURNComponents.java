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

package org.apache.jena.rfc3986;

import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse URN components.
 *
 * <pre>
 * [ "?+" r-component ] [ "?=" q-component ] [ "#" f-component ]
 * </pre>
 * @implNote
 * Validation is a single pass over the string.
 * Parsing is a single pass over the string, followed by extracting slices of the string.
 */
/*package*/ class ParseURNComponents {

    /*
     * namestring    = assigned-name
     *                  [ rq-components ]
     *                  [ "#" f-component ]
     * rq-components = [ "?+" r-component ]
     *                 [ "?=" q-component ]
     * r-component   = pchar *( pchar / "/" / "?" )
     * q-component   = pchar *( pchar / "/" / "?" )
     * f-component   = fragment
     */
    static class URNComponentException extends RuntimeException {
        URNComponentException(String msg) { super(msg); }
        @Override public URNComponentException fillInStackTrace() { return this; }
    }

    public static final char CH_QMARK    = '?' ;
    public static final char CH_HASH     = '#' ;
    public static final char CH_EQUALS   = '=' ;
    public static final char CH_PLUS     = '+' ;

    public static final String R_STR     = "?+";
    public static final String Q_STR     = "?=";
    public static final String F_STR     = "#";
    public static final char   F_CH      = CH_HASH;

    /**
     * Parse by URI components. Assume the rqString is a valid query string (no leading '?')
     * and fString is a fragment (no leading #).
     * <p>
     */
    public static URNComponents parseURNComponents(IRI iri) {
        String rqString = iri.query();
        String fString = iri.fragment();

        if ( rqString == null && fString == null )
            return null;
        if ( rqString == null || rqString.isEmpty() ) {
            if ( fString == null )
                return null;
            else
                return createValidURNComponents(null, null, fString);
        }

        BiConsumer<Issue, String> handler = (issue, msg) -> new URNComponentException(msg);
        int N = rqString.length();
        int x = 0;
        int rCompStart = -1;
        int rCompFinish = -1;
        String rComponent = null;
        int qCompStart = -1;
        int qCompFinish = -1;
        String qComponent = null;
        String fComponent = fString;

        // No leading ?
        // Check for '+'
        if ( lookingAtChar1(rqString, 0, N, CH_PLUS) ) {
            // r-component
            rCompStart = 1;
            for( ; x < N ; x++ ) {
                char ch = rqString.charAt(x);
                if ( ch == CH_QMARK ) {
                    // q-component?
                    if ( lookingAtChar1(rqString, x+1, N, CH_EQUALS) ) {
                        break;
                    }
                    // '?' but not ?=
                }
            }
            rCompFinish = x;
            if ( rCompFinish - rCompStart < 1 ) {
                handler.accept(Issue.urn_bad_components, "URN r-component must be at least one character");
                return null;
            }
            rComponent = rqString.substring(rCompStart, rCompFinish);
            if ( x < N )
                // Move over '?'
                x++;
        }

        // if not end of string, and exited on '?'
        if ( x < N ) {
            if ( lookingAtChar1(rqString, x, N, CH_EQUALS) ) {
                // r-component
                qCompStart = x+1;
                qCompFinish = N;
                if ( qCompFinish - qCompStart < 1 ) {
                    handler.accept(Issue.urn_bad_components, "URN r-component must be at least one character");
                    return null;
                }
                qComponent = rqString.substring(qCompStart, qCompFinish);
            } else {
                handler.accept(Issue.urn_bad_components, "Unexpected characters in query string");
                return null;
            }
        }
        return createValidURNComponents(rComponent, qComponent, fComponent);
    }

    /**
     * Check URN components.
     * <p>
     * The string is a full IRI query string and fragment, starting with '?', or '#' (if only an f-component is present)
     */
    public static boolean validateURNComponents(String string, int start, BiConsumer<Issue, String> handler) {
        int N = string.length();
        int x = start;
        if ( N == 0 )
            return true;

        boolean startIsQmark = lookingAtChar1(string, start, N, CH_QMARK);
        boolean startIsFrag = lookingAtChar1(string, start, N, CH_HASH);

        if ( !startIsQmark && ! startIsFrag ) {
            handler.accept(Issue.urn_bad_components, "Does not start with a r-, q- or f- component");
            return false;
        }

        if ( startIsQmark ) {
            x = findValidateR(string, x, handler);
            if ( x == -1 )
                // Handler called
                return false;
            if ( x == N )
                return true;
            x = findValidateQ(string, x, handler);
            if ( x == -1 )
                // Handler called
                return false;
            if ( x == N )
                return true;
            if ( x == start ) {
                handler.accept(Issue.urn_bad_components, "Query string does not start with an r- or q- component");
                return false;
            }
        }
        x = findValidateF(string, x, handler);
        if ( x == -1 )
            return false;
        return true;
    }

    private static BiConsumer<Issue, String> noopHandler = (issue, msg)->{};

    /**
     * Parse URN components in a string.
     * The string starts '?' or '#'.
     * Return URNComponents or null if in error.
     * @See {@link #parseURNComponents(String, int, BiConsumer)}
     */
    public static URNComponents parseURNComponents(String componentsString) {
        return parseURNComponents(componentsString, 0, noopHandler);
    }

    /**
     * Parse URN components from the end of a string. For example, find the "?" in a URN and parse out the components.
     */
    public static URNComponents parseURNComponents(String string, int start, BiConsumer<Issue, String> handler) {
        int N = string.length();
        if ( N == 0 )
            return null;

        boolean startIsQmark = lookingAtChar1(string, start, N, CH_QMARK);
        boolean startIsFrag = lookingAtChar1(string, start, N, CH_HASH);

        if ( !startIsQmark && ! startIsFrag ) {
            handler.accept(Issue.urn_bad_components, "Does not start with an r-, q- or f- component");
            return null;
        }

        int rCompStart = -1 ;
        int rCompFinish = -1 ;
        int qCompStart = -1 ;
        int qCompFinish = -1 ;
        int fCompStart = -1 ;
        int fCompFinish = -1 ;

        int x = start;
        int xEnd = -1 ;   // Index ahead.

        if ( startIsQmark ) {

            xEnd = findValidateR(string, x, handler);
            if ( xEnd == -1 )
                // Handler called
                return null;
            if ( xEnd != x) {
                rCompStart = x+2;
                rCompFinish = xEnd;
            }
            // Move on.
            x = xEnd;

            xEnd = findValidateQ(string, x, handler);
            if ( xEnd == -1 )
                // Handler called
                return null;
            if ( xEnd == start ) {
                handler.accept(Issue.urn_bad_components, "Query string not start with an r- or q- component");
                return null;
            }

            if ( xEnd != x) {
                qCompStart = x+2;
                qCompFinish = xEnd;
            }
            x = xEnd;
        }
        xEnd = findValidateF(string, x, handler);
        if ( xEnd == -1 )
            return null;
        if ( xEnd != N )
            return null;
        if ( xEnd != x ) {
            fCompStart = x+1;   // Skip '#'
            fCompFinish = xEnd;
        }

        String rComponent = slice(string, rCompStart, rCompFinish);
        String qComponent = slice(string, qCompStart, qCompFinish);
        String fComponent = slice(string, fCompStart, fCompFinish);
        if ( rComponent != null && rComponent.isEmpty() ) {
            handler.accept(Issue.urn_bad_components, "URN r-component must be at least one character");
            return null;
        }
        if ( qComponent != null && qComponent.isEmpty() ) {
            handler.accept(Issue.urn_bad_components, "URN q-component must be at least one character");
            return null;
        }
        if ( rComponent == null && qComponent == null && fComponent == null ) {
            handler.accept(Issue.urn_bad_components, "Bad URN components");
            return null;
        }
        return createValidURNComponents(rComponent, qComponent, fComponent);

    }

    private static String slice(String string, int start, int finish) {
        if ( start == -1 )
            return null;
        return string.substring(start, finish);
    }

    /**
     * Check any r-component.
     * <p>
     * Return the end (exclusive) of the r-component.
     * <br/>
     * Return the start index if none found.
     * <br/>
     * Return -1 if an error occurred.
     * <p>
     * Call {@code handler} to pass back scheme-specific violations. This is allowed to raise an exception.
     * Return -1 on error if the handler returned.
     * NB An r-component can contain '?'. We interpret this as "r-component ends at first q-component".
     */
    private static int findValidateR(String string, int start, BiConsumer<Issue, String> handler) {
        int N = string.length();
        boolean found = lookingAtChar2(string, start, N, CH_QMARK, CH_PLUS);
        if ( ! found )
            return start;
        // Skip to ?= or #
        int compStart = start+2;
        int x = compStart;
        char ch = 0;
        for( ; x < N ; x++ ) {
            ch = string.charAt(x);
            if ( ch == CH_HASH )
                break;
            if ( ch == CH_QMARK ) {
                if ( lookingAtChar1(string, x+1, N, CH_EQUALS) )
                    break;
                // '?' but not ?=
            }
        }
        int compFinish = x;
        if ( compStart == compFinish ) {
            handler.accept(Issue.urn_bad_components, "URN r-component must be at least one character");
            return -1;
        }
        return compFinish;
    }

    private static int findValidateQ(String string, int start, BiConsumer<Issue, String> handler) {
        int N = string.length();
        boolean found = lookingAtChar2(string, start, N, CH_QMARK, CH_EQUALS);
        if ( ! found )
            return start;
        // Skip to end of string or hash.
        int compStart = start+2;
        int x = compStart;
        char ch = 0;
        for( ; x < N ; x++ ) {
            ch = string.charAt(x);
            if ( ch == CH_HASH )
                break;
        }
        int compFinish = x;
        if ( compStart == compFinish ) {
            handler.accept(Issue.urn_bad_components, "URN q-component must be at least one character");
            return -1;
        }
        return compFinish;
    }

    /**
     * Check for an f-component at any place after start.
     * <p>
     * Return the end (exclusive) of the f-component.
     * <br/>
     * Return the start index if none found.
     * <br/>
     * Return -1 if an error occurred.
     * <p>
     * Call {@code handler} to pass back scheme-specific violations. This is allowed to raise an exception.
     * Return -1 on error if the handler returned.
     */
    private static int findValidateF(String string, int start, BiConsumer<Issue, String> handler) {
        int N = string.length();
        boolean found = lookingAtChar1(string, start, N, CH_HASH);
        if ( ! found )
            return start;
        // Runs from # to end of string.
        return N;
    }

    // Are we looking at a character?
    private static boolean lookingAtChar1(String string, int index, int N, char ch1) {
        int idx = index;
        if ( idx >= N )
            return false;
        char x1 = string.charAt(idx);
        if ( x1 != 0 && x1 != ch1 )
                return false;
        return true;
    }

    // Are we looking at one character followed by another?
    private static boolean lookingAtChar2(String string, int index, int N, char ch1, char ch2) {
        return lookingAtChar1(string, index, N, ch1) &&
               lookingAtChar1(string, index+1, N, ch2);
    }


    private static URNComponents createValidURNComponents(String rComp, String qComp, String fComp) {
        if ( rComp != null && rComp.isEmpty() )
            return null;
        if ( qComp != null && qComp.isEmpty() )
            return null;
        return new URNComponents(rComp, qComp, fComp);
    }

    // --- By regex

    private static int AnyIChar = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS | Pattern.UNICODE_CASE;
    private static int AnyASCIIChar = Pattern.CASE_INSENSITIVE;
    // Non-collecting (?: ... ) is a non-binding regex group
    private static Pattern pattern = Pattern.compile("^(?:\\?\\+[0-9a-z]+)?(?:\\?=[0-9a-z]+)?(?:#[0-9a-z]*)?$", AnyIChar);

    // Collecting - the group is the inner component part without the marker ?+,?=, #
    // The r-compoent can contain an r-component
    private static Pattern patternComponents = Pattern.compile("^(?:\\?\\+([?+0-9a-z]+))?(?:\\?=([?+=0-9a-z]+))?(?:#(.*))?$", AnyIChar);

    static URNComponents parseURNcomponentsRegex(String componentsString) {
        Matcher m = patternComponents.matcher(componentsString);
        if ( ! m.matches() )
            return null;
        String r = m.group(1);
        String q = m.group(2);
        String f = m.group(3);
        return createValidURNComponents(r, q, f);
    }


}
