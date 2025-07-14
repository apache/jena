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

import static org.apache.jena.rfc3986.Chars3986.EOF;
import static org.apache.jena.rfc3986.Chars3986.charAt;
import static org.apache.jena.rfc3986.LibParseIRI.caseInsensitivePrefix;

import java.util.function.BiConsumer;

/**
 * Validate and parse URNs.
 *
 * @implNote
 * Validation is a single pass over the string.
 * Parsing is a single pass over the string, followed by extracting slices of the string.
 */

public class ParseURN {
    // RFC 8141
    // @formatter:off
    /*
     * namestring    = assigned-name
     *                  [ rq-components ]
     *                  [ "#" f-component ]
     * assigned-name = "urn" ":" NID ":" NSS
     * NID           = (alphanum) 0*30(ldh) (alphanum)
     * ldh           = alphanum / "-"
     * NSS           = pchar *(pchar / "/")
     * rq-components = [ "?+" r-component ]
     *                 [ "?=" q-component ]
     * r-component   = pchar *( pchar / "/" / "?" )
     * q-component   = pchar *( pchar / "/" / "?" )
     * f-component   = fragment
     */
    /*
     *  InformalNamespaceName = "urn-" Number
     *  Number                = DigitNonZero 0*Digit
     *  DigitNonZero          = "1"/ "2" / "3" / "4"/ "5"
     *                        / "6" / "7" / "8" / "9"
     *  Digit                 = "0" / DigitNonZero
     */
    /*
     * alphanum, fragment, and pchar from RFC 3986
     *
    // pchar         = unreserved / pct-encoded / sub-delims / ":" / "@"
       alphanum        ALPHA / DIGIT
       fragment    = *( pchar / "/" / "?" )

       pct-encoded   = "%" HEXDIG HEXDIG
       unreserved    = ALPHA / DIGIT / "-" / "." / "_" / "~"
       reserved      = gen-delims / sub-delims
       gen-delims    = ":" / "/" / "?" / "#" / "[" / "]" / "@"
       sub-delims    = "!" / "$" / "&" / "'" / "(" / ")"
     */
    /*
     * The NID rules:
     * + At last one character
     * Disallow "X-"
     * "urn-" which are not informal names: "urn-NNN"
     */
    // @formatter:on

    // For use by handlers.
    public static class URNParseException extends IRIParseException {
        URNParseException(String entity, String msg) { super(entity, msg); }
    }

    private static BiConsumer<Issue, String> noOpHandler = (x,y)->{};

    /**
     * Parse a string a a URN, Return null for invalid.
     */
    public static URN parseURN(String string) {
        return parseURN(string, noOpHandler);
    }

    /**
     * Parse a URN, including URN components.
     * Return a {@link URN}.
     * Call {@code handler} to pass back scheme-specific violations. This is allowed to raise an exception.
     * Return null on error if the handler returned.
     */
    public static URN parseURN(String string, BiConsumer<Issue, String> handler) {
        int N = string.length();

        int startNamespace = findValidURNScheme(string, handler);
        if ( startNamespace == -1 )
            return null;

        int finishNamespace = findValidNamespace(string, startNamespace, handler);
        if ( finishNamespace == -1 ) {
            // Handler already called.
            //handler.accept(Issue.urn_bad_nid, "Failed find the URN scheme name");
            return null;
        }
        String scheme = string.substring(0, 3);
        int startNSS = finishNamespace+1;
        int finishNSS = findValidNamespaceSpecificString(string, startNSS, handler);
        if ( finishNSS == -1 )
            return null;

        URNComponents components = null;
        if ( finishNSS < N ) {
            components = ParseURNComponents.parseURNComponents(string, finishNSS, handler);
            if ( components == null )
                return null;
        }

        String namespace = string.substring(startNamespace, finishNamespace);
        String nsSpecific = string.substring(startNSS, finishNSS);
        return new URN(scheme, namespace, nsSpecific, components);
    }

    /**
     *  Parse the URN components of an IRI (query string and fragment)
     */
    public static URNComponents parseURNComponents(IRI iri) {
        return ParseURNComponents.parseURNComponents(iri);
    }

    // Excluding components.
    /**
     * Validate a URN, NID and NSS, <em>excluding</em> URN components.
     * Return the index (exclusive) of the end of NSS.
     * Call {@code handler} to pass back scheme-specific violations. This is allowed to raise an exception.
     */
    public static void validateURN(String string, BiConsumer<Issue, String> handler) {
        int endNSS = validateAssignedName(string, handler);
        if ( endNSS == -1 ) {
            // Find end of NSS manually.
            int idx = string.indexOf(ParseURNComponents.CH_QMARK);
            if ( idx == -1 )
                idx = string.indexOf(ParseURNComponents.CH_HASH);
            if ( idx == -1 )
                return;
            endNSS = idx;
        }
        if ( endNSS == string.length() )
            return;
        ParseURNComponents.validateURNComponents(string, endNSS, handler);
    }

    /**
     * Validate a URN, NID and NSS, <em>excluding</em> URN components.
     * Return the index (exclusive) of the end of NSS.
     * Call {@code handler} to pass back scheme-specific violations. This is allowed to raise an exception.
     */
    public static int validateAssignedName(String string, BiConsumer<Issue, String> handler) {
        int N = string.length();

        int startNID = findValidURNScheme(string, handler);
        if ( startNID == -1 )
            return -1;

        int finishNID = findValidNamespace(string, startNID, handler);
        if ( finishNID == -1 )
            return -1;
        int startNSS = finishNID+1;

        int finishNSS = findValidNamespaceSpecificString(string, startNSS, handler);
        if ( finishNSS == -1 )
            return -1;
        return finishNSS;
    }

