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

package org.apache.jena.rdfconnection;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.http.HttpClient;
import java.util.function.Consumer;

import org.apache.jena.http.HttpEnv;
import org.apache.jena.http.auth.AuthDomain;
import org.apache.jena.http.auth.AuthEnv;
import org.apache.jena.web.AuthSetup;

public class LibSec {
    /** Construct an {@link Authenticator} to hold user and password */
    public static Authenticator authenticator(String user, String password) {
        return new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password.toCharArray());
            }
        };
    }

    public static HttpClient httpClient(AuthSetup authSetup) {
        Authenticator a = authenticator(authSetup.user, authSetup.password);
        return HttpEnv.httpClientBuilder().authenticator(a).build();
    }

    public static void withAuth(String urlStr, AuthSetup auth, Consumer<RDFConnection> action) {
        // Prefix
        URI urix = URI.create(urlStr);
        //String requestTarget = HttpLib.requestTarget(urix);
        AuthDomain domain = new AuthDomain(urix, null);
        try {
            AuthEnv.get().registerUsernamePassword(urix, auth.user, auth.password);
            try ( RDFConnection conn = RDFConnectionRemote.newBuilder().destination(urlStr).build() ) {
                action.accept(conn);
            }
        } finally {
            AuthEnv.get().unregisterUsernamePassword(urix);
        }
    }
}
