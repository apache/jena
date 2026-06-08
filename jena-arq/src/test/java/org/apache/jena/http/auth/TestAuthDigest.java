/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.http.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class TestAuthDigest {
    // Examples from RFC 2617

    // digest 1
    // No algorithm - use MD5
    // MD5:          6629fae49393a05397450978507c4ef1
    // SHA-256:      5abdd07184ba512a22c53f41470e5eea7dcaa3a93a59b630c13dfe0a5dc6e38b
    // SHA-512-256:  f23c08ec7334a881f8286e68450ddbd9f0cd91c41481f0e1433604da8113c6dc

    @Test
    public void digest_1_no_algorithm() {
        String header = "Digest realm=\"testrealm@host.com\", qop=\"auth,auth-int\","+
                " nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\","+
                " opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"";
        AuthChallenge aHeader = AuthChallenge.parse(header);

        String requestTarget = "/dir/index.html";
        String method = "GET";
        String cnonce = "0a4f113b";
        String nc = "00000001";
        String username = "Mufasa";
        String password = "Circle Of Life";

        String responseField =
                DigestLib.calcDigestChallengeResponse(aHeader, username, password,
                                                      method, requestTarget,
                                                      cnonce, nc, "auth");

        assertEquals("6629fae49393a05397450978507c4ef1", responseField);
    }


    @Test
    public void digest_1_md5() {
        String header = "Digest realm=\"testrealm@host.com\", qop=\"auth,auth-int\","+
                " nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\","+
                " opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"" +
                " algorithm=MD5";
        AuthChallenge aHeader = AuthChallenge.parse(header);

        String requestTarget = "/dir/index.html";
        String method = "GET";
        String cnonce = "0a4f113b";
        String nc = "00000001";
        String username = "Mufasa";
        String password = "Circle Of Life";

        String responseField =
                DigestLib.calcDigestChallengeResponse(aHeader, username, password,
                                                      method, requestTarget,
                                                      cnonce, nc, "auth");

        assertEquals("6629fae49393a05397450978507c4ef1", responseField);
    }

    @Test
    public void digest_1_sha256() {
        String header = "Digest realm=\"testrealm@host.com\", qop=\"auth,auth-int\","+
                " nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\","+
                " opaque=\"5ccc069c403ebaf9f0171e9517f40e41\""+
                " algorithm=SHA-256";
        AuthChallenge aHeader = AuthChallenge.parse(header);

        String requestTarget = "/dir/index.html";
        String method = "GET";
        String cnonce = "0a4f113b";
        String nc = "00000001";
        String username = "Mufasa";
        String password = "Circle Of Life";

        String responseField =
                DigestLib.calcDigestChallengeResponse(aHeader, username, password,
                                                      method, requestTarget,
                                                      cnonce, nc, "auth");

        assertEquals("5abdd07184ba512a22c53f41470e5eea7dcaa3a93a59b630c13dfe0a5dc6e38b", responseField);
    }

    @Test
    public void digest_1_sha512_256() {
        String header = "Digest realm=\"testrealm@host.com\", qop=\"auth,auth-int\","+
                " nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\","+
                " opaque=\"5ccc069c403ebaf9f0171e9517f40e41\""+
                " algorithm=SHA-512-256";
        AuthChallenge aHeader = AuthChallenge.parse(header);

        String requestTarget = "/dir/index.html";
        String method = "GET";
        String cnonce = "0a4f113b";
        String nc = "00000001";
        String username = "Mufasa";
        String password = "Circle Of Life";

        String responseField =
                DigestLib.calcDigestChallengeResponse(aHeader, username, password,
                                                      method, requestTarget,
                                                      cnonce, nc, "auth");

        assertEquals("f23c08ec7334a881f8286e68450ddbd9f0cd91c41481f0e1433604da8113c6dc", responseField);
    }


    // Example from RFC 7616

    // digest 2
    // MD5:         8ca523f5e9506fed4657c9700eebdbec
    // SHA-256      753927fa0e85d155564e2e272a28d1802ca10daf4496794697cf8db5856cb6c1
    // SHA-512-256: 430d05014cecc49cab6fbe03176d41a1da86cbfe24a16580e22aaad928d960d0

    @Test
    public void digest_2() {
        String header = "Digest realm=\"http-auth@example.org\", qop=\"auth,auth-int\"," +
                " nonce=\"7ypf/xlj9XXwfDPEoM4URrv/xwf94BcCAzFZH4GiTo0v\","+
                " opaque=\"FQhe/qaU925kfnzjCev0ciny7QMkPqMAFRtzCUYo5tdS\"";
        AuthChallenge aHeader = AuthChallenge.parse(header);
        String requestTarget = "/dir/index.html";
        String method = "GET";
        String cnonce = "f2/wE4q74E6zIJEtWaHKaf5wv/H5QzzpXusqGemxURZJ";
        String nc = "00000001";
        String username = "Mufasa";
        String password = "Circle of Life"; // NB Different to RFC 2617

        String responseField =
                DigestLib.calcDigestChallengeResponse(aHeader, username, password,
                                                      method, requestTarget,
                                                      cnonce, nc, "auth");

        assertEquals("8ca523f5e9506fed4657c9700eebdbec", responseField);
    }

    // Example from RFC 7616
    @Test
    public void digest_2_sha256() {
        String header = "Digest realm=\"http-auth@example.org\", qop=\"auth,auth-int\"," +
                " nonce=\"7ypf/xlj9XXwfDPEoM4URrv/xwf94BcCAzFZH4GiTo0v\","+
                " opaque=\"FQhe/qaU925kfnzjCev0ciny7QMkPqMAFRtzCUYo5tdS\""+
                " algorithm=SHA-256";
        AuthChallenge aHeader = AuthChallenge.parse(header);
        String requestTarget = "/dir/index.html";
        String method = "GET";
        String cnonce = "f2/wE4q74E6zIJEtWaHKaf5wv/H5QzzpXusqGemxURZJ";
        String nc = "00000001";
        String username = "Mufasa";
        String password = "Circle of Life"; // NB Different to RFC 2617

        String responseField =
                DigestLib.calcDigestChallengeResponse(aHeader, username, password,
                                                      method, requestTarget,
                                                      cnonce, nc, "auth");

        assertEquals("753927fa0e85d155564e2e272a28d1802ca10daf4496794697cf8db5856cb6c1", responseField);
    }

    // Example from RFC 7616
    @Test
    public void digest_2_sha512_256() {
        String header = "Digest realm=\"http-auth@example.org\", qop=\"auth,auth-int\"," +
                " nonce=\"7ypf/xlj9XXwfDPEoM4URrv/xwf94BcCAzFZH4GiTo0v\","+
                " opaque=\"FQhe/qaU925kfnzjCev0ciny7QMkPqMAFRtzCUYo5tdS\""+
                " algorithm=SHA-512-256";
        AuthChallenge aHeader = AuthChallenge.parse(header);
        String requestTarget = "/dir/index.html";
        String method = "GET";
        String cnonce = "f2/wE4q74E6zIJEtWaHKaf5wv/H5QzzpXusqGemxURZJ";
        String nc = "00000001";
        String username = "Mufasa";
        String password = "Circle of Life"; // NB Different to RFC 2617

        String responseField =
                DigestLib.calcDigestChallengeResponse(aHeader, username, password,
                                                      method, requestTarget,
                                                      cnonce, nc, "auth");

        assertEquals("430d05014cecc49cab6fbe03176d41a1da86cbfe24a16580e22aaad928d960d0", responseField);
    }

    // Bad algorithm
    @Test
    public void digest_3_unknown() {
        String header = "Digest realm=\"testrealm@host.com\", qop=\"auth,auth-int\","+
                " nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\","+
                " opaque=\"5ccc069c403ebaf9f0171e9517f40e41\""+
                " algorithm=UNKNOWN";
        AuthChallenge aHeader = AuthChallenge.parse(header);

        String requestTarget = "/dir/index.html";
        String method = "GET";
        String cnonce = "0a4f113b";
        String nc = "00000001";
        String username = "Mufasa";
        String password = "Circle Of Life";

        String responseField =
                DigestLib.calcDigestChallengeResponse(aHeader, username, password,
                                                      method, requestTarget,
                                                      cnonce, nc, "auth");

        assertNull(responseField);
    }
}
