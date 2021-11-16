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

package arq.examples;

import java.util.Objects;

import org.apache.jena.atlas.web.AuthScheme;
import org.apache.jena.fuseki.auth.Auth;
import org.apache.jena.fuseki.jetty.JettyLib;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.sparql.core.DatasetGraph;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.UserStore;

/** Support code for examples */
public class ExamplesServer {

    // Plain server
    public static FusekiServer startServer(String dsName, DatasetGraph dsg, boolean verbose) {
        FusekiServer server = FusekiServer.create()
                .port(0)
                .loopback(true)
                .verbose(verbose)
                .enablePing(true)
                .add(dsName, dsg)
                .build();
            server.start();
            return server;
    }


    // Server with users and password.
    public static FusekiServer startServerWithAuth(String dsName, DatasetGraph dsg, boolean verbose, String user, String password) {
        Objects.requireNonNull(user);
        Objects.requireNonNull(password);

        UserStore userStore = JettyLib.makeUserStore(user, password);
        SecurityHandler sh = JettyLib.makeSecurityHandler("Fuseki",  userStore, AuthScheme.BASIC);

        FusekiServer server = FusekiServer.create()
            .port(0)
            .loopback(true)
            .verbose(verbose)
            .enablePing(true)
            .securityHandler(sh)
            // Only this user can make requests.
            .serverAuthPolicy(Auth.policyAllowSpecific(user))
            .add(dsName, dsg)
            .build();
        server.start();
        return server;
    }

}
