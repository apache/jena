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
import java.util.Set;

import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.sys.FusekiAutoModule;
import org.apache.jena.fuseki.server.DataAccessPoint;
import org.apache.jena.fuseki.server.DataAccessPointRegistry;
import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.fuseki.server.Endpoint;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.rdf.model.Model;

public class FMod_SpatialIndexer implements FusekiAutoModule {

    private Operation spatialOperation = null;

    public Operation getOperation() {
        if (spatialOperation == null) {
            synchronized (this) {
                if (spatialOperation == null) {
                    Fuseki.configLog.info(name() + ": Add spatial indexer operation into global registry.");
                    spatialOperation = Operation.alloc("http://org.apache.jena/spatial-index-service",
                            "spatial-indexer",
                            "Spatial index computation service");
                }
            }
        }
        return spatialOperation;
    }

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
        Fuseki.configLog.info(name() + ": Module adds spatial index servlet");
        Operation op = getOperation();
        builder.registerOperation(op, new SpatialIndexComputeService());

        // This does not appear to do anything - datasetNames may be empty but data access points may exist.
        // datasetNames.forEach(name -> builder.addEndpoint(name, "spatial", op));
        // datasetNames.forEach(name -> builder.addEndpoint(name, "spatial-events", op));
    }

    @Override
    public void configured(FusekiServer.Builder serverBuilder, DataAccessPointRegistry dapRegistry, Model configModel) {
        FusekiAutoModule.super.configured(serverBuilder, dapRegistry, configModel);

        Operation op = getOperation();
        List<DataAccessPoint> daps = new ArrayList<>();

        for (DataAccessPoint dap : dapRegistry.accessPoints()) {
            Endpoint endpoint = Endpoint.create()
                    .operation(op)
                    .endpointName("spatial")
                    .build();

            // create new DataService based on existing one with the endpoint attached
            DataService dSrv = DataService.newBuilder(dap.getDataService())
                .addEndpoint(endpoint)
                .build();
            daps.add(new DataAccessPoint(dap.getName(), dSrv));
        }

        // "replace" each DataAccessPoint
        daps.forEach(dap -> {
            dapRegistry.remove(dap.getName());
            dapRegistry.register(dap);
        });
    }

    @Override
    public void serverAfterStarting(FusekiServer server) {
        Fuseki.configLog.info(name() + ": Customized server start on port " + server.getHttpPort());
    }
}
