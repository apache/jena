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

package org.apache.jena.arq;

public class TestConsts {
    /**
     * Root of the RDF tests CG file hierarchy.
     * This contains RDF syntax and SPARQL tests.
     */
    public static final String CG_TESTS_DIR = "testing/rdf-tests-cg/";

    // RDF syntax tests for RDF 1.2 references RDF 1.1 in their manifests.
    public static final String RDF12_TESTS_DIR = CG_TESTS_DIR+"rdf/rdf12/";
    public static final String RDF11_TESTS_DIR = CG_TESTS_DIR+"rdf/rdf11/";

    // SPARQL 1.2 tests do not references SPARQL 1.1 tests.
    public static final String SPARQL10_TESTS_DIR = CG_TESTS_DIR+"sparql/sparql10/";
    public static final String SPARQL11_TESTS_DIR = CG_TESTS_DIR+"sparql/sparql11/";
    public static final String SPARQL12_TESTS_DIR = CG_TESTS_DIR+"sparql/sparql12/";

    // Other RIOT tests by manifest
    public static final String RIOT_TESTS_DIR = "testing/RIOT/Lang/";
}
