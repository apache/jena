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
 * Parser for authentication header strings for both challenge and credentials.
 * <ul>
 * <li>This parser is scheme-specific. e.g. digest credentials can not be token68.
 * <li>This parser does not check auth-params names.
 * <li>The auth-params map has lower case keys.
 * </ul>
 * Covers:
 * <ul>
 * <li><a href="https://www.rfc-editor.org/rfc/rfc7617.html">RFC 7617</a> - was
 * <a href="https://www.rfc-editor.org/rfc/rfc7617.html">RFC 7617</a> - basic
 * authentication
 * <li><a href="https://www.rfc-editor.org/rfc/rfc7616.html">RFC 7616</a> - was
 * <a href="https://www.rfc-editor.org/rfc/rfc7617.html">RFC 617</a> - digest
 * authentication
 * <li><a href="https://www.rfc-editor.org/rfc/rfc6750.html">RFC 6750</a> - Bearer
 * authentication
 * <li>"Unknown"
 * </ul>
 */
public class AuthHeader {
    // Update for RFC 9112
    /* RFC 7235 header:
     *    WWW-Authenticate:
     *    Authorization:
     *    Proxy-Authenticate:
     *    Proxy-Authorization:
     *
     * Appendix C. Collected ABNF
     *    # is a comma-separate list
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

    private final String original;

    // Scheme as written
    private final String authSchemeName;
    // The rest of the value, stripped of leading and trailing whitespace.
    private final String authSchemeArgs;

    private final AuthScheme authScheme;
    // Any key-value pairs.
    private final Map<String, String> authParams;

    // Per schema fields.
    private final String basicUserPassword;
    private final String bearerToken;
    private final String unknown;

    /** Create an AuthHeader by parsing an "Authorization" header. */
    public static AuthHeader parseAuth(String string) {
        return parse(string, false);
    }

    /** Create an AuthHeader by parsing a "WWW-Authenticate" header. */
    public static AuthHeader parseChallenge(String string) {
        return parse(string, true);
    }

    /** Create an AuthHeader by parsing an "Authorization" or "WWW-Authenticate" header. */
    private static AuthHeader parse(String string, boolean isChallenge) {
        string = string.strip();
        AuthHeader.Builder builder = new AuthHeader.Builder(string, isChallenge);
        try {
            builder.parse$();
            return builder.build();
        } catch (AuthParseException ex) {
            return null;
        }
    }

    /** Create an AuthHeader for bearer with a token value. */
    public static AuthHeader bearerToken(String tokenString) {
        AuthHeader.Builder builder = new AuthHeader.Builder();
        return builder.setAuthScheme(AuthScheme.BEARER).setBearerToken(tokenString).build();
    }

    private static class AuthParseException extends RuntimeException {}

    private AuthHeader(String original, AuthScheme authScheme, String authSchemeName, String authSchemeArgs,
                       Map<String, String> authParams, String basicUserPassword, String bearerToken, String unknown) {
        this.original = original;
        this.authScheme = authScheme;
        this.authSchemeName = authSchemeName;
        this.authSchemeArgs = authSchemeArgs;
        this.authParams = authParams;
        this.basicUserPassword = basicUserPassword;
        this.bearerToken = bearerToken;
        this.unknown = unknown;

    }

    /** Get the {@link AuthScheme}. */
    public AuthScheme getAuthScheme() { return authScheme; }

    /** Get the auth scheme as written in the authentication header value. */
    public String getAuthSchemeName() { return authSchemeName; }

    /** Get the string after the auth scheme as written in the authentication header value. */
    public String getAuthArgs() { return authSchemeArgs; }

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

    private static class Builder {

        // The original header value.
        private final String string;
        private final int N;
        private final boolean isChallenge;

        private int idx;
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

        // Scheme as written
        private String authSchemeName = null;
        // The rest of the value, stripped of leading and trailing whitespace.
        private String authSchemeArgs = null;

        private AuthScheme authScheme = null;
        // Any key-value pairs.
        private Map<String, String> authParams = null;

        // Per schema fields.
        private String basicUserPassword = null;
        private String bearerToken = null;
        private String unknown = null;

        // Parsing setup.
        Builder(String string, boolean isChallenge) {
            this.isChallenge = isChallenge;
            this.string = string;
            this.N = string.length();
        }

        // Non-parsing setup.
        Builder() {
            this.isChallenge = false;
            this.string = null;
            this.N = 0;
        }

        public Builder setAuthSchemeName(String authSchemeName) {
            this.authSchemeName = authSchemeName;
            return this;
        }

        public Builder setAuthSchemeArgs(String authSchemeArgs) {
            this.authSchemeArgs = authSchemeArgs;
            return this;
        }

        public Builder setAuthScheme(AuthScheme authScheme) {
            this.authScheme = authScheme;
            return this;
        }

        public Builder setAuthParams(Map<String, String> authParams) {
            this.authParams = authParams;
            return this;
        }

        public Builder setBasicUserPassword(String basicUserPassword) {
            this.basicUserPassword = basicUserPassword;
            return this;
        }

        public Builder setBearerToken(String bearerToken) {
            this.bearerToken = bearerToken;
            return this;
        }

        public Builder setUnknownToken(String unknownToken) {
            this.unknown = unknownToken;
            return this;
        }

