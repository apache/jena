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

package org.apache.jena.riot.lang.rdfxml.manifest_rdf11;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Run all the parsers on the rdf-test CG RDF/XML manifest files (RDF 1.1).
 */
@RunWith(Suite.class)
@SuiteClasses( {
    // ARP0 is unlikely to be maintained.
    TestManifest_RDF11_ARP0.class,
    TestManifest_RDF11_ARP1.class,
    TestManifest_RDF11_RRX_SAX.class,
    TestManifest_RDF11_RRX_StAXsr.class,
    TestManifest_RDF_RRX_StAXev.class
})

public class Scripts_RRX_RDFXML {}
