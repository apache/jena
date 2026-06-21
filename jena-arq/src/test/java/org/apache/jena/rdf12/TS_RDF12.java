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

package org.apache.jena.rdf12;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Basic testing of RDF 1.2 features to test the machinery.
 *
 * The rdf-tests should do full coverage so the tests here
 * are a precursor, with more focused error reporting.
 */
@Suite
@SelectClasses({
    TestNQuadsTripleTerms.class,
    TestNTriplesTripleTerms.class,
    TestTrigParseTripleTerms.class,
    TestTurtleTripleTermsParse.class,
    TestSPARQL12TripleTerms.class,

    TestRDF12LangDirSyntax.class,
    TestSPARQL12Syntax.class,
    TestSPARQL12Eval.class,
    TestSPARQL12Results.class
})
public class TS_RDF12 {}
