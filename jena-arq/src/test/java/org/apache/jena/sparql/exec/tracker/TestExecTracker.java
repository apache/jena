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

package org.apache.jena.sparql.exec.tracker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.system.Txn;
import org.junit.jupiter.api.Test;

public class TestExecTracker {

    /** Test tracking of execution requests using TaskEventHistory. */
    @Test
    public void test() {
        int maxHistorySize = 100;
        int requestCount = 50;

        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        IntStream.range(0, requestCount).mapToObj(i -> NodeFactory.createURI("urn:foo:bar" + i))
            .forEach(x -> dsg.getDefaultGraph().add(x, x, x));

        TaskEventBroker tracker = TaskEventBroker.getOrCreate(dsg.getContext());
        TaskEventHistory history = TaskEventHistory.getOrCreate(dsg.getContext());
        history.setMaxHistorySize(maxHistorySize);
        history.connect(tracker);

        IntStream.range(0, requestCount).boxed().collect(Collectors.toCollection(ArrayList::new))
            .parallelStream().forEach(x -> {
                Txn.executeWrite(dsg, () -> {
                    UpdateExec.dataset(dsg).update("INSERT { ?s ?p 1 } WHERE { ?s ?p ?o }").execute();
                });
            });

        assertEquals(0, history.getActiveTasks().size());
        int expectedHistorySize = Math.min(requestCount, maxHistorySize);
        assertEquals(expectedHistorySize, history.getHistory().size());

        // Sanity check: Disconnect from the history and make sure no more events are logged.

        history.disconnectFromAll();
        history.clear();

        IntStream.range(0, requestCount).boxed().collect(Collectors.toCollection(ArrayList::new))
            .parallelStream().forEach(x -> {
                Txn.executeWrite(dsg, () -> {
                    UpdateExec.dataset(dsg).update("INSERT { ?s ?p 1 } WHERE { ?s ?p ?o }").execute();
                });
            });

        assertEquals(0, history.getActiveTasks().size());
        assertEquals(0, history.getHistory().size());
   }
}
