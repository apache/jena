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

import static org.apache.jena.rfc3986.Chars3986.charAt;

import java.util.regex.Pattern;

public class ParseDID {

    private static char EOF = Chars3986.EOF ;

    /*
    ==== "did:" rules
    https://www.w3.org/TR/did-core/
    @context https://www.w3.org/ns/did/v1
    did:
    did                = "did:" method-name ":" method-specific-id
    method-name        = 1*method-char
    method-char        = %x61-7A / DIGIT     [x61 is 'a', x7A is 'z']
    method-specific-id = *( ":" *idchar ) 1*idchar
    idchar             = ALPHA / DIGIT / "." / "-" / "_" / pct-encoded
    pct-encoded        = "%" HEXDIG HEXDIG

    At least one char for method-name.
    At least one char for method-specific-id; end char not ":"

    April 2021: At-risk : add: empty  method-specific-id
*/
    private static final Pattern DID_PATTERN = Pattern.compile("^did:[a-z]+:(?:(?:[a-z0-9.-_]|%[0-9]A-Fa-f]{2})+)$");
    private static final int DIDStart = "did:".length();

    static
    public void parse(String string, boolean allowPercentEncoding) {
        if ( ! string.startsWith("did:") )
            error(string, "Not a DID");

        //int end = length;
        int end = string.length();
        int p = DIDStart;

        int q = methodName(string, p);
        if ( q <= p+1)
            error(string, "No method name");
        p = q ;

        q = methodSpecificId(string, p);
        if ( q <= p )
            error(string, "No method-specific-id");

        if ( q != end )
            error(string, "Trailing characters after method-specific-id");
    }

    private static int methodName(String str, int p) {
        //int end = length;
        int end = str.length();
        int start = p;
        while (p < end) {
            char ch = charAt(str, p);
            if ( ch == EOF ) // Internal error.
                return p;
            if ( ch == ':' ) {
                p++;
                return p;
            }
            // Special upper case for better error message?
            if ( ! methodChar(ch) ) {
                if ( uppercaseMethodChar(ch) )
                    error(str, "Uppercase character (not allowed in DID method name): '"+Character.toString(ch)+"'");
                else
                    error(str, "Bad character: '"+Character.toString(ch)+"'");
            }
            p++;
        }
        if ( p != end && start+1 == p )
            error(str, "Zero length methodName");
        // Still may be zero,
        return p;
    }

    private static int methodSpecificId(String str, int p) {
        //int end = length;
        int end = str.length();
        int start = p;
        while (p < end) {
            char ch = charAt(str, p);
            if ( ch == EOF ) {}
            if ( ch == ':' ) { p++; continue; }

            if ( ! idchar(ch, str, p) ) {
                error(str, "Bad character: '"+Character.toString(ch)+"'");
            }
            p++;
        }
        if ( p != end )
            error(str, "Traing charactser after method specific id");
        if ( start == p )
            error(str, "Zero length method specific id");
        char chLast = charAt(str, p-1);
        if ( chLast == ':' )
            error(str, "Final method specifc id character is a ':'");
        return p;
    }

    private static boolean methodChar(char ch) {
        return (ch >= 'a' && ch <= 'z');
    }

    private static boolean uppercaseMethodChar(char ch) {
        return (ch >= 'A' && ch <= 'Z');
    }

    private static boolean idchar(char ch, String str, int p) {
        return (ch >= 'a' && ch <= 'z') ||
               (ch >= 'A' && ch <= 'Z') ||
               (ch >= '0' && ch <= '9') ||
               ch == '.' || ch == '-' || ch == '_' ||
               Chars3986.isPctEncoded(ch, str, p);
    }

    static class DIDParseException extends IRIParseException {
        DIDParseException(String entity, String msg) { super(entity, msg); }
    }

    private static void error(String didString, String msg) {
        throw new DIDParseException(didString, msg);
    }
}
