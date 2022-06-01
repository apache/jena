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

import static org.apache.jena.http.auth.DigestLib.digestAuthModifier;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.util.List;

import org.apache.jena.atlas.web.AuthScheme;
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

    /* Handle a 401 (authentication challenge). */
    private static <T> HttpResponse<T> handle401(HttpClient httpClient,
                                                 HttpRequest request,
                                                 BodyHandler<T> bodyHandler,
                                                 HttpResponse<T> httpResponse401) {
        AuthChallenge aHeader = wwwAuthenticateHeader(httpResponse401);
        if ( aHeader == null )
            // No valid header - simply return the original response.
            return httpResponse401;

        // Currently on a URI endpoint-by-endpoint basis.
        // String realm = aHeader.getRealm();
        // AuthDomain domain = new AuthDomain(request.uri(), realm);

        PasswordRecord passwordRecord = null;
        if ( aHeader.authScheme == AuthScheme.BASIC || aHeader.authScheme == AuthScheme.DIGEST ) {
            passwordRecord = AuthEnv.get().getUsernamePassword(request.uri());
            if ( passwordRecord == null )
                // No entry.
                throw new HttpException(HttpSC.UNAUTHORIZED_401);
        }

        // Request target - no query string.
        AuthRequestModifier authRequestModifier;
        switch (aHeader.authScheme) {
            case BASIC :
                authRequestModifier = basicAuthModifier(passwordRecord.getUsername(), passwordRecord.getPassword());
                break;
            case DIGEST : {
                String requestTarget = HttpLib.requestTargetServer(request.uri());
                authRequestModifier = digestAuthModifier(aHeader, passwordRecord.getUsername(), passwordRecord.getPassword(),
                                                         request.method(), requestTarget);
                break;
            }
            case BEARER : {
                // Challenge
                String requestTarget = HttpLib.endpoint(request.uri().toString());
                authRequestModifier = bearerAuthModifier(requestTarget, aHeader);
                break;
            }
            case UNKNOWN :
                // Not handled. Pass back the 401.
                return httpResponse401;
            default:
                throw new HttpException("Not an authentication scheme -- "+aHeader.authScheme);
        }

        // Failed to generate a request modifier for a retry.
        if ( authRequestModifier == null)
            return httpResponse401;

        // ---- Register for next time the app calls this URI.
        AuthEnv.get().registerAuthModifier(request.uri().toString(), authRequestModifier);

        // ---- Call with modified request
        HttpRequest.Builder request2builder = HttpLib.createBuilder(request);
        request2builder = authRequestModifier.addAuth(request2builder);

        HttpRequest httpRequest2 = request2builder.build();
        HttpResponse<T> httpResponse2 = HttpLib.executeJDK(httpClient, httpRequest2, bodyHandler);
        // Pass back to application regardless of response code.
        return httpResponse2;
    }

    /**
     * Choose the first Digest auth header, else first Basic auth header.
     */
    private static AuthChallenge wwwAuthenticateHeader(HttpResponse<?> httpResponse) {
        List<String> headers = httpResponse.headers().allValues("WWW-Authenticate");
        if ( headers.size() == 0 )
            return null;
        // Choose first digest or bearer, else the first basic. Prefer digest or bearer to basic.
        AuthChallenge aHeader = null;
        String result = null;
        for ( String headerValue : headers ) {
            AuthChallenge aHeader2 = AuthChallenge.parse(headerValue);
            if ( aHeader2 == null ) {
                AuthEnv.LOG.warn("Bad authentication response - ignored: "+headerValue);
                return null;
            }
            AuthScheme authScheme = aHeader2.authScheme;
            switch(authScheme) {
                case  DIGEST :
                    return aHeader2;
                case BASIC:
                    if ( aHeader == null )
                        // Choose first Basic auth for now - there may also be a Digest.
                        aHeader = aHeader2;
                    break;
                case BEARER:
                    return aHeader2;
                case UNKNOWN:
                    AuthEnv.LOG.warn("Authentication required: "+authScheme);
                    break;
                default:
                    AuthEnv.LOG.warn("Unrecogized authentication response - ignored: "+headerValue);
                    break;
            }
        }
        return aHeader;
    }

    /**
     * Create an {@link AuthRequestModifier} that applies a user/password for basic auth.
     */
    /*package*/ static AuthRequestModifier basicAuthModifier(String user, String password) {
        return req->req.setHeader(HttpNames.hAuthorization, HttpLib.basicAuth(user, password));
    }


    /**
     * Create an {@link AuthRequestModifier} that supplies the token bearer auth.
     * Return null for reject challenge (401 returned to application).
     */
    private static AuthRequestModifier bearerAuthModifier(String remoteService, AuthChallenge aHeader) {
        String token = AuthEnv.get().getBearerToken(remoteService, aHeader);
        if ( token == null )
            return null;
        if ( token.contains(" ") ) {
            throw new AuthException("Bad token - contains spaces");
        }
        return builder->builder.setHeader(HttpNames.hAuthorization, "Bearer "+token);
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
