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
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.http.auth;

import static org.apache.jena.http.auth.AuthHttp.*;
import java.util.function.Function;

import org.apache.commons.codec.digest.DigestUtils;

/** Algorithms for digest authentication. */
public enum DigestAlgorithm {
    SHA_256(algortihmSHA256, DigestUtils::sha256Hex),
    SHA_256_sess(algortihmSHA256_sess, DigestUtils::sha256Hex),
    SHA_512_256(algortihmSHA512_256, DigestUtils::sha512_256Hex),
    SHA_512_256_sess(algortihmSHA512_256_sess, DigestUtils::sha512_256Hex),
    MD5(algortihmMD5, DigestUtils::md5Hex),
    MD5_sess(algortihmMD5, DigestUtils::md5Hex)
    ;
    public final String algorithmName;
    public final Function<String, String> digest;

    private DigestAlgorithm(String algorithmName, Function<String, String> digest) {
        this.algorithmName = algorithmName ;
        this.digest = digest;
    }

    /** Algothm name to enum, or null (unecognized) */
    public static DigestAlgorithm fromString(String name) {
        return switch(name) {
            case algortihmSHA256 -> SHA_256;
            case algortihmSHA256_sess -> SHA_256_sess;
            case algortihmSHA512_256 -> SHA_512_256;
            case algortihmSHA512_256_sess -> SHA_512_256_sess;
            case algortihmMD5 -> MD5;
            case algortihmMD5_sess -> MD5_sess;
            default -> null;
        };
    }
}
