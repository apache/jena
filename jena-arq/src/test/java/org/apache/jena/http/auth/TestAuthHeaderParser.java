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

import static org.junit.Assert.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import org.apache.jena.atlas.web.AuthScheme;
import org.junit.Test;

public class TestAuthHeaderParser {

    private static AuthHeader parseAuth(String input) {
        return AuthHeader.parseAuth(input);
    }

    private static AuthHeader parseChallenge(String input) {
        return AuthHeader.parseChallenge(input);
    }


    @Test public void parse_empty() {
        AuthHeader auth = parseAuth("");
        assertNull(auth.getAuthScheme());
    }

    @Test public void parse_basic_01() {
        AuthHeader auth = parseAuth("Basic       BASE64");
        assertEquals(AuthScheme.BASIC, auth.getAuthScheme());
        assertTrue(auth.isBasicAuth());
        assertFalse(auth.isDigestAuth());
        assertFalse(auth.isBearerAuth());
        assertFalse(auth.isUnknownAuth());
        assertEquals("BASE64", auth.getBasicUserPassword());
    }

    @Test public void parse_basic_02() {
        AuthHeader auth = parseAuth("Basic dGVzdDoxMjPCow==");
        assertEquals(AuthScheme.BASIC, auth.getAuthScheme());
        assertTrue(auth.isBasicAuth());
        assertFalse(auth.isDigestAuth());
        assertFalse(auth.isBearerAuth());
        assertFalse(auth.isUnknownAuth());
        String userPassword = auth.getBasicUserPassword();
        assertNotNull(userPassword);
        byte[] bytes = userPassword.getBytes(StandardCharsets.US_ASCII);
        String x = new String(Base64.getUrlDecoder().decode(bytes), StandardCharsets.UTF_8);
        assertTrue(x.contains(":"));
        assertEquals(x, "test:123£");
    }

    // fn5+fjp/f38K is "~~~~:\x7F\x7F\x7F"
    @Test public void parse_basic_03() {
        AuthHeader auth = parseAuth("Basic fn5+fjp/f38K");
        assertTrue(auth.isBasicAuth());
        assertFalse(auth.isDigestAuth());
        Map<String, String> map = auth.getAuthParams();
        assertNull(map);
        assertNotNull(auth.getBasicUserPassword());
    }

    @Test public void parse_basic_bad_01() {
        AuthHeader auth = parseAuth("Basic {}ABCD");  // Not base64
        assertTrue(auth.isBasicAuth());
        Map<String, String> map = auth.getAuthParams();
        assertNull(map);
        assertNull(auth.getBasicUserPassword());
    }

    @Test public void parse_bad_01() {
        AuthHeader auth = parseAuth("Basic realm =");
        assertTrue(auth.isBasicAuth());
        assertNull(auth.getAuthParams());
        assertNull(auth.getBasicUserPassword());
    }

    @Test public void parse_bad_02() {
        AuthHeader auth = parseAuth("Basic ");
        assertTrue(auth.isBasicAuth());
        assertNull(auth.getAuthParams());
        assertNull(auth.getBasicUserPassword());
    }

    @Test public void parse_digest_01() {
        AuthHeader auth = parseAuth("Digest realm = hades");
        assertTrue(auth.isDigestAuth());
        assertFalse(auth.isBasicAuth());
        assertEquals(AuthScheme.DIGEST, auth.getAuthScheme());

        Map<String, String> map = auth.getAuthParams();
        assertNotNull(map);
        assertEquals(1, map.size());
        assertEquals("hades", map.get("realm"));
    }

    @Test public void parse_digest_02() {
        AuthHeader auth = parseAuth("Digest a=b C=\"def\", xyz=\"rst uvw\"");
        assertTrue(auth.isDigestAuth());
        assertFalse(auth.isBasicAuth());
        assertEquals(AuthScheme.DIGEST, auth.getAuthScheme());

        Map<String, String> map = auth.getAuthParams();
        assertNotNull(map);

        assertEquals(3, map.size());
        assertEquals("b", map.get("a"));
        assertEquals("def", map.get("c"));  // Lower case
        assertEquals("rst uvw", map.get("xyz"));
    }

