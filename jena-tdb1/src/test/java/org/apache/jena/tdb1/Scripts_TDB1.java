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

package org.apache.jena.tdb1;

import java.util.stream.Stream;

import org.junit.jupiter.api.*;

import org.apache.jena.arq.junit.Scripts;
import org.apache.jena.arq.junit.manifest.TestMaker;
import org.apache.jena.arq.junit.sparql.SparqlTests;
import org.apache.jena.atlas.lib.Creator;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Dataset;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.NodeValue;

public class Scripts_TDB1
{
    @BeforeAll static public void beforeClass() {
        ARQ.setNormalMode();
        NodeValue.VerboseWarnings = false;
        E_Function.WarnOnUnknownFunction = false;
    }

    @AfterAll static public void afterClass() {
        NodeValue.VerboseWarnings = true;
        E_Function.WarnOnUnknownFunction = true;
    }

    @TestFactory
    @DisplayName("TDB1")
    public Stream<DynamicNode> testFactory(){
        @SuppressWarnings("removal")
        Creator<Dataset> creator = ()->TDB1Factory.createDataset();
        TestMaker testMaker = (manifestEntry) -> SparqlTests.makeSPARQLTestExecOnly(manifestEntry, creator);
        return Scripts.manifestTestFactory("testing/manifest.ttl", "TDB1-", testMaker);
    }
}
