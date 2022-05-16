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

import org.apache.jena.arq.junit.manifest.Manifests;
import org.apache.jena.arq.junit.runners.Label;
import org.apache.jena.arq.junit.runners.RunnerSPARQL;
import org.junit.runner.RunWith;

/**
 * The test suite for SPARQL 1.1 (the second SPARQL working group) approved tests, as
 * maintained by the "rdf-tests" community group.
 * <p>
 * Query tests modified to work in ARQ in default mode.
 * <ul>
 * <li>("+" can be used for string concatenation
 * <li>; the parser tokenizer is modified to be compatible with unicode surrogate
 * pairs for Java. Broken surrogate pairs are illegal.
 * <li>Supports expression without AS in SELECT clause
 * </ul>
 * <p>
 * Functionality for query is also covered by Scripts_ARQ (many tests were developed
 * there and contributed to the W3C working group).
 * <p>
 */
@RunWith(RunnerSPARQL.class)
@Label("SPARQL 1.1")
@Manifests({
    "testing/sparql11-query/manifest-sparql11-query.ttl"
    ,"testing/sparql11-update/manifest-sparql11-update.ttl"
})
public class Scripts_SPARQL11 {}

