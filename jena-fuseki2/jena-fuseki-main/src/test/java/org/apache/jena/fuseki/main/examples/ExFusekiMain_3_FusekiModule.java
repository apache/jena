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

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.sys.FusekiModule;
import org.apache.jena.fuseki.main.sys.FusekiModules;
import org.apache.jena.fuseki.server.DataAccessPointRegistry;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.http.HttpEnv;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.web.HttpSC;

public class ExFusekiMain_3_FusekiModule {

    public static void main(String...a) throws Exception {
        JenaSystem.init();
        FusekiLogging.setLogging();

        // Normally done with ServiceLoader
        // A file /META-INF/services/org.apache.jena.fuseki.main.sys.FusekiModule
        // in the jar file with contents:
        //    org.apache.jena.fuseki.main.examples.ExampleModule
        //
        // The file is typically put into the jar by having
        //   src/main/resources/META-INF/services/org.apache.jena.fuseki.main.sys.FusekiModule
        FusekiModule module = new FMod_ProvidePATCH();
        FusekiModules.add(module);

        // Create server.
        FusekiServer server =
            FusekiServer.create()
                .port(0)
                .build()
                .start();
        int port = server.getPort();

        // Client HTTP request: "PATCH /extra"
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:"+port+"/extra"))
                .method("PATCH", BodyPublishers.ofString("hello world!"))
                .build();
        HttpResponse<Void> response = HttpEnv.getDftHttpClient().send(request, BodyHandlers.discarding());
        server.stop();
    }

    static class FMod_ProvidePATCH implements FusekiModule {

        private String modName = UUID.randomUUID().toString();
        @Override
        public String name() {
            return modName;
        }

        @Override public void configuration(FusekiServer.Builder builder, DataAccessPointRegistry dapRegistry, Model configModel) {
            System.out.println("Module adds servlet");
            HttpServlet servlet = new HttpServlet() {
                @Override public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
                    if ( req.getMethod().equalsIgnoreCase("PATCH") ) {
                        doPatch(req, res);
                        return ;
                    }
                    super.service(req, res);
                }

                private void doPatch(HttpServletRequest req, HttpServletResponse res) throws IOException {
                    String x = IO.readWholeFileAsUTF8(req.getInputStream());
                    System.out.println("HTTP PATCH: "+x);
                    res.setStatus(HttpSC.OK_200);
                }
            };

            builder.addServlet("/extra", servlet);
        }

        @Override public void serverAfterStarting(FusekiServer server) {
            System.out.println("Customized server start on port "+server.getHttpPort());
        }
    }
}

