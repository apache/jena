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

package org.apache.jena.riot.lang.rdfxml.converted_legacy;

import static org.apache.jena.riot.lang.rdfxml.converted_legacy.LibTestARP1.positiveTest;

import org.junit.Test;

/**
 * The ARP test suite run on a local legacy copy of the RDF 1.0 WG test suite
 * (updated for RDF 1.1). Tests marked pending.
 */
public class TestARP1_W3C_Pending {
    @Test
    public void test_rdfms_empty_property_elements_016() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test016",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test016.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test016.nt");
    }

    @Test
    public void test_rdfms_empty_property_elements_017() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test017",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test017.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test017.nt");
    }

    @Test
    public void test_rdfms_difference_between_ID_and_about_3() {
        positiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/Manifest.rdf#test3",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/test3.rdf",
                     "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/test3.nt");
    }
}