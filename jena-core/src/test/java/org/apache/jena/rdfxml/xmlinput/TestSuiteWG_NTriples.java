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

package org.apache.jena.rdfxml.xmlinput ;

import junit.framework.Test ;
import junit.framework.TestSuite ;
import org.apache.jena.rdfxml.libtest.InputStreamFactoryTests;

public class TestSuiteWG_NTriples
{
    static public Test suite()
    {
        InputStreamFactoryTests factory = new InputStreamFactoryTests("http://www.w3.org/2000/10/rdf-tests/rdfcore/","wg");
        WGTestSuite testSuite = new org.apache.jena.rdfxml.xmlinput.NTripleTestSuite(factory,"NTriple WG Tests", false) ;

        TestSuite test212 = new TestSuite("APPROVED") ;
        Test test213 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-024",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-024.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-024.nt",
                                                  false) ;
        test212.addTest(test213) ;
        Test test214 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-025",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-025.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-025.nt",
                                                  false) ;
        test212.addTest(test214) ;
        Test test215 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test003",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test003.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test003.nt",
                                                  false) ;
        test212.addTest(test215) ;
        Test test216 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test004",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test004.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test004.nt",
                                                  false) ;
        test212.addTest(test216) ;
        Test test217 = testSuite
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-019",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-019.rdf",
                                                  true, new int[]{205,}) ;
        test212.addTest(test217) ;
        Test test218 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-017",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-017.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-017.nt",
                                                  false) ;
        test212.addTest(test218) ;
        Test test219 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-018",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-018.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-018.nt",
                                                  false) ;
        test212.addTest(test219) ;
        Test test220 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-element-not-mandatory/Manifest.rdf#test001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-element-not-mandatory/test001.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-element-not-mandatory/test001.nt",
                                                  false) ;
        test212.addTest(test220) ;
        Test test221 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/datatypes/Manifest.rdf#test001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/datatypes/test001.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/datatypes/test001.nt",
                                                  false) ;
        test212.addTest(test221) ;
        Test test222 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/Manifest.rdf#test005",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test005.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test005.nt",
                                                  false) ;
        test212.addTest(test222) ;
        Test test223 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/Manifest.rdf#test006",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test006.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test006.nt",
                                                  false) ;
        test212.addTest(test223) ;
        Test test224 = testSuite
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-001.rdf",
                                                  true, new int[]{205,}) ;
        test212.addTest(test224) ;
        Test test225 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test011",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test011.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test011.nt",
                                                  false) ;
        test212.addTest(test225) ;
        Test test226 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test010",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test010.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test010.nt",
                                                  false) ;
        test212.addTest(test226) ;
        Test test227 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0013",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0013.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0013.nt",
                                                  false) ;
        test212.addTest(test227) ;
        Test test228 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0012",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0012.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0012.nt",
                                                  false) ;
        test212.addTest(test228) ;
        Test test229 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/unrecognised-xml-attributes/Manifest.rdf#test002",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/unrecognised-xml-attributes/test002.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/unrecognised-xml-attributes/test002.nt",
                                                  false) ;
        test212.addTest(test229) ;
        Test test230 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/unrecognised-xml-attributes/Manifest.rdf#test001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/unrecognised-xml-attributes/test001.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/unrecognised-xml-attributes/test001.nt",
                                                  false) ;
        test212.addTest(test230) ;
        Test test231 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test002",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test002.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test002.nt",
                                                  false) ;
        test212.addTest(test231) ;
        Test test232 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test001.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test001.nt",
                                                  false) ;
        test212.addTest(test232) ;
        Test test233 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfs-domain-and-range/Manifest.rdf#test002",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfs-domain-and-range/test002.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfs-domain-and-range/test002.nt",
                                                  false) ;
        test212.addTest(test233) ;
        Test test234 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfs-domain-and-range/Manifest.rdf#test001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfs-domain-and-range/test001.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfs-domain-and-range/test001.nt",
                                                  false) ;
        test212.addTest(test234) ;
        Test test235 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xml-literal-namespaces/Manifest.rdf#test001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xml-literal-namespaces/test001.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xml-literal-namespaces/test001.nt",
                                                  false) ;
        test212.addTest(test235) ;
        Test test236 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test009",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test009.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test009.nt",
                                                  false) ;
        test212.addTest(test236) ;
        Test test237 = testSuite
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-004",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-004.rdf",
                                                  true, new int[]{205,}) ;
        test212.addTest(test237) ;
        Test test238 = testSuite
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-005",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-005.rdf",
                                                  true, new int[]{205,}) ;
        test212.addTest(test238) ;
        Test test239 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-028",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-028.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-028.nt",
                                                  false) ;
        test212.addTest(test239) ;
        Test test240 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-029",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-029.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-029.nt",
                                                  false) ;
        test212.addTest(test240) ;
        Test test241 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test012",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test012.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test012.nt",
                                                  false) ;
        test212.addTest(test241) ;
        Test test242 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test013",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test013.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test013.nt",
                                                  false) ;
        test212.addTest(test242) ;
        Test test243 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/Manifest.rdf#test002",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test002.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test002.nt",
                                                  false) ;
        test212.addTest(test243) ;
        Test test244 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/Manifest.rdf#test003",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test003.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test003.nt",
                                                  false) ;
        test212.addTest(test244) ;
        Test test245 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0006",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0006.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0006.nt",
                                                  false) ;
        test212.addTest(test245) ;
        Test test246 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0005",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0005.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0005.nt",
                                                  false) ;
        test212.addTest(test246) ;
        Test test247 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-reification-required/Manifest.rdf#test001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-reification-required/test001.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-reification-required/test001.nt",
                                                  false) ;
        test212.addTest(test247) ;
        Test test248 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-021",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-021.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-021.nt",
                                                  false) ;
        test212.addTest(test248) ;
        Test test249 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-020",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-020.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-020.nt",
                                                  false) ;
        test212.addTest(test249) ;
        Test test250 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test008",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test008.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test008.nt",
                                                  false) ;
        test212.addTest(test250) ;
        Test test251 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test007",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test007.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test007.nt",
                                                  false) ;
        test212.addTest(test251) ;
        Test test252 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/Manifest.rdf#test006",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test006.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test006.nt",
                                                  false) ;
        test212.addTest(test252) ;
        Test test253 = testSuite
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-016",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-016.rdf",
                                                  true, new int[]{205,}) ;
        test212.addTest(test253) ;
        Test test254 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/Manifest.rdf#test007",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test007.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test007.nt",
                                                  false) ;
        test212.addTest(test254) ;
        Test test255 = testSuite
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-015",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-015.rdf",
                                                  true, new int[]{205,}) ;
        test212.addTest(test255) ;
        Test test256 = testSuite
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#error005",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/error005.rdf",
                                                  true, null) ;
        test212.addTest(test256) ;
        Test test257 = testSuite
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#error004",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/error004.rdf",
                                                  true, null) ;
        test212.addTest(test257) ;
        Test test258 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0003",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0003.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0003.nt",
                                                  false) ;
        test212.addTest(test258) ;
        Test test259 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0004",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0004.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0004.nt",
                                                  false) ;
        test212.addTest(test259) ;
        Test test260 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/Manifest.rdf#test005",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test005.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test005.nt",
                                                  false) ;
        test212.addTest(test260) ;
        Test test261 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test014",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test014.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test014.nt",
                                                  false) ;
        test212.addTest(test261) ;
        Test test262 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0009",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0009.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0009.nt",
                                                  false) ;
        test212.addTest(test262) ;
        Test test263 = testSuite
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/Manifest.rdf#error004",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/error004.rdf",
                                                  true, null) ;
        test212.addTest(test263) ;
        Test test264 = testSuite
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/Manifest.rdf#error005",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/error005.rdf",
                                                  true, null) ;
        test212.addTest(test264) ;
        Test test265 = testSuite
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/Manifest.rdf#error007",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/error007.rdf",
                                                  true, null) ;
        test212.addTest(test265) ;
        Test test266 = testSuite
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/Manifest.rdf#error006",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/error006.rdf",
                                                  true, null) ;
        test212.addTest(test266) ;
        Test test267 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/amp-in-url/Manifest.rdf#test001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/amp-in-url/test001.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/amp-in-url/test001.nt",
                                                  false) ;
        test212.addTest(test267) ;
        Test test268 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#test001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/test001.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/test001.nt",
                                                  false) ;
        test212.addTest(test268) ;
        Test test269 = testSuite
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-abouteach/Manifest.rdf#error002",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-abouteach/error002.rdf",
                                                  true, new int[]{206,}) ;
        test212.addTest(test269) ;
        Test test270 = testSuite
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/Manifest.rdf#error002",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/error002.rdf",
                                                  true, null) ;
        test212.addTest(test270) ;
        Test test271 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/Manifest.rdf#test004",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test004.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test004.nt",
                                                  false) ;
        test212.addTest(test271) ;
        Test test272 = testSuite
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/Manifest.rdf#error003",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/error003.rdf",
                                                  true, null) ;
        test212.addTest(test272) ;
        Test test273 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/datatypes/Manifest.rdf#test002",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/datatypes/test002.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/datatypes/test002.nt",
                                                  false) ;
        test212.addTest(test273) ;
        Test test274 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test004",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test004.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test004.nt",
                                                  false) ;
        test212.addTest(test274) ;
        Test test275 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test003",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test003.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test003.nt",
                                                  false) ;
        test212.addTest(test275) ;
        Test test276 = testSuite
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/Manifest.rdf#error1",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/error1.rdf",
                                                  true, new int[]{105,}) ;
        test212.addTest(test276) ;
        Test test277 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-030",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-030.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-030.nt",
                                                  false) ;
        test212.addTest(test277) ;
        Test test278 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/Manifest.rdf#test001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test001.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test001.nt",
                                                  false) ;
        test212.addTest(test278) ;
        Test test279 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/Manifest.rdf#test002",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test002.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test002.nt",
                                                  false) ;
        test212.addTest(test279) ;
        Test test280 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#test004",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/test004.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/test004.nt",
                                                  false) ;
        test212.addTest(test280) ;
        Test test281 = testSuite
                              .createWarningTest(
                                                 "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#warn-001",
                                                 "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/warn-001.rdf",
                                                 true,
                                                 "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/warn-001.nt",
                                                 false, null) ;
        test212.addTest(test281) ;
        Test test282 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xml-canon/Manifest.rdf#test001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xml-canon/test001.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xml-canon/test001.nt",
                                                  false) ;
        test212.addTest(test282) ;
        Test test283 = testSuite
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-abouteach/Manifest.rdf#error001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-abouteach/error001.rdf",
                                                  true, new int[]{206,}) ;
        test212.addTest(test283) ;
        Test test284 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/Manifest.rdf#test001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/test001.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/test001.nt",
                                                  false) ;
        test212.addTest(test284) ;
        Test test285 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-para196/Manifest.rdf#test001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-para196/test001.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-para196/test001.nt",
                                                  false) ;
        test212.addTest(test285) ;
        Test test286 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-006",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-006.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-006.nt",
                                                  false) ;
        test212.addTest(test286) ;
        Test test287 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-007",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-007.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-007.nt",
                                                  false) ;
        test212.addTest(test287) ;
        Test test288 = testSuite
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/Manifest.rdf#error002",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/error002.rdf",
                                                  true, new int[]{204,}) ;
        test212.addTest(test288) ;
        Test test289 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/Manifest.rdf#test005",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/test005.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/test005.nt",
                                                  false) ;
        test212.addTest(test289) ;
        Test test290 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/Manifest.rdf#test004",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/test004.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/test004.nt",
                                                  false) ;
        test212.addTest(test290) ;
        Test test291 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-charmod-uris/Manifest.rdf#test001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-charmod-uris/test001.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-charmod-uris/test001.nt",
                                                  false) ;
        test212.addTest(test291) ;
        Test test292 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-002",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-002.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-002.nt",
                                                  false) ;
        test212.addTest(test292) ;
        Test test293 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-duplicate-member-props/Manifest.rdf#test001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-duplicate-member-props/test001.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-duplicate-member-props/test001.nt",
                                                  false) ;
        test212.addTest(test293) ;
        Test test294 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-003",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-003.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-003.nt",
                                                  false) ;
        test212.addTest(test294) ;
        Test test295 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-seq-representation/Manifest.rdf#test001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-seq-representation/test001.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-seq-representation/test001.nt",
                                                  false) ;
        test212.addTest(test295) ;
        Test test296 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/Manifest.rdf#test001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test001.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test001.nt",
                                                  false) ;
        test212.addTest(test296) ;
        Test test297 = testSuite
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-020",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-020.rdf",
                                                  true, new int[]{205,}) ;
        test212.addTest(test297) ;
        Test test298 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-015",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-015.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-015.nt",
                                                  false) ;
        test212.addTest(test298) ;
        Test test299 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-016",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-016.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-016.nt",
                                                  false) ;
        test212.addTest(test299) ;
        Test test300 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/Manifest.rdf#test1",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/test1.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/test1.nt",
                                                  false) ;
        test212.addTest(test300) ;
        Test test301 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test007",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test007.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test007.nt",
                                                  false) ;
        test212.addTest(test301) ;
        Test test302 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test008",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test008.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test008.nt",
                                                  false) ;
        test212.addTest(test302) ;
        Test test303 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test005",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test005.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test005.nt",
                                                  false) ;
        test212.addTest(test303) ;
        Test test304 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test006",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test006.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test006.nt",
                                                  false) ;
        test212.addTest(test304) ;
        Test test305 = testSuite
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/Manifest.rdf#error001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/error001.rdf",
                                                  true, null) ;
        test212.addTest(test305) ;
        Test test306 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-010",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-010.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-010.nt",
                                                  false) ;
        test212.addTest(test306) ;
        Test test307 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/Manifest.rdf#test003",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test003.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test003.nt",
                                                  false) ;
        test212.addTest(test307) ;
        Test test308 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/Manifest.rdf#test004",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test004.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test004.nt",
                                                  false) ;
        test212.addTest(test308) ;
        Test test309 = testSuite
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#error001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/error001.rdf",
                                                  true, new int[]{201,}) ;
        test212.addTest(test309) ;
        Test test310 = testSuite
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#error002",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/error002.rdf",
                                                  true, new int[]{201,}) ;
        test212.addTest(test310) ;
        Test test311 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-013",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-013.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-013.nt",
                                                  false) ;
        test212.addTest(test311) ;
        Test test312 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-014",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-014.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-014.nt",
                                                  false) ;
        test212.addTest(test312) ;
        Test test313 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-001.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-001.nt",
                                                  false) ;
        test212.addTest(test313) ;
        Test test314 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-charmod-literals/Manifest.rdf#test001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-charmod-literals/test001.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-charmod-literals/test001.nt",
                                                  false) ;
        test212.addTest(test314) ;
        Test test315 = testSuite
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-007",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-007.rdf",
                                                  true, new int[]{205,}) ;
        test212.addTest(test315) ;
        Test test316 = testSuite
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-006",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-006.rdf",
                                                  true, new int[]{205,}) ;
        test212.addTest(test316) ;
        Test test317 = testSuite
                              .createWarningTest(
                                                 "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#warn-003",
                                                 "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/warn-003.rdf",
                                                 true,
                                                 "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/warn-003.nt",
                                                 false, null) ;
        test212.addTest(test317) ;
        Test test318 = testSuite
                              .createWarningTest(
                                                 "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#warn-002",
                                                 "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/warn-002.rdf",
                                                 true,
                                                 "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/warn-002.nt",
                                                 false, null) ;
        test212.addTest(test318) ;
        Test test319 = testSuite
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-014",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-014.rdf",
                                                  true, new int[]{205,}) ;
        test212.addTest(test319) ;
        Test test320 = testSuite
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-013",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-013.rdf",
                                                  true, new int[]{205,}) ;
        test212.addTest(test320) ;
        Test test321 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/Manifest.rdf#test003",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test003.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test003.nt",
                                                  false) ;
        test212.addTest(test321) ;
        Test test322 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/Manifest.rdf#test004",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test004.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test004.nt",
                                                  false) ;
        test212.addTest(test322) ;
        Test test323 = testSuite
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-017",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-017.rdf",
                                                  true, new int[]{205,}) ;
        test212.addTest(test323) ;
        Test test324 = testSuite
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-018",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-018.rdf",
                                                  true, new int[]{205,}) ;
        test212.addTest(test324) ;
        Test test325 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test009",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test009.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test009.nt",
                                                  false) ;
        test212.addTest(test325) ;
        Test test326 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xml-literal-namespaces/Manifest.rdf#test002",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xml-literal-namespaces/test002.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xml-literal-namespaces/test002.nt",
                                                  false) ;
        test212.addTest(test326) ;
        Test test327 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/Manifest.rdf#test001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test001.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test001.nt",
                                                  false) ;
        test212.addTest(test327) ;
        Test test328 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/Manifest.rdf#test002",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test002.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test002.nt",
                                                  false) ;
        test212.addTest(test328) ;
        Test test329 = testSuite
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#error006",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/error006.rdf",
                                                  true, null) ;
        test212.addTest(test329) ;
        Test test330 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/Manifest.rdf#test008",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test008.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test008.nt",
                                                  false) ;
        test212.addTest(test330) ;
        Test test331 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-026",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-026.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-026.nt",
                                                  false) ;
        test212.addTest(test331) ;
        Test test332 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-027",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-027.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-027.nt",
                                                  false) ;
        test212.addTest(test332) ;
        Test test333 = testSuite
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-010",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-010.rdf",
                                                  true, new int[]{205,}) ;
        test212.addTest(test333) ;
        Test test334 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test011",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test011.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test011.nt",
                                                  false) ;
        test212.addTest(test334) ;
        Test test335 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test010",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test010.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test010.nt",
                                                  false) ;
        test212.addTest(test335) ;
        Test test336 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-019",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-019.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-019.nt",
                                                  false) ;
        test212.addTest(test336) ;
        Test test337 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test001.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test001.nt",
                                                  false) ;
        test212.addTest(test337) ;
        Test test338 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test002",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test002.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test002.nt",
                                                  false) ;
        test212.addTest(test338) ;
        Test test339 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0014",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0014.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0014.nt",
                                                  false) ;
        test212.addTest(test339) ;
        Test test340 = testSuite
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/Manifest.rdf#error001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/error001.rdf",
                                                  true, new int[]{206,}) ;
        test212.addTest(test340) ;
        Test test341 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#test003",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/test003.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/test003.nt",
                                                  false) ;
        test212.addTest(test341) ;
        Test test342 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#test002",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/test002.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/test002.nt",
                                                  false) ;
        test212.addTest(test342) ;
        Test test343 = testSuite
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-012",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-012.rdf",
                                                  true, new int[]{205,}) ;
        test212.addTest(test343) ;
        Test test344 = testSuite
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-011",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-011.rdf",
                                                  true, new int[]{205,}) ;
        test212.addTest(test344) ;
        Test test345 = testSuite
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#error001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/error001.rdf",
                                                  true, null) ;
        test212.addTest(test345) ;
        Test test346 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-034",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-034.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-034.nt",
                                                  false) ;
        test212.addTest(test346) ;
        Test test347 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-033",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-033.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-033.nt",
                                                  false) ;
        test212.addTest(test347) ;
        Test test348 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-037",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-037.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-037.nt",
                                                  false) ;
        test212.addTest(test348) ;
        Test test349 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-036",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-036.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-036.nt",
                                                  false) ;
        test212.addTest(test349) ;
        Test test350 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-035",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-035.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-035.nt",
                                                  false) ;
        test212.addTest(test350) ;
        Test test351 = testSuite
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#error003",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/error003.rdf",
                                                  true, null) ;
        test212.addTest(test351) ;
        Test test352 = testSuite
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#error002",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/error002.rdf",
                                                  true, null) ;
        test212.addTest(test352) ;
        Test test353 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/Manifest.rdf#test2",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/test2.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/test2.nt",
                                                  false) ;
        test212.addTest(test353) ;
        Test test354 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-008",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-008.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-008.nt",
                                                  false) ;
        test212.addTest(test354) ;
        Test test355 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-009",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-009.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-009.nt",
                                                  false) ;
        test212.addTest(test355) ;
        Test test356 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-032",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-032.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-032.nt",
                                                  false) ;
        test212.addTest(test356) ;
        Test test357 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-031",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-031.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-031.nt",
                                                  false) ;
        test212.addTest(test357) ;
        Test test358 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0001.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0001.nt",
                                                  false) ;
        test212.addTest(test358) ;
        Test test359 = testSuite
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-008",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-008.rdf",
                                                  true, new int[]{204,}) ;
        test212.addTest(test359) ;
        Test test360 = testSuite
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-009",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-009.rdf",
                                                  true, new int[]{205,}) ;
        test212.addTest(test360) ;
        Test test361 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test006",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test006.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test006.nt",
                                                  false) ;
        test212.addTest(test361) ;
        Test test362 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-uri-substructure/Manifest.rdf#test001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-uri-substructure/test001.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-uri-substructure/test001.nt",
                                                  false) ;
        test212.addTest(test362) ;
        Test test363 = testSuite
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#error003",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/error003.rdf",
                                                  true, new int[]{201,}) ;
        test212.addTest(test363) ;
        Test test364 = testSuite
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-002",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-002.rdf",
                                                  true, new int[]{205,}) ;
        test212.addTest(test364) ;
        Test test365 = testSuite
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-003",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-003.rdf",
                                                  true, new int[]{205,}) ;
        test212.addTest(test365) ;
        Test test366 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test013",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test013.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test013.nt",
                                                  false) ;
        test212.addTest(test366) ;
        Test test367 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-022",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-022.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-022.nt",
                                                  false) ;
        test212.addTest(test367) ;
        Test test368 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-023",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-023.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-023.nt",
                                                  false) ;
        test212.addTest(test368) ;
        Test test369 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-004",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-004.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-004.nt",
                                                  false) ;
        test212.addTest(test369) ;
        Test test370 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-005",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-005.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-005.nt",
                                                  false) ;
        test212.addTest(test370) ;
        Test test371 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/Manifest.rdf#test002",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/test002.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/test002.nt",
                                                  false) ;
        test212.addTest(test371) ;
        Test test372 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test014",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test014.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test014.nt",
                                                  false) ;
        test212.addTest(test372) ;
        Test test373 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test015",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test015.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test015.nt",
                                                  false) ;
        test212.addTest(test373) ;
        Test test374 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-charmod-uris/Manifest.rdf#test002",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-charmod-uris/test002.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-charmod-uris/test002.nt",
                                                  false) ;
        test212.addTest(test374) ;
        Test test375 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0011",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0011.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0011.nt",
                                                  false) ;
        test212.addTest(test375) ;
        Test test376 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0010",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0010.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0010.nt",
                                                  false) ;
        test212.addTest(test376) ;
        Test test377 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-012",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-012.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-012.nt",
                                                  false) ;
        test212.addTest(test377) ;
        Test test378 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-011",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-011.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-011.nt",
                                                  false) ;
        test212.addTest(test378) ;
        testSuite.addTest(test212) ;
        TestSuite test379 = new TestSuite("PENDING") ;
        Test test380 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test016",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test016.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test016.nt",
                                                  false) ;
        test379.addTest(test380) ;
        Test test381 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test017",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test017.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test017.nt",
                                                  false) ;
        test379.addTest(test381) ;
        Test test382 = testSuite
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/Manifest.rdf#test3",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/test3.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/test3.nt",
                                                  false) ;
        test379.addTest(test382) ;
        testSuite.addTest(test379) ;
        // TODO: not for 2.3. stop this being generated.
        // TestSuite test383 = new TestSuite("WITHDRAWN");
        // Test test384 =
        // test211.createPositiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test012","http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test012.rdf",true,"http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test012.nt",false);
        // test383.addTest(test384);
        // test211.addTest(test383);
        return testSuite;
        // return test206;
    }
}
