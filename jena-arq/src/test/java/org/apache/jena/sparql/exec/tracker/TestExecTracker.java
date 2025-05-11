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

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.system.Txn;
import org.junit.Test;

public class TestExecTracker {
    @Test
    public void test() {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        IntStream.range(0, 1000).mapToObj(i -> NodeFactory.createURI("urn:foo:bar" + i))
            .forEach(x -> dsg.getDefaultGraph().add(x, x, x));

        TaskEventBroker tracker = TaskEventBroker.getOrCreate(dsg.getContext());
        TaskEventHistory history = TaskEventHistory.getOrCreate(dsg.getContext());
        history.connect(tracker);
        // ExecTracker execTracker = ExecTracker.ensureTracker(dsg.getContext());

        IntStream.range(0, 1000).boxed().collect(Collectors.toCollection(ArrayList::new)).parallelStream().forEach(x -> {
//            Table actualTable = QueryExec.newBuilder()
//                    .dataset(finalDsg).query("SELECT * { ?s ?p ?o }").table();
//            System.out.println("" + x + ": " + execTracker);
            Txn.executeWrite(dsg, () -> {
                UpdateExec.dataset(dsg).update("INSERT { ?s ?p 1 } WHERE { ?s ?p ?o }").execute();
            });
        });
        System.out.println("Result: " + history);
   }
}
