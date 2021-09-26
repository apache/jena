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

import static org.apache.jena.http.auth.RFC2617.A1_MD5;
import static org.apache.jena.http.auth.RFC2617.A2_auth;
import static org.apache.jena.http.auth.RFC2617.H;
import static org.apache.jena.http.auth.RFC2617.KD;

import java.net.Authenticator;
import java.net.Authenticator.RequestorType;
import java.net.PasswordAuthentication;
import java.net.http.HttpClient;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.riot.web.HttpNames;

public class DigestLib {

    /** From the challenge, username and password, calculate the response.field. */
    public static String calcDigestChallengeResponse(AuthChallenge auth,
                                                     String username, String password,
                                                     String method, String requestTarget,
                                                     String cnonce, String nc, String authType) {
        String a1 = A1_MD5(username, auth.realm, password) ;
        if ( auth.qop == null ) {
            // RFC 2069
            // Firefox seems to prefer this form??
            return KD(H(a1), auth.nonce+":"+H(A2_auth(method, requestTarget))) ;
        }
        else {
            Objects.nonNull(cnonce) ;
            Objects.nonNull(nc) ;
            return KD(H(a1),
                      auth.nonce+":"+nc+":"+cnonce+":"+authType+":"+H(A2_auth(method, requestTarget))
                    ) ;
        }
    }

    private static Random nonceGenerator = new SecureRandom();

    /**
     * Generate a nonce for the client to use in a digest auth session. Each call of
     * this static method returns a new string (to within the limits of random number
     * generation).
     */
    public static String generateNonce() {
        return String.format("%08X",nonceGenerator.nextLong());
    }

    /** Extract user and password from a {@link HttpClient}. */
    private static Pair<String, String> getUserNameAndPassword(HttpClient httpClient) {
        Optional<Authenticator> optAuth = httpClient.authenticator();
        if ( optAuth.isEmpty() )
            throw new HttpException("Username/password required but not present in HttpClient");
        // We just want the PasswordAuthentication!
        PasswordAuthentication x = optAuth.get().requestPasswordAuthenticationInstance(null,
                                                                                       null,
                                                                                       -1,   //port,
                                                                                       null, //protocol,
                                                                                       null, //prompt,
                                                                                       null, //scheme,
                                                                                       null, //url,
                                                                                       RequestorType.SERVER);
        String user = x.getUserName();
        String password = new String(x.getPassword());
        return Pair.create(user, password);
    }

    /**
     * Function to modify a {@link java.net.http.HttpRequest.Builder} for digest authentication.
     * One instance of this function is used for each digest session.
     */
    public static AuthRequestModifier buildDigest(AuthChallenge aHeader, String user, String password, String method, String requestTarget) {
        String clientNonce = DigestLib.generateNonce();
        AtomicLong ncCounter = new AtomicLong(0);
        return req->{
            // Bump nc
            String nc = String.format("%08X", ncCounter.getAndIncrement());
            String responseField =
                    DigestLib.calcDigestChallengeResponse(aHeader, user, password,
                                                          method, requestTarget,
                                                          clientNonce, nc, "auth");
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Digest ");
            field(stringBuilder, true, "username", user, true);
            field(stringBuilder, false, "realm", aHeader.realm, true);
            field(stringBuilder, false, "nonce", aHeader.nonce, true);
            field(stringBuilder, false, "uri", requestTarget, true);
            field(stringBuilder, false, "qop", "auth", false);
            field(stringBuilder, false, "cnonce", clientNonce, true);
            field(stringBuilder, false, "nc", nc, false);
            field(stringBuilder, false, "response", responseField, true);
            field(stringBuilder, false, "opaque", aHeader.opaque, true);
            String x = stringBuilder.toString();
            // setHeader - replace previous
            req.setHeader(HttpNames.hAuthorization , x);
            return req;
        };
    }

    private static void field(StringBuilder stringBuilder, boolean first, String name, String value, boolean quotes) {
        if ( value == null )
            return;
        if ( ! first )
            stringBuilder.append(", ");
        stringBuilder.append(name);
        stringBuilder.append("=");
        if ( quotes )
            stringBuilder.append('"');
        stringBuilder.append(value);
        if ( quotes )
            stringBuilder.append('"');
    }
}
