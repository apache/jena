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
package org.apache.jena.fuseki.mod.exec.tracker;

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
import org.apache.jena.sparql.exec.tracker.HistoryTrackerRegistry;
import org.apache.jena.sparql.exec.tracker.TaskTrackerRegistry;
import org.apache.jena.sparql.exec.tracker.todelete.DatasetGraphWithExecTracker;
import org.apache.jena.sparql.util.Context;

public class FMod_ExecTracker implements FusekiAutoModule {
    private static Operation Tracker = Operation.alloc(FusekiVocab.NS + "tracker", "tracker", "Execution Tracker");

    public static Operation getOperation() {
        return Tracker;
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
        Fuseki.configLog.info(name() + ": Registering exec tracker operation and servlet.");
        Operation op = getOperation();
        builder.registerOperation(op, new ExecTrackerService());
    }

    /**
     * For each dataset with a 'tracker' endpoint replace the DataAccessPoint with one
     * that wraps the original dataset with {@link DatasetGraphWithExecTracker#wrap(DatasetGraph)}.
     */
    @Override
    public void configured(FusekiServer.Builder serverBuilder, DataAccessPointRegistry dapRegistry, Model configModel) {
        FusekiAutoModule.super.configured(serverBuilder, dapRegistry, configModel);

        List<DataAccessPoint> newDataAccessPoints = new ArrayList<>();
        for (DataAccessPoint dap : dapRegistry.accessPoints()) {
            String dapName = dap.getName();
            DataService dataService = dap.getDataService();
            DatasetGraph dsg = dataService.getDataset();

            List<Endpoint> trackerEndpoints = Optional.ofNullable(dataService.getEndpoints(Tracker)).orElse(List.of());

            boolean wrapEngines = false;
            if (!trackerEndpoints.isEmpty()) {
                if (wrapEngines) {
                    DatasetGraph trackedDsg = DatasetGraphWithExecTracker.wrap(dsg);

                    DataService newDataService = DataService.newBuilder(dataService)
                        .dataset(trackedDsg)
                        .build();

                    newDataAccessPoints.add(new DataAccessPoint(dapName, newDataService));
                } else {
                    // Register a task tracker registry in the dataset context.
                    Context datasetCxt = dsg.getContext();
                    TaskTrackerRegistry taskTrackerRegistry = TaskTrackerRegistry.getOrSet(datasetCxt);

                    // Then register task trackers with history into the endpoint context.
                    for (Endpoint endpoint : trackerEndpoints) {
                        Context endpointCxt = endpoint.getContext();

                        HistoryTrackerRegistry historyTracker = HistoryTrackerRegistry.getOrSet(endpointCxt);
                        historyTracker.connect(taskTrackerRegistry);
                        // FIXME Make sure to dispose when stopping the server!
                        // Runnable disposer = taskTrackerRegistry.addListener(BasicTaskExec.class, historyTracker);
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
}
