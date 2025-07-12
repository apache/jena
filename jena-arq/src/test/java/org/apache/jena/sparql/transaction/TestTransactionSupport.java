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

package org.apache.jena.sparql.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.apache.jena.atlas.lib.Creator;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.DatasetGraphSink;
import org.apache.jena.sparql.core.DatasetGraphZero;
import org.apache.jena.sparql.graph.GraphFactory;

/** "supports" for various DatasetGraph implementations */

@ParameterizedClass(name="{index}: {0}")
@MethodSource("provideArgs")
public class TestTransactionSupport {
    private static Stream<Arguments> provideArgs() {
        List<Arguments> x = List.of
                (Arguments.of("createTxnMem", (Creator<DatasetGraph>)()->DatasetGraphFactory.createTxnMem(), true, true),
                 Arguments.of("createGeneral",  (Creator<DatasetGraph>)()->DatasetGraphFactory.createGeneral(), true, false),
                 Arguments.of("create", (Creator<DatasetGraph>)()->DatasetGraphFactory.create(),  true, false),
                 Arguments.of("wrap(Graph)",(Creator<DatasetGraph>)()->DatasetGraphFactory.wrap(GraphFactory.createDefaultGraph()),true, false),
                 Arguments.of("zero" ,(Creator<DatasetGraph>)()->DatasetGraphZero.create(),true, true),
                 Arguments.of("sink" ,(Creator<DatasetGraph>)()->DatasetGraphSink.create(),true, true),
                 Arguments.of("create(Graph)", (Creator<DatasetGraph>)()->DatasetGraphFactory.create(GraphFactory.createDefaultGraph()), true, false)
                        );
        return x.stream();
    }

    @Parameter(0)
    String name;
    @Parameter(1)
    Creator<DatasetGraph> maker;
    @Parameter(2)
    boolean supportsTxn;
    @Parameter(3)
    boolean supportsAbort;

    @Test public void txn_support() {
        DatasetGraph dsg = maker.create();
        test(dsg, supportsTxn, supportsAbort);
    }

    private static void test(DatasetGraph dsg, boolean supportsTxn, boolean supportsAbort) {
        assertEquals(supportsTxn,    dsg.supportsTransactions(),      ()->"supports");
        assertEquals( supportsAbort, dsg.supportsTransactionAbort(),  ()->"supportsAbort");
    }
}