    /**
     * Check the scheme.
     * Return the start of the namespace specific string or -1 if the schema or namespace is invalid.
     * Call {@code handler} to pass back scheme-specific violations. This is allowed to raise an exception.
     * Return -1 on error if the handler returned.
     */
    private static int findValidURNScheme(String string, BiConsumer<Issue, String> handler) {
        //findValidURNScheme()
        // Avoid creating intermediate objects.
        final String urnSchemeStr = "urn:";
        boolean urnScheme = caseInsensitivePrefix(string, urnSchemeStr);
        if ( ! urnScheme ) {
            handler.accept(Issue.urn_bad_pattern, "Failed find the URN scheme name");
            return -1;
        }
        return urnSchemeStr.length();
    }

    /**
     * Check the namespace identifier of an IRI string assumed to be valid RFC 3986 syntax.
     * Return the start of the namespace specific string at ":".
     * (Therefore, the NID is characters 4 ("urn:") to return int-1.)
     * Call {@code handler} to pass back scheme-specific violations. This is allowed to raise an exception.
     * Return -1 on error if the handler returned.
     */
    private static int findValidNamespace(String string, int startNamespace, BiConsumer<Issue, String> handler) {
        int N = string.length();
        // Start of namespace id
        int x = startNamespace;
        // First character, alpha.
        char ch = charAt(string, x);
        if ( ch == EOF ) {
            handler.accept(Issue.urn_bad_nid, "No namespace id");
            return -1;
        }
        if ( ch == ':' ) {
            handler.accept(Issue.urn_bad_nid, "Missing namespace id");
            return -1;
        }
        if ( ! Chars3986.isAlphaNum(ch) ) {
            handler.accept(Issue.urn_bad_nid, "Namespace id does not start with an alphabetic ASCII character");
            return -1;
        }
        x++;
        char prevChar = EOF;
        // Scan for the terminating ":"
        while(x < N ) {
            prevChar = ch;
            ch = charAt(string, x);
            if ( ch == ':' ) {
                if ( prevChar == '-' ) {
                    // Can't end in hyphen
                    handler.accept(Issue.urn_bad_nid, "Namespace id ends in '-'");
                    return -1;
                }
                break;
            }
            if ( ! isLDH(ch) ) {
                handler.accept(Issue.urn_bad_nid, "Bad character in Namespace id");
                return -1;
            }
            x++;
            if ( x-startNamespace > 32 ) {
                handler.accept(Issue.urn_bad_nid, "Namespace id more than 32 characters");
                return -1;
            }
        }

        int finishNamespace = x;
        if ( ch != ':' ) {
            handler.accept(Issue.urn_bad_nid, "Namespace not terminated by ':'");
            return -1;
        }
        x++;

        if ( finishNamespace-startNamespace < 2 ) {
            handler.accept(Issue.urn_bad_nid, "Namespace id must be at least 2 characters");
            return -1;
        }

        // RFC 8141 section 5.1 (described in RFC 3406)
        if ( LibParseIRI.caseInsensitiveRegion(string, startNamespace, "X-") ) {
            String start = string.substring(startNamespace,2+startNamespace);
            handler.accept(Issue.urn_x_namespace, "Namespace id starts with '"+start+"'");
            return -1;
        }

        // RFC 8141 section 5.2 - Informal namespace. "urn-1234"
        if ( LibParseIRI.caseInsensitiveRegion(string, startNamespace, "urn-") ) {
            boolean seenNonZero = false;
            for ( int i = startNamespace+"urn-".length() ; i < finishNamespace ; i++ ) {
                char chx = charAt(string, i);
                if ( !seenNonZero ) {
                    if ( chx == '0' ) {
                        handler.accept(Issue.urn_bad_nid, "Leading zero in an informal namespace");
                        return -1;
                    } else
                        seenNonZero = true;
                }
                // Allows leading zeros.
                if ( ! Chars3986.isDigit(chx) ) {
                    handler.accept(Issue.urn_bad_nid, "Bad informal namepsace");
                    return -1;
                }
            }
        }
        return finishNamespace;
    }

    /**
     * Find the NSS - Namespace Specific String - starting at 'start', the index after the ':' ending the NID.
     * The NSS ends at end of string or at '?' or '#' if there are URN components,
     * Return the index of the end of the NSS (exclusive).
     * Call {@code handler} to pass back scheme-specific violations. This is allowed to raise an exception.
     * Return -1 on error if the handler returned.
     */
    private static int findValidNamespaceSpecificString(String string, int startNSS, BiConsumer<Issue, String> handler) {
        int idx = startNSS;
        int N = string.length();
        int finishNSS = N;
        for ( ; idx < N ; idx++ ) {
            char ch = string.charAt(idx);
            if ( ch == '?' || ch == '#' ) {
                finishNSS = idx;
                break;
            }
        }

        if ( finishNSS-startNSS < 1) {
            handler.accept(Issue.urn_bad_nss, "Namespace specific string must be at least one character");
            return -1;
        }
        return finishNSS;
    }

    // LDH = letter-digit-hyphen
    private static boolean isLDH(char ch) {
        return Chars3986.isAlphaNum(ch) || ch == '-';
    }

//    public static void parseRegex(String string) {
//      boolean urnMatches = URN_REGEX.matcher(string).matches();
//    }
}
