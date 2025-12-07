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

package org.apache.jena.sparql;

import java.util.stream.Stream;

import org.junit.jupiter.api.*;

import org.apache.jena.arq.junit.Scripts;
import org.apache.jena.arq.junit.manifest.TestMaker;
import org.apache.jena.arq.junit.sparql.SparqlTests;
import org.apache.jena.atlas.lib.Creator;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.sparql.core.DatasetGraphMap;
import org.apache.jena.sparql.core.DatasetGraphMapLink;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.graph.GraphFactory;

public class Scripts_SPARQL_Dataset {

    private static TestMaker testMakerDataset(Creator<Dataset> creator) {
        return (manifestEntry) -> SparqlTests.makeSPARQLTestExecOnly(manifestEntry, creator);
    }

    @TestFactory
    @DisplayName("DS-TIM")
    public Stream<DynamicNode> testFactory_TIM() {
        TestMaker testMaker_TIM = testMakerDataset(()->DatasetFactory.createTxnMem());
        return Scripts.manifestTestFactory("testing/ARQ/manifest-arq.ttl", "TIM-", testMaker_TIM);
    }

    @TestFactory
    @DisplayName("DS-General")
    public Stream<DynamicNode> testFactory_General() {
        TestMaker testMaker_General = testMakerDataset(()->DatasetFactory.createGeneral());
        return Scripts.manifestTestFactory("testing/ARQ/manifest-arq.ttl", "General-", testMaker_General);
    }

    @TestFactory
    @DisplayName("DS-Map")
    public Stream<DynamicNode> testFactory_Map() {
        Creator<Dataset> creator = ()->DatasetFactory.wrap(new DatasetGraphMap());
        TestMaker testMaker_Map = testMakerDataset(creator);
        return Scripts.manifestTestFactory("testing/ARQ/manifest-arq.ttl", "Map-", testMaker_Map);
    }

    @TestFactory
    @DisplayName("DS-MapLink")
    public Stream<DynamicNode> testFactory_MapLink() {
        Creator<Dataset> creator = ()->DatasetFactory.wrap(new DatasetGraphMapLink(GraphFactory.createDefaultGraph()));
        TestMaker testMaker_Map = testMakerDataset(creator);
        return Scripts.manifestTestFactory("testing/ARQ/manifest-arq.ttl", "MapLink-", testMaker_Map);
    }

    private static boolean bVerboseWarnings;
    private static boolean bWarnOnUnknownFunction;

    @BeforeAll
    public static void beforeClass() {
        bVerboseWarnings = NodeValue.VerboseWarnings;
        bWarnOnUnknownFunction = E_Function.WarnOnUnknownFunction;
        NodeValue.VerboseWarnings = false;
        E_Function.WarnOnUnknownFunction = false;
    }

    @AfterAll
    public static void afterClass() {
        NodeValue.VerboseWarnings = bVerboseWarnings;
        E_Function.WarnOnUnknownFunction = bWarnOnUnknownFunction;
    }
}
