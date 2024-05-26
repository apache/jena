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

package org.apache.jena.arq.junit.runners;

import org.apache.jena.arq.junit.sparql.SparqlTests;
import org.junit.runners.model.InitializationError;

/**
 * Runner for SPARQL Manifests. Annotations supported:
 * <ul>
 * <li>{@code @Label("Some name")}</li>
 * <li>{@code @Manifests({"manifest1","manifest2",...})}</li>
 * </ul>
 * This class sorts out the annotations, including providing before/after class, then
 * creates a hierarchy of tests to run.
 *
 * @see RunnerOneTest
 */
public class RunnerSPARQL extends AbstractRunnerOfTests {

    public RunnerSPARQL(Class<? > klass) throws InitializationError {
        super(klass, SparqlTests::makeSPARQLTest);
    }
}
