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

package org.apache.jena.fuseki.mod.exectracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.sys.FusekiAutoModule;
import org.apache.jena.fuseki.server.DataAccessPoint;
import org.apache.jena.fuseki.server.DataAccessPointRegistry;
import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.fuseki.server.Endpoint;
import org.apache.jena.fuseki.server.FusekiVocab;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.exec.tracker.TaskEventHistory;
import org.apache.jena.sparql.exec.tracker.TaskEventBroker;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;

public class FMod_ExecTracker implements FusekiAutoModule {
    public static final Symbol symAllowAbort = Symbol.create("allowAbort");

    private static final Operation OPERATION = Operation.alloc(
            FusekiVocab.NS + "tracker", "tracker", "Execution Tracker");

    public static Operation getOperation() {
        return OPERATION;
    }

    public FMod_ExecTracker() {
        super();
    }

    @Override
    public String name() {
        return "ExecTracker";
    }

    @Override
    public void prepare(FusekiServer.Builder builder, Set<String> datasetNames, Model configModel) {
        Operation trackerOperation = getOperation();
        Fuseki.configLog.info(name() + ": Registering operation " + trackerOperation.getId());
        builder.registerOperation(trackerOperation, new ExecTrackerService());
    }

    /**
     * For each dataset with a 'tracker' endpoint set up a task event broker.
     */
    @Override
    public void configured(FusekiServer.Builder serverBuilder, DataAccessPointRegistry dapRegistry, Model configModel) {
        FusekiAutoModule.super.configured(serverBuilder, dapRegistry, configModel);

        Operation trackerOperation = getOperation();
        List<DataAccessPoint> newDataAccessPoints = new ArrayList<>();
        for (DataAccessPoint dap : dapRegistry.accessPoints()) {
            DataService dataService = dap.getDataService();
            DatasetGraph dsg = dataService.getDataset();

            List<Endpoint> trackerEndpoints = Optional.ofNullable(dataService.getEndpoints(trackerOperation)).orElse(List.of());

            if (!trackerEndpoints.isEmpty()) {
                // Register a task tracker registry in the dataset context.
                Context datasetCxt = dsg.getContext();
                if (datasetCxt != null) {
                    TaskEventBroker taskTrackerRegistry = TaskEventBroker.getOrCreate(datasetCxt);

                    // Then register task trackers with history into the endpoint context.
                    for (Endpoint endpoint : trackerEndpoints) {
                        Context endpointCxt = endpoint.getContext();

                        TaskEventHistory historyTracker = TaskEventHistory.getOrCreate(endpointCxt);
                        historyTracker.connect(taskTrackerRegistry);
                        // XXX Should disconnect history tracker on server shutdown.
                    }
                }
            } else {
                newDataAccessPoints.add(dap);
            }
        }

        // "replace" each DataAccessPoint
        newDataAccessPoints.forEach(dap -> {
            dapRegistry.remove(dap.getName());
            dapRegistry.register(dap);
        });
    }

    @Override
    public void serverStopped(FusekiServer server) {
        // XXX Should disconnect history tracker on server shutdown.
    }
}
