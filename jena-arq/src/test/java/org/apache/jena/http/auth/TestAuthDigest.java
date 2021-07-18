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

package org.apache.jena.http.auth;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestAuthDigest {
    // Example from RFC 2617
    @Test
    public void digest_1() {
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

    // Example from RFC 7616
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
}
