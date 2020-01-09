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

package org.apache.jena.fuseki.main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.fuseki.access.AccessCtl_AllowGET;
import org.apache.jena.fuseki.access.AccessCtl_Deny;
import org.apache.jena.fuseki.access.AccessCtl_GSP_R;
import org.apache.jena.fuseki.access.AccessCtl_SPARQL_QueryDataset;
import org.apache.jena.fuseki.build.FusekiConfig;
import org.apache.jena.fuseki.server.DataAccessPointRegistry;
import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.fuseki.server.Endpoint;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.fuseki.servlets.ActionService;
import org.apache.jena.fuseki.servlets.GSP_RW;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.core.DatasetGraph;

/** Actions on and about a {@link FusekiServer} */
public class FusekiLib {
    /**
     * Return a collection of the names registered. This collection does not change as the
     * server changes.
     */
    public static Collection<String> names(FusekiServer server) {
        DataAccessPointRegistry dataAccessPoints = DataAccessPointRegistry.get(server.getServletContext());
        int N = dataAccessPoints.size();
        Stream<String> stream = DataAccessPointRegistry.get(server.getServletContext()).keys().stream();
        // Correct size, no reallocate.
        List<String> names = stream.collect(Collectors.toCollection(() -> new ArrayList<>(N)));
        return names;
    }

    /** Add a dataset to a server */
    public static FusekiServer addDataset(FusekiServer server, String name, DatasetGraph dsg, boolean withUpdate) {
        DataAccessPointRegistry dataAccessPoints = DataAccessPointRegistry.get(server.getServletContext());
        FusekiConfig.addDataset(dataAccessPoints, name, dsg, withUpdate);
        return server;
    }

    /** Add a {@link DataService} to a server */
    public static FusekiServer addDataset(FusekiServer server, String name, DataService dataService) {
        DataAccessPointRegistry dataAccessPoints = DataAccessPointRegistry.get(server.getServletContext());
        FusekiConfig.addDataService(dataAccessPoints, name, dataService);
        return server;
    }

    /** Remove dataset from a server */
    public static FusekiServer removeDataset(FusekiServer server, String name) {
        DataAccessPointRegistry dataAccessPoints = DataAccessPointRegistry.get(server.getServletContext());
        FusekiConfig.removeDataset(dataAccessPoints, name);
        return server;
    }

    /**
     * Return a {@code FusekiServer.Builder} setup for data access control.
     */
    public static FusekiServer.Builder fusekiBuilderAccessCtl(Function<HttpAction, String> determineUser) {
        FusekiServer.Builder builder = FusekiServer.create();
        return fusekiBuilderAccessCtl(builder, determineUser);
    }

    /**
     * Modify a {@code FusekiServer.Builder} setup for data access control.
     */
    public static FusekiServer.Builder fusekiBuilderAccessCtl(FusekiServer.Builder builder, Function<HttpAction, String> determineUser) {
        // Replace the standard operations with the SecurityRegistry processing ones.
        builder.registerOperation(Operation.Query, WebContent.contentTypeSPARQLQuery, new AccessCtl_SPARQL_QueryDataset(determineUser));
        builder.registerOperation(Operation.GSP_R, new AccessCtl_GSP_R(determineUser));

        // Block updates (can just not register these operations).
        builder.registerOperation(Operation.Update, WebContent.contentTypeSPARQLUpdate, new AccessCtl_Deny("Update"));
        builder.registerOperation(Operation.GSP_RW, new AccessCtl_AllowGET(new GSP_RW(), "GSP Write"));
        builder.registerOperation(Operation.GSP_RW, new AccessCtl_GSP_R(determineUser));
        return builder;
    }

    /**
     * Modify in-place existing {@link Endpoint Endpoints} so that the read-operations for
     * query/GSP/Quads go to the data-filtering versions of the {@link ActionService ActionServices}.
     */
    public static void modifyForAccessCtl(DataAccessPointRegistry dapRegistry, Function<HttpAction, String> determineUser) {
        dapRegistry.forEach((name, dap) -> {
            dap.getDataService().forEachEndpoint(ep->{
                Operation op = ep.getOperation();
                modifyForAccessCtl(ep, determineUser);
            });
        });
    }

    /**
     * Modify in-place existing an {@link Endpoint} so that the read-operations for
     * query/GSP/Quads go to the data-filtering versions of the {@link ActionService ActionServices}.
     * Any other operations are replaced with "access denied".
     */
    public static void modifyForAccessCtl(Endpoint endpoint, Function<HttpAction, String> determineUser) {
        endpoint.setProcessor( controlledProc(endpoint.getOperation(), determineUser));
    }

    private static ActionService controlledProc(Operation op, Function<HttpAction, String> determineUser) {
        if ( Operation.Query.equals(op) )
            return new AccessCtl_SPARQL_QueryDataset(determineUser);
       if ( Operation.GSP_R.equals(op) )
           return new AccessCtl_GSP_R(determineUser);
       if ( Operation.GSP_RW.equals(op) )
           return new AccessCtl_GSP_R(determineUser);
       return new AccessCtl_Deny("Not supported for graph level access control: "+op.getDescription());
    }
}
