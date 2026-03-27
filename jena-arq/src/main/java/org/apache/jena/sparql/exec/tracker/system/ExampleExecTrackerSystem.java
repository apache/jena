/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.sparql.exec.tracker.system;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.jena.query.ARQ;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.engine.dispatch.QueryDispatcherRegistry;
import org.apache.jena.sparql.engine.dispatch.UpdateDispatcherRegistry;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase0;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.system.AutoTxn;
import org.apache.jena.system.Txn;

public class ExampleExecTrackerSystem {

    static { JenaSystem.init(); }

    public static class FunctionFail extends FunctionBase0 {
        @Override
        public NodeValue exec() {
            throw new RuntimeException("Simulated failure");
        }
    }

    public static void main(String[] args) {
        // Already registered in InitExecTrackerSystem:
        QueryDispatcherRegistry.addDispatcher(new ChainingQueryDispatcherExecTrackerSystem());
        UpdateDispatcherRegistry.addDispatcher(new ChainingUpdateDispatcherExecTrackerSystem());

        TaskEventBroker broker = TaskEventBroker.getOrCreate(ARQ.getContext());
        TaskEventHistory history = TaskEventHistory.getOrCreate(ARQ.getContext());

        // Make the history listen to the event broker.
        history.connect(broker);

        FunctionRegistry.get().put("urn:fail", FunctionFail.class);

        DatasetGraph dsg = DatasetGraphFactory.create();
        // DatasetGraph dsg = TDB2Factory.createDataset().asDatasetGraph();
        try (AutoTxn txn = Txn.autoTxn(dsg, ReadWrite.WRITE)) {
            QueryExec.newBuilder()
                .dataset(dsg)
                .query("SELECT * { ?s ?p ?o }")
                .table();

            UpdateExec.newBuilder()
                .dataset(dsg)
                .update("PREFIX eg: <http://www.example.org/> INSERT DATA { eg:s eg:p eg:o }")
                .execute();

            // Will fail:
            try {
                UpdateExec.newBuilder()
                    .dataset(DatasetGraphFactory.empty())
                    .update("PREFIX eg: <http://www.example.org/> INSERT DATA { eg:s eg:p eg:o }")
                    .execute();
            } catch (Exception e) {
                // Ignore
            }

            // Will fail:
            try {
                QueryExec.newBuilder()
                    .dataset(dsg)
                    .query("SELECT (<urn:fail>() AS ?x) { }")
                    .table();
            } catch (Exception e) {
                // Ignore
            }

            txn.commit();
        }

        for (var e : history.getHistory()) {
            BasicTaskExec taskExec = e.getValue();
            String label = taskExec.getLabel().replaceAll("\n", "↵");

            System.out.println("----------------------");
            System.out.println("Label:    " + label);
            System.out.println("Created:  " + taskExec.getCreationTime());
            System.out.println("Started:  " + taskExec.getStartTime().map(ExampleExecTrackerSystem::formatInstant).orElse("n/a"));
            System.out.println("Aborted:  " + taskExec.getAbortTime().map(ExampleExecTrackerSystem::formatInstant).orElse("n/a"));
            System.out.println("Finished: " + taskExec.getFinishTime().map(ExampleExecTrackerSystem::formatInstant).orElse("n/a"));
            System.out.println("Error:    " + taskExec.getThrowable().map(Throwable::getMessage).orElse("n/a"));
        }
    }

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS z");

    private static String formatInstant(Instant instant) {
        ZoneId zone = ZoneId.of("UTC");
        ZonedDateTime zdt = instant.atZone(zone);
        String str = formatter.format(zdt);
        return str;
    }
}
