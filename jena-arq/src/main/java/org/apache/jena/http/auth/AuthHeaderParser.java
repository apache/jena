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

import static org.apache.jena.atlas.lib.Chars.CH_RSLASH;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.IntPredicate;

import org.apache.jena.atlas.web.AuthScheme;
import org.apache.jena.riot.system.RiotChars;

/**
 * Parser for authentication header strings.
 * <ul>
 * <li>This parser is scheme-specific. e/.g. digest credentials can not be token68.
 * <li>This parser does not check auth-params names.
 * <li>auth-params have lower case key.
 * <ul>
 * Covers:
 * <ul>
 * <li>RFC 7617 - was RFC 2617 - basic authentication
 * <li>RFC 7616 - was RFC 2617 - digest authentication
 * <li>RFC 6750 - Bearer authentication
 * <li>"Unknown"
 * <ul>
 */
class AuthHeaderParser {
    /* RFC 7235 header:
     *    WWW-Authenticate:
     *    Authorization:
     *    Proxy-Authenticate:
     *    Proxy-Authorization:
     *
     *
     * Appendix C. Collected ABNF
     *    # is a comma-separate list
     *
     *
     * BWS = <BWS, see [RFC7230], Section 3.2.3>
     *
     * OWS = <OWS, see [RFC7230], Section 3.2.3>
     *
     * WWW-Authenticate = *( "," OWS ) challenge *( OWS "," [ OWS challenge ] )
     * Authorization = credentials
     * Proxy-Authenticate = *( "," OWS ) challenge *( OWS "," [ OWS challenge ] )
     * Proxy-Authorization = credentials
     *
     * challenge = auth-scheme [ 1*SP ( token68 / #auth-param ) ]
     *
     * credentials = auth-scheme [ 1*SP ( token68 / #auth-param ) ]
     *
     * auth-scheme = token
     *
     * auth-param = token BWS "=" BWS ( token / quoted-string )
     *
     * quoted-string = <quoted-string, see [RFC7230], Section 3.2.6>
     *
     * token = <token, see [RFC7230], Section 3.2.6>
     *      * ==> token = 1*tchar
     *  tchar = "!" / "#" / "$" / "%" / "&" / "'" / "*" / "+" /
     *          "-" / "." / "^" / "_" / "`" / "|" / "~" / DIGIT / ALPHA
     *
     * token68 = 1*( ALPHA / DIGIT / "-" / "." / "_" / "~" / "+" / "/" ) *"="
     *
     * quoted-string  = DQUOTE *( qdtext / quoted-pair ) DQUOTE
     *
     * qdtext         = HTAB / SP /%x21 / %x23-5B / %x5D-7E / obs-text
     *
     * obs-text       = %x80-FF
     *
     * RFC 7230 Section 3.2.3
     * OWS            = *( SP / HTAB )
     *                     ; optional whitespace
     * RWS            = 1*( SP / HTAB )
     *                     ; required whitespace
     * BWS            = OWS
     *                   ; "bad" whitespace - The BWS rule is used where the grammar allows
     *                                        optional whitespace only for historical reasons.
     */


    private final String string;
    private final int N;
    private int idx;

    // Scheme as written
    private String authSchemeStr = null;
    private AuthScheme authScheme = null;
    private Map<String, String> authParams = null;
    // Schemes
    private String basicUserPassword = null;
    private String bearerToken = null;
    private String unknown = null;

    public static AuthHeaderParser parse(String string) {
        string = string.strip();
        AuthHeaderParser obj = new AuthHeaderParser(string);
        try {
            obj.parse$(string);
            return obj;
        } catch (AuthStringException ex) {
            return null;
        }
    }

    private AuthHeaderParser(String string) {
        this.string =string;
        this.N = string.length();
        this.idx = 0;
    }

    /** Get the {@link AuthScheme}. */
    public AuthScheme getAuthScheme() { return authScheme; }

    /** Get the auth scheme as written in the authentication header value. */
    public String getAuthSchemeStr() { return authSchemeStr; }

    /** Any auth scheme value that has a=b auth-params, else null. */
    public Map<String, String> getAuthParams() { return authParams; }

    /** Is this basic auth? */
    public boolean isBasicAuth() { return AuthScheme.BASIC == authScheme; }

    /** Return user-password (still base64 encoded) or null (if not basic auth) */
    public String getBasicUserPassword() {
        return basicUserPassword;
    }

    /** Is this Digest auth? The digest auth-params are available from {@link #getAuthParams()} */
    public boolean isDigestAuth() { return AuthScheme.DIGEST == authScheme; }

    /** Is this Bearer auth? */
    public boolean isBearerAuth() { return AuthScheme.BEARER == authScheme; }

    public String getBearerToken() { return bearerToken; }

