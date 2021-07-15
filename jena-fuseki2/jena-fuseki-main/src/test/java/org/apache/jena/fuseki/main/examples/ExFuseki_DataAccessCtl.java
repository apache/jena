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

package org.apache.jena.fuseki.main.examples;

import static java.lang.String.format;
import static org.apache.jena.fuseki.main.examples.ExamplesLib.httpClient;

import java.net.http.HttpClient;

import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.atlas.web.WebLib;
import org.apache.jena.fuseki.access.AuthorizationService;
import org.apache.jena.fuseki.access.DataAccessCtl;
import org.apache.jena.fuseki.access.SecurityContextView;
import org.apache.jena.fuseki.access.SecurityRegistry;
import org.apache.jena.fuseki.jetty.JettyLib;
import org.apache.jena.fuseki.main.FusekiLib;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.DatabaseMgr;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.UserStore;
import org.eclipse.jetty.util.security.Credential;
import org.eclipse.jetty.util.security.Password;

/**
 * Example of a Fuseki with per-graph data access control.
 */
public class ExFuseki_DataAccessCtl {

    public static void main(String ... a) {
        FusekiLogging.setLogging();
        int port = WebLib.choosePort();
        String datasetName = "/ds";
        String URL = format("http://localhost:%d/%s", port, datasetName);

        // ---- Set up the registry.
        AuthorizationService authorizeSvc;
        {
            SecurityRegistry reg = new SecurityRegistry();
            // user1 can see the default graph and :g1
            reg.put("user1", new SecurityContextView("http://example/g1", Quad.defaultGraphIRI.getURI()));
            // user2 can see :g1
            reg.put("user2", new SecurityContextView("http://example/g1"));
            // user3 can see :g1 and :g2
            reg.put("user3", new SecurityContextView("http://example/g1", "http://example/g2"));
            // Hide implementation.
            authorizeSvc = reg;
        }

        // ---- Some data
        DatasetGraph dsg = createData();
        // ---- User authentication database (Jetty specific)
        UserStore userStore = new UserStore();
        addUserPassword(userStore, "user1", "pw1", "**");
        addUserPassword(userStore, "user2", "pw2", "**");
        try { userStore.start(); }
        catch (Exception ex) { throw new RuntimeException("UserStore", ex); }

        // ---- Build server, start server.
        FusekiServer server = fuseki(port, userStore, authorizeSvc, datasetName, dsg);
        server.start();

        // ---- HttpClient connection with user and password basic authentication.
        HttpClient client = httpClient("user1", "pw1");

        // ---- Use it.
        try (RDFConnection conn = RDFConnectionRemote.newBuilder()
            .destination(URL)
            .httpClient(client)
            .build()){

            // What can we see of the database? user1 can see g1 and the default graph
            Dataset ds1 = conn.fetchDataset();
            RDFDataMgr.write(System.out, ds1, RDFFormat.TRIG_FLAT);

            // Get a graph.
            Model m1 = conn.fetch("http://example/g1");
            RDFDataMgr.write(System.out, m1, RDFFormat.TURTLE_FLAT);

            // Get a graph. user tries to get a graph they have no permission for ==> 404
            try {
                Model m2 = conn.fetch("http://example/g2");
                } catch (HttpException ex) {
                }
        }
        // Need to exit the JVM : there is a background server
        System.exit(0);
    }

    /** Create data : the subject indicates whic it comes from */
    private static DatasetGraph createData() {
        DatasetGraph dsg = DatabaseMgr.createDatasetGraph();
        Txn.executeWrite(dsg, () -> {
            // Dft graph
            dsg.add(SSE.parseQuad("(_ :s0 :p :o)"));
            // Named graphs
            for ( int i = 0 ; i < 5 ; i++ ) {
                dsg.add(SSE.parseQuad(format("(:g%d :s%d :p :o)", i, i)));
            }
            RDFDataMgr.write(System.out, dsg, RDFFormat.TRIG_FLAT);
        });
        return dsg;
    }

    private static void addUserPassword(UserStore userStore, String user, String password, String role) {
        String[] roles = role == null ? null : new String[]{role};
        Credential cred  = new Password(password);
        userStore.addUser(user, cred, roles);
    }

    /** Create a Fuseki server with:
     * <ul>
     * <li>port, dataset and name
     * <li>user/password for Jetty basic authentication
     * <li>Authorization service
     * </ul>
     */
    private static FusekiServer fuseki(int port, UserStore userStore, AuthorizationService authorizeSvc, String dsName, DatasetGraph dsgBase) {
        // Associate access control information with the dataset.
        DatasetGraph dsx = DataAccessCtl.controlledDataset(dsgBase, authorizeSvc);
        // Build a Fuseki server with the access control operations replacing the normal (no control) operations.

        FusekiServer.Builder builder = FusekiLib.fusekiBuilderAccessCtl(DataAccessCtl.requestUserServlet)
            .port(port)
            .add(dsName, dsx, false);
        // Add service endpoint login for authentication.
        ConstraintSecurityHandler sh = null;
        if ( userStore != null ) {
            sh = JettyLib.makeSecurityHandler("Dataset:"+dsName, userStore);
            JettyLib.addPathConstraint(sh, dsName);
            builder.securityHandler(sh);
        }
        return builder.build();
    }
}
