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

package org.apache.jena.rdflink.dataset.assembler;

import java.net.Authenticator;
import java.net.http.HttpClient;
import java.util.Arrays;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.exceptions.AssemblerException;
import org.apache.jena.atlas.lib.Creator;
import org.apache.jena.http.HttpEnv;
import org.apache.jena.http.auth.AuthLib;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.rdflink.RDFLinkHTTP;
import org.apache.jena.rdflink.dataset.DatasetGraphOverRDFLink;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.assembler.AssemblerUtils;
import org.apache.jena.sparql.core.assembler.DatasetAssembler;
import org.apache.jena.sparql.util.graph.GraphUtils;
import org.apache.jena.sys.JenaSystem;

public class DatasetAssemblerHTTP extends DatasetAssembler
{
    static { JenaSystem.init(); }

    @Override
    public DatasetGraph createDataset(Assembler a, Resource root) {
        return make(a, root);
    }

    public static DatasetGraph make(Assembler a, Resource root) {
        String destination = GraphUtils.getStringValue(root, VocabAssemblerHTTP.pDestination);

        String queryEndpoint = destination;
        String updateEndpoint = destination;
        String gspEndpoint = destination;

        queryEndpoint  = GraphUtils.getStringValue(root, VocabAssemblerHTTP.pQueryEndpoint);
        updateEndpoint = GraphUtils.getStringValue(root, VocabAssemblerHTTP.pUpdateEndpoint);
        gspEndpoint    = GraphUtils.getStringValue(root, VocabAssemblerHTTP.pGspEndpoint);

        String q = queryEndpoint;
        String u = updateEndpoint;
        String g = gspEndpoint;

        if (q == null && u == null && g == null) {
            throw new AssemblerException(root, "No destination set using any of the properties: " +
                Arrays.asList(VocabAssemblerHTTP.pDestination, VocabAssemblerHTTP.pQueryEndpoint, VocabAssemblerHTTP.pUpdateEndpoint, VocabAssemblerHTTP.pGspEndpoint));
        }

        String user = GraphUtils.getStringValue(root, VocabAssemblerHTTP.pUser);
        String pass = GraphUtils.getStringValue(root, VocabAssemblerHTTP.pPass);

        if ((user != null && pass == null)) {
            throw new AssemblerException(root, "HTTP Credentials: Password is null.");
        }

        if ((user == null && pass != null)) {
            throw new AssemblerException(root, "HTTP Credentials: User is null.");
        }

        HttpClient httpClient = null;
        if (user != null || pass != null) {
            Authenticator auth = AuthLib.authenticator(user, pass);
            httpClient = HttpEnv.httpClientBuilder().authenticator(auth).build();
        }

        HttpClient h = httpClient;

        Creator<RDFLink> linkCreator = () -> {
            RDFLink link = RDFLinkHTTP.newBuilder()
                .queryEndpoint(q)
                .updateEndpoint(u)
                .gspEndpoint(g)
                .httpClient(h)
                .build();
            return link;
        };

        DatasetGraph dsg = new DatasetGraphOverRDFLink(linkCreator);

        /*
        <r> rdf:type tdb:DatasetTDB2;
            tdb:location "dir";
            //ja:context [ ja:cxtName "arq:queryTimeout";  ja:cxtValue "10000" ] ;
            tdb:unionGraph true; # or "true"
        */
        AssemblerUtils.mergeContext(root, dsg.getContext());
        return dsg;
    }
}
