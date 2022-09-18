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

package org.apache.jena.tdb2.junit;

import java.util.function.Function;

import org.apache.jena.arq.junit.manifest.ManifestEntry;
import org.apache.jena.arq.junit.runners.AbstractRunnerOfTests;
import org.apache.jena.arq.junit.sparql.SparqlTests;
import org.apache.jena.atlas.lib.Creator;
import org.apache.jena.query.Dataset;
import org.apache.jena.tdb2.TDB2Factory;
import org.junit.runners.model.InitializationError;

public class RunnerSPARQL_TDB2 extends AbstractRunnerOfTests {

    public RunnerSPARQL_TDB2(Class<? > klass) throws InitializationError {
        super(klass, testMaker());
    }

    private static Function<ManifestEntry, Runnable> testMaker() {
        Creator<Dataset> creator = ()->TDB2Factory.createDataset();
        return
            (manifestEntry) ->
                SparqlTests.makeSPARQLTestExecOnly(manifestEntry, creator);
    }
}