        /** Set the fields by parsing */
        private void parse$() {
            // The scheme.
            skipWhitespace();
            String token = httpToken();
            if ( token == null )
                return;

            setAuthSchemeName(token);
            AuthScheme authScheme = AuthScheme.scheme(token);
            if ( authScheme == null )
                return;
            setAuthScheme(authScheme);
            skipWhitespace();
            if ( idx >= N )
                return;
            setAuthSchemeArgs(string.substring(idx));

            switch(authScheme) {
                case BASIC :   parseBasic(); break;
                case DIGEST :  parseDigest(); break;
                case BEARER :  parseBearer(); break;
                case UNKNOWN : parseUnknown(); break;
                default :
                    break;
            }
        }

        private AuthHeader build() {
            return new AuthHeader(string, authScheme, authSchemeName, authSchemeArgs, authParams, basicUserPassword, bearerToken, unknown);
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
                setAuthParams(mapAuthParams());
            else
                setBasicUserPassword(basicBase64());
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
            setAuthParams(mapAuthParams());
        }

        /* RFC 6750 - bearer
         *   b64token    = 1*( ALPHA / DIGIT / "-" / "." / "_" / "~" / "+" / "/" ) *"="
         *   credentials = "Bearer" 1*SP b64token
         * But JWT occurs where each part is padded and so has internal "="
         */
        private void parseBearer() {
            int startIdx = idx;
            if ( isChallenge() )
                setAuthParams(mapAuthParams());
            else {
                // Generalizes b64token to cover internal "=" as found in JWT.
                String token = bearerToken();
                if ( token == null )
                    setUnknownToken(string.substring(startIdx).trim());
                else {
                    setBearerToken(token);
                }
            }
        }

        // Remove trailing padding characters ('=') of base64.
        private String stripPadding(String token) {
            int idx = token.length();
            for (; idx > 0; idx--) {
                if ( token.charAt(idx-1) != '=' )
                    break;
            }
            if ( idx == token.length() )
                return token;
            return token.substring(0,idx);
        }

        /*
         * The rest of the line, trimmed.
         */
        private void parseUnknown() {
            setUnknownToken(string.substring(idx).trim());
        }

        private Map<String, String> mapAuthParams() {
            Map<String, String> map = new LinkedHashMap<>();
            // auth-param = token BWS "=" BWS ( token / quoted-string )
            skipWhitespaceComma();
            while ( idx < N ) {
                String key = httpToken();
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

        private boolean isChallenge() { return isChallenge; }

        private String tokenOrQuotedString() {
            char ch = peekChar();
            if ( ch == '"' )
                return quotedString();
            else
                return httpToken();
        }

        // HTTP token.
        private String httpToken() {
            int i = idx;
            int j = whileTrue(string, idx, N, test_http_tokenchar);
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
                //System.err.println("Does not start with a dquote");
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

        //token68        = 1*( ALPHA / DIGIT /
        //                     "-" / "." / "_" / "~" / "+" / "/" ) *"="
        /** RFC 7235 : Single token68 */
        private String token68() {
            int j1 = whileTrue(string, idx, N, test_tok68);
            int j2 = whileTrue(string, j1, N, test_base64_pad);
            if ( j2 != N )
                // Didn't reach the end of string.
                return null;
            String s = string.substring(idx,  j2);
            idx = j2;
            return s;
        }

        // RFC6750 OAuth
        // Bearer "base 64" URL encoding is given as
        // b64token    = 1*( ALPHA / DIGIT /
        //                  "-" / "." / "_" / "~" / "+" / "/" ) *"="
        // which is token68.

        private String b64token() { return token68(); }

        /**
         * bearerToken as found in the wild
         * Allows internal "=".
         */
        private String bearerToken() {
            int j = whileTrue(string, idx, N, test_auth_tokenchar);
            if ( j != N )
                // Didn't reach the end of string.
                return null;
            String s = string.substring(idx,  j);
            idx = j;
            return s;
        }

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
        private static boolean is_tok68(int ch) {
            if ( RiotChars.isA2ZN(ch) )
                return true;
            switch (ch) {
                // URL base64 (not for URLs)   A2ZN and '-' and case '_'
                // Plain base64 (not for URLs) A2ZN and '+' and '/'
                case '-': case '.': case '_': case '~': case '+': case '/':
                    return true;
            }
            return false;
        }

        // Include internal "=" for JWT usage.
        private static boolean is_authTokenChar(int ch) {
            if ( RiotChars.isA2ZN(ch) )
                return true;
            switch (ch) {
                case '-': case '.': case '_': case '~': case '+': case '/':
                case '=':
                    // JWTs where are 3 parts and each part is padded base64 -- tttt==.tttt=.tttt=
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

        private static IntPredicate test_base64         = AuthHeader.Builder::is_base64;
        private static IntPredicate test_tok68          = AuthHeader.Builder::is_tok68;
        private static IntPredicate test_base64_pad     = ch -> ch == '=';
        private static IntPredicate test_http_tokenchar = AuthHeader.Builder::is_tchar;
        private static IntPredicate test_auth_tokenchar = AuthHeader.Builder::is_authTokenChar;
        private static IntPredicate testNWS             = ch -> ( ch != ' ' && ch != '\t' );
        private static IntPredicate testWS              = ch -> ( ch == ' ' || ch == '\t' );
        private static IntPredicate testWSC             = ch -> ( ch == ' ' || ch == '\t' || ch == ',' );

        private void skipWhitespace() {
            int j = whileTrue(string, idx, N, testWS);
            this.idx = j;
        }

        private void skipWhitespaceComma() {
            int j = whileTrue(string, idx, N, testWSC);
            this.idx = j;
        }

        private static int whileTrue(String string, int i, int N, IntPredicate test) {
            for ( ; i<N ; i++ ) {
                char ch = string.charAt(i);
                if ( ! test.test(ch) )
                    return i;
            }
            return N;
        }
    }
}
