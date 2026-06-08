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

import java.util.Objects ;

/**
 * Constants and operations from RFC 2617 (digest, now RFC 7616; basic, now RFC 7617)
 * Original digest authentication: RFC 2069
 */
class AuthHttp {
    // Field names
    public static final String strUsername      = "username";
    public static final String strRealm         = "realm";
    public static final String strNonce         = "nonce";
    public static final String strNc            = "nc";
    public static final String strCNonce        = "cnonce";
    public static final String strQop           = "qop";
    public static final String strResponse      = "response";
    public static final String strOpaque        = "opaque";
    public static final String strUri           = "uri";
    public static final String strAlgorithm     = "algorithm";

    // Algorithm names.
    public static final String algortihmSHA256           = "SHA-256";
    public static final String algortihmSHA256_sess      = "SHA-256-sess";
    public static final String algortihmSHA512_256       = "SHA-512-256";
    public static final String algortihmSHA512_256_sess  = "SHA-512-256-sess";
    public static final String algortihmMD5              = "MD5";
    public static final String algortihmMD5_sess         = "MD5-sess";

    public static String KD(String secret, String data, DigestAlgorithm digestAlgorithm) {
        return H(secret+":"+data, digestAlgorithm) ;
    }

    public static String H(String string, DigestAlgorithm algorithm) {
        return algorithm.digest.apply(string);
    }

    public static String A1(String username, String realm, String password, DigestAlgorithm digestAlgorithm) {
        Objects.requireNonNull(username) ;
        Objects.requireNonNull(realm) ;
        Objects.requireNonNull(password) ;
        String s = username+":"+realm+":"+password ;
        return s ;
    }

    public static String A1_sess(String username, String realm, String password, String nonce, String cnonce, DigestAlgorithm digestAlgorithm) {
        Objects.requireNonNull(username) ;
        Objects.requireNonNull(realm) ;
        Objects.requireNonNull(password) ;
        Objects.requireNonNull(nonce) ;
        Objects.requireNonNull(cnonce) ;
        String s = username+":"+realm+":"+password ;
        String x = H(s, digestAlgorithm)+":"+nonce+":"+cnonce ;
        return s ;
    }

    public static String A2_auth(String method, String uri) {
        return method+":"+uri ;
    }

    public static String A2_auth_int(String method, String uri, String entityBody) {
        throw new UnsupportedOperationException() ;
    }
}
