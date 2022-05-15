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

    private static AuthHeaderParser parse(String input) {
        return AuthHeaderParser.parse(input);
    }

    @Test public void parse_empty() {
        AuthHeaderParser auth = parse("");
        assertNull(auth.getAuthScheme());
    }

    @Test public void parse_basic_01() {
        AuthHeaderParser auth = parse("Basic       BASE64");
        assertEquals(AuthScheme.BASIC, auth.getAuthScheme());
        assertTrue(auth.isBasicAuth());
        assertFalse(auth.isDigestAuth());
        assertFalse(auth.isBearerAuth());
        assertFalse(auth.isUnknownAuth());
        assertEquals("BASE64", auth.getBasicUserPassword());
    }

    @Test public void parse_basic_02() {
        AuthHeaderParser auth = parse("Basic dGVzdDoxMjPCow==");
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

    @Test public void parse_basic_03() {
        AuthHeaderParser auth = parse("Basic realm = hades");
        assertTrue(auth.isBasicAuth());
        assertFalse(auth.isDigestAuth());
        Map<String, String> map = auth.getAuthParams();
        assertNotNull(map);
        assertEquals(1, map.size());
        assertEquals("hades", map.get("realm"));
    }

    @Test public void parse_basic_04() {
        AuthHeaderParser auth = parse("Basic realm=hades");
        assertTrue(auth.isBasicAuth());
        assertFalse(auth.isDigestAuth());
        Map<String, String> map = auth.getAuthParams();
        assertNotNull(map);
        assertEquals(1, map.size());
        assertEquals("hades", map.get("realm"));
    }

    // fn5+fjp/f38K is "~~~~:\x7F\x7F\x7F"
    @Test public void parse_basic_05() {
        AuthHeaderParser auth = parse("Basic fn5+fjp/f38K");
        assertTrue(auth.isBasicAuth());
        assertFalse(auth.isDigestAuth());
        Map<String, String> map = auth.getAuthParams();
        assertNull(map);
        assertNotNull(auth.getBasicUserPassword());
    }

    @Test public void parse_basic_bad_01() {
        AuthHeaderParser auth = parse("Basic {}ABCD");  // Not base64
        assertTrue(auth.isBasicAuth());
        Map<String, String> map = auth.getAuthParams();
        assertNull(map);
        assertNull(auth.getBasicUserPassword());
    }

    @Test public void parse_bad_01() {
        AuthHeaderParser auth = parse("Basic realm =");
        assertTrue(auth.isBasicAuth());
        assertNull(auth.getAuthParams());
        assertNull(auth.getBasicUserPassword());
    }

    @Test public void parse_bad_02() {
        AuthHeaderParser auth = parse("Basic ");
        assertTrue(auth.isBasicAuth());
        assertNull(auth.getAuthParams());
        assertNull(auth.getBasicUserPassword());
    }

    @Test public void parse_digest_01() {
        AuthHeaderParser auth = parse("Digest realm = hades");
        assertTrue(auth.isDigestAuth());
        assertFalse(auth.isBasicAuth());
        assertEquals(AuthScheme.DIGEST, auth.getAuthScheme());

        Map<String, String> map = auth.getAuthParams();
        assertNotNull(map);
        assertEquals(1, map.size());
        assertEquals("hades", map.get("realm"));
    }

    @Test public void parse_digest_02() {
        AuthHeaderParser auth = parse("Digest a=b C=\"def\", xyz=\"rst uvw\"");
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
        AuthHeaderParser auth = parse("Digest a=b c=");
        assertTrue(auth.isDigestAuth());
        assertFalse(auth.isBasicAuth());
        assertEquals(AuthScheme.DIGEST, auth.getAuthScheme());

        Map<String, String> map = auth.getAuthParams();
        assertNull(map);
    }

    @Test public void parse_digest_bad_02() {
        AuthHeaderParser auth = parse("Digest c=\"");
        assertTrue(auth.isDigestAuth());
        assertFalse(auth.isBasicAuth());
        assertEquals(AuthScheme.DIGEST, auth.getAuthScheme());

        Map<String, String> map = auth.getAuthParams();
        assertNull(map);
    }

    @Test public void parse_bearer_01() {
        // Credentials
        AuthHeaderParser auth = parse("Bearer AAAA");
        assertTrue(auth.isBearerAuth());
        assertFalse(auth.isDigestAuth());
        assertFalse(auth.isBasicAuth());
        assertEquals("AAAA", auth.getBearerToken());
        Map<String, String> map = auth.getAuthParams();
        assertNull(map);
    }

    @Test public void parse_bearer_02() {
        // Challenge
        AuthHeaderParser auth = parse("Bearer realm = hades");
        assertNull(auth.getBearerToken());
        Map<String, String> map = auth.getAuthParams();
        assertNotNull(map);
        assertEquals("hades", map.get("realm"));
    }

    @Test public void parse_bearer_03() {
        // Challenge
        AuthHeaderParser auth = parse("Bearer realm=hades");
        assertNull(auth.getBearerToken());
        Map<String, String> map = auth.getAuthParams();
        assertNotNull(map);
        assertEquals("hades", map.get("realm"));
    }

    @Test public void parse_bearer_bad_01() {
        AuthHeaderParser auth = parse("Bearer ");
        assertTrue(auth.isBearerAuth());
        assertNull(auth.getBearerToken());
        Map<String, String> map = auth.getAuthParams();
        assertNull(map);
    }

    @Test public void parse_bearer_bad_03() {
        AuthHeaderParser auth = parse("Bearer abcd()"); // Not base64
        assertTrue(auth.isBearerAuth());
        assertNull(auth.getBearerToken());
    }

    @Test public void parse_unknown_01() {
        // scheme, param
        AuthHeaderParser auth = parse("Unknown abcd");
        assertEquals(AuthScheme.UNKNOWN, auth.getAuthScheme());
        assertEquals("abcd", auth.getUnknown());
    }

    @Test public void parse_unknown_02() {
        AuthHeaderParser auth = parse("UnKnOwN a=b");
        assertEquals(AuthScheme.UNKNOWN, auth.getAuthScheme());
        assertEquals("UnKnOwN", auth.getAuthSchemeStr());
        assertEquals("a=b", auth.getUnknown());
    }
}