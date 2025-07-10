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

package org.apache.jena.rdf12;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import org.apache.jena.rdf12.parse.TS_RDFStar_Parse;

// Currently, RDF1.2 and SPARQL 1.2 specific tests.
// Split sometime / replace with scripted tests.
@Suite
@SelectClasses({
    TS_RDFStar_Parse.class,

    TestRDF12LangSyntax.class,
    TestSPARQL12Syntax.class,
    TestSPARQL12Eval.class,
    TestSPARQL12Results.class
})
public class TS_RDF12 {
}