    /** Is this some unkown scheme? (not basic, digest or bearer). The rest of the line is available from {@link #getUnknown()}. */
    public boolean isUnknownAuth() { return AuthScheme.UNKNOWN == authScheme; }

    /** The rest of the line for "unknown" */
    public String getUnknown() { return unknown; }

    private static char EndOfStr = 0xFFFF;

    private char nextChar() {
        if ( idx < 0 || idx >= N )
            return EndOfStr;
        char ch = string.charAt(idx);
        idx++;
        return ch;
    }

    private char peekChar() {
        if ( idx < 0 || idx >= N )
            return EndOfStr;
        char ch = string.charAt(idx);
        // No idx++
        return ch;
    }

    private void parse$(String string) {
        // The scheme.
        int N = string.length();
        skipWhitespace();
        String token = token();
        if ( token == null )
            return;

        this.authSchemeStr = token;
        AuthScheme authScheme = AuthScheme.scheme(token);
        if ( authScheme == null )
            return;
        this.authScheme = authScheme;
        skipWhitespace();
        if ( idx >= N )
            return;

        switch(authScheme) {
            case BASIC :   parseBasic(); break;
            case DIGEST :  parseDigest(); break;
            case BEARER :  parseBearer(); break;
            case UNKNOWN : parseUnknown(); break;
            default :
                break;
        }
    }

    /* RFC 7617 - was RFC 2617 - basic
     * challenge   = "Basic" realm
     * credentials = "Basic" basic-credentials
     * basic-credentials = base64-user-pass
     * base64-user-pass  = <base64 [4] encoding of user-pass,
     *                      except not limited to 76 char/line>
     * user-pass   = userid ":" password
     * userid      = *<TEXT excluding ":">
     * password    = *TEXT
    */
    private void parseBasic() {
        // Determine whether a challenge or credentials.
        // challenge is "realm=" and at least one character.
        // credentials is a URL base 64 encoded string, may end in "=".
        if ( isChallenge() )
            authParams = mapAuthParams();
        else
            basicUserPassword = basicBase64();
    }

    /* RFC 7616 - was RFC 2617 - digest
     *
     * challenge        =  "Digest" digest-challenge
     *
     * digest-challenge  = 1#( realm | [ domain ] | nonce |
     *                     [ opaque ] |[ stale ] | [ algorithm ] |
     *                     [ qop-options ] | [auth-param] )
     *
     * domain            = "domain" "=" <"> URI ( 1*SP URI ) <">
     * URI               = absoluteURI | abs_path
     * nonce             = "nonce" "=" nonce-value
     * nonce-value       = quoted-string
     * opaque            = "opaque" "=" quoted-string
     * stale             = "stale" "=" ( "true" | "false" )
     * algorithm         = "algorithm" "=" ( "MD5" | "MD5-sess" | token )
     * qop-options       = "qop" "=" <"> 1#qop-value <">
     * qop-value         = "auth" | "auth-int" | token
     */
    /*
     *  (",")? token "=" (token | quoted-string)
     */
    private void parseDigest() {
        authParams = mapAuthParams();
    }

    /* RFC 6750 - bearer
     *   b64token    = 1*( ALPHA / DIGIT / "-" / "." / "_" / "~" / "+" / "/" ) *"="
     *   credentials = "Bearer" 1*SP b64token
     */
    private void parseBearer() {
        if ( isChallenge() )
            authParams = mapAuthParams();
        else
            bearerToken = b64token();
        // XXX ERROR
    }

    /*
     * The rest of the line, trimmed.
     */
    private void parseUnknown() {
        unknown = string.substring(idx).trim();
    }

    private Map<String, String> mapAuthParams() {
        Map<String, String> map = new LinkedHashMap<>();
        // auth-param = token BWS "=" BWS ( token / quoted-string )
        skipWhitespaceComma();
        while ( idx < N ) {
            String key = token();
            skipWhitespace();
            char ch = nextChar();
            if ( idx == N || ch != '=' ) {
                // Bad. Early end-of-string or no "="
                return null;
            }
            skipWhitespace();
            String value = tokenOrQuotedString();
            if ( value == null )
                return null;
            String lcKey = key.toLowerCase(Locale.ROOT);
            map.put(lcKey, value);
            skipWhitespaceComma();
        }
        return map;
    }

    // Basic and Bearer have a challenge form which is auth-params
    // and a credentials form which is a base64 token.
    // We can tell them apart by the use of "=".
    // If the last equals "=" is followed by at least one non-whitespace character)
    // it is an auth-param which still may fail parsing.
    private boolean isChallenge() {
        // Determine whether a challenge or credentials.
        // challenge is "realm=" and at least one character.
        // credentials is a URL base 64 encoded string, may end in "=".
        // Assumes the string has been stripped of trailing white space.
        int idxEquals = string.lastIndexOf('=');
        if ( idxEquals > 0 && idxEquals < N-1 )
            // An "=" and not the last character, with non "=" after. Challenge.
            return true;
        return false;
    }

