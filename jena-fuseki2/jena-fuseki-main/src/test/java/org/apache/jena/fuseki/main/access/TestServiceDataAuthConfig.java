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

package org.apache.jena.fuseki.main.access;

import org.apache.jena.atlas.web.WebLib;
import org.apache.jena.fuseki.auth.Auth;
import org.apache.jena.fuseki.auth.AuthPolicy;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.fuseki.server.Endpoint;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.junit.BeforeClass;

/**
 * AbstractTestServiceDatasetAuth with a programtically built server which should be
 * the same as the {@link TestServiceDataAuthConfig config file version}.
 */
public class TestServiceDataAuthConfig extends AbstractTestServiceDatasetAuth {
    @BeforeClass public static void beforeClass () {
        port = WebLib.choosePort();
        server = build(port, null);
        server.start();
    }        
        
    public static FusekiServer build(int port, AuthPolicy policy) { 

        AuthPolicy policy12 = Auth.policyAllowSpecific("user1", "user2");
        AuthPolicy policy13 = Auth.policyAllowSpecific("user1", "user3");
        
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        DataService dSrv = new DataService(dsg);
        dSrv.addEndpoint(new Endpoint(Operation.Query, null, policy12));
        dSrv.addEndpoint(new Endpoint(Operation.Update, null, policy13));
        FusekiServer server = FusekiServer.create()
            //.verbose(true)
            .port(port)
            .passwordFile("testing/Access/passwd")
            //.serverAuthPolicy(policy)
            .add("/db", dSrv)
            .build();
        return server;
    }
}
