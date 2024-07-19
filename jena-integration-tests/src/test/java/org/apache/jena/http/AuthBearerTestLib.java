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

package org.apache.jena.http;

import static org.apache.jena.http.auth.AuthLib.base64dec;
import static org.apache.jena.http.auth.AuthLib.base64enc;
import static org.junit.Assert.fail;

import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.http.auth.AuthEnv;
import org.apache.jena.http.auth.AuthRequestModifier;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.sparql.exec.http.QueryExecHTTP;
import org.slf4j.Logger;

public class AuthBearerTestLib {

    enum Expect { SUCCESS, REJECT }

    private static Logger log = Fuseki.serverLog;

    static void addAuthModifierBearerToken(String endpoint, String token) {
        String headerValue = HttpLib.bearerAuthHeader(token);
        AuthRequestModifier requestModifier = builder->builder.setHeader(HttpNames.hAuthorization, headerValue);
        AuthEnv.get().registerAuthModifier(endpoint, requestModifier);
    }

    /**
     * Extract the "sub" field from an encoded bearerToken, or return null.
     * This method does not verify the token.
     */
    static String subjectFromEncodedJWT(String token) {
        try {
            String[] parts = token.split("\\.");
            if ( parts.length != 3 ) {
                log.error("Bad token: '"+token+"'");
                return null;
            }
            String jsonStr = base64dec(parts[1]);
            JsonObject obj = new Gson().fromJson(jsonStr, JsonObject.class);
            JsonElement field = obj.get("sub");
            if ( field == null ) {
                log.error("Bad token: no \"sub\" '"+jsonStr+"'");
                return null;
            }
            if ( ! field.isJsonPrimitive() ) {
                log.error("Bad token: \"sub\" is not a string'"+jsonStr+"'");
            }
            String subject = field.getAsString();
            return subject;
        } catch (IllegalArgumentException|JsonSyntaxException ex) {
            return null;
        } catch (RuntimeException ex) {
            return null;
        }
    }

    /** Test token */
    static String generateTestJWT(String user) {
        Objects.requireNonNull(user);
        String header = "{\"alg\":\"HS256\",\"typ\":\"bearerToken\"}".trim();
        String body = "{ \"iss\": \"SELF\", \"exp\": \"never\", \"sub\": \"SUBJECT\" }".replace("SUBJECT", user);
        String token = base64enc(header)+"."+base64enc(body)+"."+base64enc("HASH");
        return token;
    }

    static void attempt(String URL, String bearerToken, Expect expected) {
        if ( bearerToken != null )
            AuthEnv.get().setBearerToken(URL, bearerToken);
        try {
            attempt(URL, expected);
        } finally {
            if ( bearerToken != null )
                AuthEnv.get().setBearerToken(URL, null);
        }
    }

    static void attemptBasic(String URL, String username, String password, Expect expected) {
        try {
            String basicAuth = "Basic "+base64enc(username+":"+password);

            boolean b = QueryExecHTTP.service(URL)
                    .httpHeader(HttpNames.hAuthorization, basicAuth)
                    .query("ASK{}")
                    .ask();
            if ( expected == Expect.REJECT )
                fail("Expected the operation to be rejected");
        } catch (RuntimeException ex) {
            if ( expected == Expect.SUCCESS )
                fail("Expected the operation to succeed");
        }
    }

    static void attempt(String URL, Expect expected) {
        try {
            boolean b = QueryExecHTTP.service(URL)
                    .query("ASK{}")
                    .ask();
            if ( expected == Expect.REJECT )
                fail("Expected the operation to be rejected");
        } catch (RuntimeException ex) {
            if ( expected == Expect.SUCCESS )
                fail("Expected the operation to succeed: "+ex.getMessage());
        }
    }
}
