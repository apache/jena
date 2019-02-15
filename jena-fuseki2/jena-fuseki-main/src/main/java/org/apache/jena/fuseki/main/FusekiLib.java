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

import javax.servlet.ServletContext;

import org.apache.jena.fuseki.access.*;
import org.apache.jena.fuseki.build.FusekiBuilder;
import org.apache.jena.fuseki.server.DataAccessPointRegistry;
import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.fuseki.servlets.ActionService;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.fuseki.servlets.ServiceDispatchRegistry;
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
        FusekiBuilder.addDataset(dataAccessPoints, name, dsg, withUpdate);
        return server;
    }

    /** Add a {@link DataService} to a server */
    public static FusekiServer addDataset(FusekiServer server, String name, DataService dataService) {
        DataAccessPointRegistry dataAccessPoints = DataAccessPointRegistry.get(server.getServletContext());
        FusekiBuilder.addDataService(dataAccessPoints, name, dataService);
        return server;
    }

    /** Remove dataset from a server */
    public static FusekiServer removeDataset(FusekiServer server, String name) {
        DataAccessPointRegistry dataAccessPoints = DataAccessPointRegistry.get(server.getServletContext());
        FusekiBuilder.removeDataset(dataAccessPoints, name);
        return server;
    }
    
    /**
     * Return a {@code FusekiServer.Builder} setup for data access control.
     */
    public static FusekiServer.Builder fusekiBuilder(Function<HttpAction, String> determineUser) {
        FusekiServer.Builder builder = FusekiServer.create();
        return fusekiBuilder(builder, determineUser);
    }
    
    /**
     * Modify a {@code FusekiServer.Builder} setup for data access control.
     */
    public static FusekiServer.Builder fusekiBuilder(FusekiServer.Builder builder, Function<HttpAction, String> determineUser) {
        // Replace the standard operations with the SecurityRegistry processing ones. 
        builder.registerOperation(Operation.Query, WebContent.contentTypeSPARQLQuery, new AccessCtl_SPARQL_QueryDataset(determineUser));
        builder.registerOperation(Operation.GSP_R, new AccessCtl_SPARQL_GSP_R(determineUser));
        builder.registerOperation(Operation.Quads_R, new AccessCtl_REST_Quads_R(determineUser));
        
        // Block updates (can just not register these operations).
        builder.registerOperation(Operation.Update, WebContent.contentTypeSPARQLUpdate, new AccessCtl_SPARQL_Update(determineUser));
        builder.registerOperation(Operation.GSP_RW, new AccessCtl_SPARQL_GSP_RW(determineUser));
        builder.registerOperation(Operation.Quads_RW, new AccessCtl_REST_Quads_RW(determineUser));
        return builder;
    }

    /**
     * Modify in-place an existing {@link FusekiServer} so that the read-operations for
     * query/GSP/Quads go to the data-filtering versions of the {@link ActionService ActionServices}.
     */
    public static void modifyForAccessCtl(FusekiServer server, Function<HttpAction, String> determineUser) {
        /*
         * Reconfigure standard Jena Fuseki, replacing the default implementations with
         * filtering ones. This for this server only, not system-wide.
         */
        // The mapping operation to handler is in the ServiceDispatchRegistry and is per
        // server (per servlet context). "registerOrReplace" might be a better name,
        ServletContext cxt = server.getServletContext();
        ServiceDispatchRegistry reg = ServiceDispatchRegistry.get(cxt);
        modifyForAccessCtl(reg, determineUser);
    }
        
    /**
     * Modify in-place an existing {@link ServiceDispatchRegistry} so that the operations for
     * query/GSP/Quads go to the data-filtering versions of the {@link ActionService ActionServices}
     */
    public static void modifyForAccessCtl(ServiceDispatchRegistry reg, Function<HttpAction, String> determineUser) {
        resetOperation(reg, Operation.Query, WebContent.contentTypeSPARQLQuery, new AccessCtl_SPARQL_QueryDataset(determineUser));
        resetOperation(reg, Operation.GSP_R, null, new AccessCtl_SPARQL_GSP_R(determineUser));
        resetOperation(reg, Operation.Quads_R, null, new AccessCtl_REST_Quads_R(determineUser));

        // Block updates
        resetOperation(reg, Operation.Update, WebContent.contentTypeSPARQLUpdate, new AccessCtl_SPARQL_Update(determineUser));
        resetOperation(reg, Operation.GSP_RW, null, new AccessCtl_SPARQL_GSP_RW(determineUser));
        resetOperation(reg, Operation.Quads_RW, null, new AccessCtl_SPARQL_Update(determineUser));

//        reg.unregister(Operation.Update);
//        reg.unregister(Operation.GSP_RW);
//        reg.unregister(Operation.Quads_RW);
    }
    
    private static void resetOperation(ServiceDispatchRegistry reg, Operation operation, String contentType, ActionService service) {
        if ( reg.isRegistered(operation) )
            reg.register(operation, contentType, service);
    }
}
