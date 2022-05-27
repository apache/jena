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

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.http.auth.AuthEnv;
import org.apache.jena.http.auth.AuthRequestModifier;
import org.apache.jena.riot.web.HttpNames;
import org.slf4j.Logger;

public class AuthBearerTestLib {

    private static Logger log = Fuseki.serverLog;
    private static java.util.Base64.Encoder encoder = Base64.getUrlEncoder();
    private static java.util.Base64.Decoder decoder = Base64.getUrlDecoder();

    /**
     * Extract the "sub" field from an encoded JWT, or return null.
     * This method does not verify the token.
     */
    public static String subjectFromEncodedJWT(String token) {
        try {
            String[] parts = token.split("\\.");
            if ( parts.length != 3 ) {
                log.warn("Bad token: '"+token+"'");
                return null;
            }
            byte[] jsonBytes = decoder.decode(parts[1]);
            String jsonStr = new String(jsonBytes, StandardCharsets.UTF_8);
            JsonObject obj = new Gson().fromJson(jsonStr, JsonObject.class);
            JsonElement field = obj.get("sub");
            if ( field == null ) {
                log.warn("Bad token: no \"sub\" '"+jsonStr+"'");
                return null;
            }
            if ( ! field.isJsonPrimitive() ) {
                log.warn("Bad token: \"sub\" is not a string'"+jsonStr+"'");
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
    public static String generateTestToken(String user) {
        Objects.requireNonNull(user);
        String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}".trim();
        String body = "{ \"iss\": \"SELF\", \"exp\": \"never\", \"sub\": \"SUBJECT\" }".trim().replace("SUBJECT", user);
        String token = enc64(header)+"."+enc64(body)+"."+enc64("HASH");
        return token;
    }

    private static String enc64(String x) {
        byte[] bytes = x.getBytes(StandardCharsets.UTF_8);
        byte[] encoded = encoder.encode(bytes);
        return new String(encoded, StandardCharsets.UTF_8);
    }

    public static void addAuthModifierBearerToken(String endpoint, String token) {
        AuthRequestModifier requestModifier = builder->builder.setHeader(HttpNames.hAuthorization, "Bearer "+token);
        AuthEnv.get().registerAuthModifier(endpoint, requestModifier);
    }
}