    @Test public void parse_digest_bad_01() {
        AuthHeader auth = parseAuth("Digest a=b c=");
        assertTrue(auth.isDigestAuth());
        assertFalse(auth.isBasicAuth());
        assertEquals(AuthScheme.DIGEST, auth.getAuthScheme());

        Map<String, String> map = auth.getAuthParams();
        assertNull(map);
    }

    @Test public void parse_digest_bad_02() {
        AuthHeader auth = parseAuth("Digest c=\"");
        assertTrue(auth.isDigestAuth());
        assertFalse(auth.isBasicAuth());
        assertEquals(AuthScheme.DIGEST, auth.getAuthScheme());

        Map<String, String> map = auth.getAuthParams();
        assertNull(map);
    }

    @Test public void parse_bearer_01() {
        // Credentials
        AuthHeader auth = parseAuth("Bearer AAAA");
        assertTrue(auth.isBearerAuth());
        assertFalse(auth.isDigestAuth());
        assertFalse(auth.isBasicAuth());
        assertEquals("AAAA", auth.getBearerToken());
        Map<String, String> map = auth.getAuthParams();
        assertNull(map);
    }


    @Test public void parse_bearer_bad_01() {
        AuthHeader auth = parseAuth("Bearer ");
        assertTrue(auth.isBearerAuth());
        assertNull(auth.getBearerToken());
        Map<String, String> map = auth.getAuthParams();
        assertNull(map);
    }

    @Test public void parse_bearer_bad_03() {
        AuthHeader auth = parseAuth("Bearer abcd()"); // Not base64
        assertTrue(auth.isBearerAuth());
        assertNull(auth.getBearerToken());
    }

    @Test public void parse_unknown_01() {
        // scheme, param
        AuthHeader auth = parseAuth("Unknown abcd");
        assertEquals(AuthScheme.UNKNOWN, auth.getAuthScheme());
        assertEquals("abcd", auth.getUnknown());
    }

    @Test public void parse_unknown_02() {
        AuthHeader auth = parseAuth("UnKnOwN a=b");
        assertEquals(AuthScheme.UNKNOWN, auth.getAuthScheme());
        assertEquals("UnKnOwN", auth.getAuthSchemeName());
        assertEquals("a=b", auth.getUnknown());
    }



    @Test public void parse_challenge_01() {
        AuthHeader auth = parseChallenge("Bearer realm=\"somewhere\"");
        assertEquals(AuthScheme.BEARER, auth.getAuthScheme());
        assertEquals("somewhere", auth.getAuthParams().get("realm"));
    }

    @Test public void parse_challenge_basic_01() {
        AuthHeader auth = parseChallenge("Basic realm = hades");
        assertTrue(auth.isBasicAuth());
        assertFalse(auth.isDigestAuth());
        Map<String, String> map = auth.getAuthParams();
        assertNotNull(map);
        assertEquals(1, map.size());
        assertEquals("hades", map.get("realm"));
    }

    @Test public void parse_challenge_basic_02() {
        AuthHeader auth = parseChallenge("Basic realm=hades");
        assertTrue(auth.isBasicAuth());
        assertFalse(auth.isDigestAuth());
        Map<String, String> map = auth.getAuthParams();
        assertNotNull(map);
        assertEquals(1, map.size());
        assertEquals("hades", map.get("realm"));
    }

    @Test public void parse_challenge_bearer_01() {
        // Challenge
        AuthHeader auth = parseChallenge("Bearer realm = hades");
        assertNull(auth.getBearerToken());
        Map<String, String> map = auth.getAuthParams();
        assertNotNull(map);
        assertEquals("hades", map.get("realm"));
    }

    @Test public void parse_challenge_bearer_02() {
        // Challenge
        AuthHeader auth = parseChallenge("Bearer realm=hades");
        assertNull(auth.getBearerToken());
        Map<String, String> map = auth.getAuthParams();
        assertNotNull(map);
        assertEquals("hades", map.get("realm"));
    }

}