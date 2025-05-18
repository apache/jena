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
package org.apache.jena.fuseki.mod.geosparql;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.auth.AuthPolicy;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.sys.FusekiAutoModule;
import org.apache.jena.fuseki.server.DataAccessPoint;
import org.apache.jena.fuseki.server.DataAccessPointRegistry;
import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.fuseki.server.Endpoint;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.rdf.model.Model;

public class FMod_SpatialIndexer implements FusekiAutoModule {

    public static Operation spatialIndexerOperation =
        Operation.alloc("http://jena.apache.org/fuseki#spatial-indexer",
            "spatial-indexer",
            "Spatial indexer service");

    public FMod_SpatialIndexer() {
        super();
    }

    @Override
    public String name() {
        return "Spatial Indexer";
    }

    @Override
    public void start() {
    }

    @Override
    public void prepare(FusekiServer.Builder builder, Set<String> datasetNames, Model configModel) {
        Fuseki.configLog.info(name() + ": Registering operation " + spatialIndexerOperation.getId());
        builder.registerOperation(spatialIndexerOperation, new SpatialIndexerService());
    }

    /**
     * The spatial indexer endpoint is created for every SPARQL update endpoint.
     * The update endpoint's auth policy is inherited.
     */
    @Override
    public void configured(FusekiServer.Builder serverBuilder, DataAccessPointRegistry dapRegistry, Model configModel) {
        FusekiAutoModule.super.configured(serverBuilder, dapRegistry, configModel);

        boolean autoConfigure = false;
        if (autoConfigure) {
            autoConfigure(serverBuilder, dapRegistry, configModel);
        }
    }

    /**
     * Disabled for now.
     *
     * Automatically creates a corresponding spatial indexer endpoint for each SPARQL update endpoint.
     * The spatial indexer endpoint with name follows the pattern '{updateEndpointName}-spatial and inherits the update endpoint's auth policy.
     */
    private void autoConfigure(FusekiServer.Builder serverBuilder, DataAccessPointRegistry dapRegistry, Model configModel) {
        FusekiAutoModule.super.configured(serverBuilder, dapRegistry, configModel);

        List<DataAccessPoint> newDataAccessPoints = new ArrayList<>();
        // Register the spatial indexer for each update endpoint and inherit its auth policy.
        for (DataAccessPoint dap : dapRegistry.accessPoints()) {
            String dapName = dap.getName();
            String logPrefix = name() + " - " + dapName + ": ";

            DataService dataService = dap.getDataService();
            List<Endpoint> updateEndpoints = Optional.ofNullable(dataService.getEndpoints(Operation.Update))
                .orElse(List.of());

            List<Endpoint> spatialEndpoints = new ArrayList<>();
            for (Endpoint updateEndpoint : updateEndpoints) {
                String updateEndpointName = updateEndpoint.getName();
                String geoIndexerEndpointName = updateEndpointName + "-spatial";
                AuthPolicy authPolicy = updateEndpoint.getAuthPolicy();

                Fuseki.configLog.info(logPrefix + "Registering spatial indexer endpoint: " + geoIndexerEndpointName);

                Endpoint geoIndexerEndpoint = Endpoint.create()
                        .operation(spatialIndexerOperation)
                        .endpointName(geoIndexerEndpointName)
                        .authPolicy(authPolicy)
                        .build();

                spatialEndpoints.add(geoIndexerEndpoint);
            }

            // Create new DataService based on existing one with the spatial indexer endpoints attached.
            DataService.Builder dSrvBuilder = DataService.newBuilder(dataService);
            spatialEndpoints.forEach(dSrvBuilder::addEndpoint);
            DataService newDataService = dSrvBuilder.build();
            newDataAccessPoints.add(new DataAccessPoint(dapName, newDataService));
        }

        // "replace" each DataAccessPoint
        newDataAccessPoints.forEach(dap -> {
            dapRegistry.remove(dap.getName());
            dapRegistry.register(dap);
        });
    }
}
