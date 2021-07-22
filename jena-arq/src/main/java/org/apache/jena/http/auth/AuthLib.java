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

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.util.List;

import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.http.HttpLib;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.web.HttpSC;

public class AuthLib {

    /**
     * Call {@link HttpClient#send} after applying an active {@link AuthRequestModifier}
     * to modify the {@link java.net.http.HttpRequest.Builder}.
     * If no {@link AuthRequestModifier} is available and if a 401 response is received,
     * setup a {@link AuthRequestModifier} passed on registered username and password information.
     * This function supports basic and digest authentication.
     *
     * @param httpClient HttpClient
     * @param httpRequest
     * @param bodyHandler
     * @return HttpResponse<T>
     */
    public static <T> HttpResponse<T> authExecute(HttpClient httpClient, HttpRequest httpRequest, BodyHandler<T> bodyHandler) {
        HttpResponse<T> httpResponse = HttpLib.executeJDK(httpClient, httpRequest, bodyHandler);

        // -- 401 handling.
        if ( httpResponse.statusCode() != 401 )
            return httpResponse;
        HttpResponse<T> httpResponse2 = handle401(httpClient, httpRequest, bodyHandler, httpResponse);
        return httpResponse2;
    }

    /**
     * Choose the first Digest auth header, else first Basic auth header.
     */
    private static AuthChallenge authenticateHeader(HttpResponse<?> httpResponse) {
        List<String> headers = httpResponse.headers().allValues("WWW-Authenticate");
        if ( headers.size() == 0 )
            return null;
        // Choose first digest or first basic.
        AuthChallenge aHeader = null;
        String result = null;
        for ( String headerValue : headers ) {
            AuthChallenge aHeader2 = AuthChallenge.parse(headerValue);
            if ( aHeader2 == null )
                AuthEnv.LOG.warn("Bad authentication response - ignored: "+headerValue);
            // Prefer Digest
            switch(aHeader2.authScheme) {
                case  DIGEST :
                    return aHeader2;
                case BASIC:
                    if ( aHeader == null )
                        // Choose first Basic auth for now - there may also be a Digest.
                        aHeader = aHeader2;
                    break;
                default:
                    AuthEnv.LOG.warn("Unrecogized authentication response - ignored: "+headerValue);
            }
        }
        return aHeader;
    }

    /* Handle a 401 (Basic and Digest) authentication challenge. */
    private static <T> HttpResponse<T> handle401(HttpClient httpClient,
                                                 HttpRequest request,
                                                 BodyHandler<T> bodyHandler,
                                                 HttpResponse<T> httpResponse1) {
        AuthChallenge aHeader = authenticateHeader(httpResponse1);
        if ( aHeader == null )
            // No valid header - simply return the original response.
            return httpResponse1;

        AuthDomain domain = new AuthDomain(request.uri());
        PasswordRecord passwordRecord = AuthEnv.get().getUsernamePassword(request.uri());
        if ( passwordRecord == null )
            // No entry.
            throw new HttpException(HttpSC.UNAUTHORIZED_401);

        AuthRequestModifier digestAuthModifier;
        switch (aHeader.authScheme) {
            case BASIC :
                digestAuthModifier = req->req.setHeader(HttpNames.hAuthorization, HttpLib.basicAuth(passwordRecord.getUsername(), passwordRecord.getPassword()));
                break;
            case DIGEST : {
                String requestTarget = HttpLib.requestTarget(request.uri());
                digestAuthModifier = DigestLib.buildDigest(aHeader,
                                                           passwordRecord.getUsername(), passwordRecord.getPassword(),
                                                           requestTarget, request.method());
                break;
            }
            default:
                throw new HttpException("Not an authentication scheme -- "+aHeader.authScheme);
        }
        String endpointURL = HttpLib.requestTarget(request.uri());
        AuthEnv.get().registerAuthModifier(endpointURL, digestAuthModifier);

        // ---- Call with modifier or fail.
        HttpRequest.Builder request2builder = HttpLib.createBuilder(request);
        request2builder = digestAuthModifier.addAuth(request2builder);
        // Try once more.
        HttpRequest httpRequest2 = request2builder.build();
        HttpResponse<T> httpResponse2 = HttpLib.executeJDK(httpClient, httpRequest2, bodyHandler);
        return httpResponse2;
    }

    /**
     * Create a JDK {@link Authenticator} for this (username and password).
     * The java.net.http as supplied only supports basic authentication.
     * */
    public static Authenticator authenticator(String user, String password) {
        return new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password.toCharArray());
            }
        };
    }

    /** Get the {@link PasswordAuthentication} from an {@link Authenticator} */
    public static PasswordAuthentication getPasswordAuthentication(Authenticator authenticator) {
        return authenticator.requestPasswordAuthenticationInstance(null, null, -1, null, null, null, null, null);
    }
}