    private String tokenOrQuotedString() {
        char ch = peekChar();
        if ( ch == '"' )
            return quotedString();
        else
            return token();
    }

    private String token() {
        int i = idx;
        int j = whileTrue(string, idx, N, test_tchar);
        if ( i == j )
            return null;
        this.idx = j;
        if ( j > 0 )
            return string.substring(i, j);
        return null;
    }

    private String quotedString() {
        if ( string == null )
            return null;
        if ( idx < 0 )
            return null;
        if ( idx >= N )
            return null;
        StringBuilder stringBuilder = new StringBuilder();
        // If reuse a string builder. -- stringBuilder.setLength(0);
        char ch0 = nextChar();
        if ( ch0 != '"' ) {
            System.out.println("Does not start with a dquote");
            return null;
        }

        for (;;) {
            char ch = nextChar();
            if ( ch == EndOfStr )
                return null;
            if ( ch == '"' )
                // Terminating double quote
                return stringBuilder.toString();
            if ( ch == CH_RSLASH ) {
                ch = nextChar();
                if ( ch == EndOfStr )
                    return null;
            }
            stringBuilder.append(ch);
        }
    }

    // Base64 form defined in RFC4648, Section 4. with '+' and '/'
    // An alternative is base64url uses '-' and '_'
    private String basicBase64() {
        int j1 = whileTrue(string, idx, N, test_base64);
        int j2 = whileTrue(string, j1, N, test_base64_pad); // Strictly, one or two.
        if ( j2 != N )
            // Didn't reach the end of string.
            return null;
        return string.substring(idx,  j2);
    }
    // base64url uses '-' and '_'



    //token68        = 1*( ALPHA / DIGIT /
    //                     "-" / "." / "_" / "~" / "+" / "/" ) *"="
    /** Single token68 */
    private String token68() {
        int j1 = whileTrue(string, idx, N, test_tok68);
        int j2 = whileTrue(string, j1, N, test_base64_pad);
        if ( j2 != N )
            // Didn't reach the end of string.
            return null;
        return string.substring(idx,  j2);
    }

    // Bearer "base 64" URL encoding is given as
    // b64token    = 1*( ALPHA / DIGIT /
    //                  "-" / "." / "_" / "~" / "+" / "/" ) *"="
    // which is token68.

    private String b64token() { return token68(); }

    // token
    private static boolean is_tchar(int ch) {
        if ( RiotChars.isA2ZN(ch) )
            return true;
        switch (ch) {
            case '!': case '#': case '$': case '%': case '&': case '\'': case '*':
            case '+': case '-': case '.': case '^': case '_': case '`': case '|': case '~':
                return true;
        }
        return false;
    }

    // Token68, without padding.
    // Used by bearer auth.
    private static boolean is_tok68(int ch) {
        if ( RiotChars.isA2ZN(ch) )
            return true;
        switch (ch) {
            // URL base64
            case '-': case '.': case '_': case '~': case '+': case '/':
                // URL base64 (not for URLs)   A2ZN and '-' and case '_'
                // Plain base64 (not for URLs) A2ZN and '+' and '/'
                return true;
        }
        return false;
    }

    // RFC 4648, section 4 base 64. Uses '+' and '/'
    private static boolean is_base64(int ch) {
        if ( RiotChars.isA2ZN(ch) )
            return true;
        // URL base64 (not for URLs)   A2ZN and '-' and case '_'
        // Plain base64 (not for URLs) A2ZN and '+' and '/'
        return ch == '+' || ch == '/';
    }

    private static IntPredicate test_base64       = AuthHeaderParser::is_base64;
    private static IntPredicate test_tok68        = AuthHeaderParser::is_tok68;
    private static IntPredicate test_base64_pad   = ch -> ch == '=';
    private static IntPredicate test_tchar        = AuthHeaderParser::is_tchar;
    private static IntPredicate testNWS           = ch -> ( ch != ' ' && ch != '\t' );
    private static IntPredicate testWS            = ch -> ( ch == ' ' || ch == '\t' );
    private static IntPredicate testWSC           = ch -> ( ch == ' ' || ch == '\t' || ch == ',' );

    private void skipWhitespace() {
        int j = whileTrue(string, idx, N, testWS);
        this.idx = j;
    }

    private void skipWhitespaceComma() {
        int j = whileTrue(string, idx, N, testWSC);
        this.idx = j;
    }

    private static int whileTrue(String string, int idx, int N, IntPredicate test) {
        for ( ; idx<N ; idx++ ) {
            char ch = string.charAt(idx);
            if ( ! test.test(ch) )
                return idx;
        }
        return idx;
    }
}
