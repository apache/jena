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

package org.apache.jena.rdfxml.xmlinput1 ;

import junit.framework.Test ;
import junit.framework.TestSuite ;
import org.apache.jena.rdfxml.libtest.InputStreamFactoryTests;

public class TestSuiteWG_RDFXML
{
    static public Test suite()
    {
        TestSuite testSuite = new TestSuite("WG RDF/XML") ;
        WGTestSuite testSuiteWG =
            new org.apache.jena.rdfxml.xmlinput1.WGTestSuite(
                                                         new InputStreamFactoryTests("http://www.w3.org/2000/10/rdf-tests/rdfcore/", "wg"),
                                                         "WG Parser Tests", false) ;
        TestSuite testSuiteApproved = new TestSuite("APPROVED") ;

        Test test3 = testSuiteWG
                         .createPositiveTest(
                                             "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-024",
                                             "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-024.rdf",
                                             true,
                                             "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-024.nt",
                                             false) ;
        testSuiteApproved.addTest(test3) ;
        Test test4 = testSuiteWG
                          .createPositiveTest(
                                              "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-025",
                                              "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-025.rdf",
                                              true,
                                              "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-025.nt",
                                              false) ;
        testSuiteApproved.addTest(test4) ;
        Test test5 = testSuiteWG
                          .createPositiveTest(
                                              "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test003",
                                              "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test003.rdf",
                                              true,
                                              "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test003.nt",
                                              false) ;
        testSuiteApproved.addTest(test5) ;
        Test test6 = testSuiteWG
                          .createPositiveTest(
                                              "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test004",
                                              "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test004.rdf",
                                              true,
                                              "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test004.nt",
                                              false) ;
        testSuiteApproved.addTest(test6) ;
        Test test7 = testSuiteWG
                          .createNegativeTest(
                                              "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-019",
                                              "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-019.rdf",
                                              true, new int[]{205,}) ;
        testSuiteApproved.addTest(test7) ;
        Test test8 = testSuiteWG
                          .createPositiveTest(
                                              "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-017",
                                              "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-017.rdf",
                                              true,
                                              "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-017.nt",
                                              false) ;
        testSuiteApproved.addTest(test8) ;
        Test test9 = testSuiteWG
                          .createPositiveTest(
                                              "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-018",
                                              "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-018.rdf",
                                              true,
                                              "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-018.nt",
                                              false) ;
        testSuiteApproved.addTest(test9) ;
        Test test10 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-element-not-mandatory/Manifest.rdf#test001",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-element-not-mandatory/test001.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-element-not-mandatory/test001.nt",
                                               false) ;
        testSuiteApproved.addTest(test10) ;
        Test test11 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/datatypes/Manifest.rdf#test001",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/datatypes/test001.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/datatypes/test001.nt",
                                               false) ;
        testSuiteApproved.addTest(test11) ;
        Test test12 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/Manifest.rdf#test005",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test005.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test005.nt",
                                               false) ;
        testSuiteApproved.addTest(test12) ;
        Test test13 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/Manifest.rdf#test006",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test006.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test006.nt",
                                               false) ;
        testSuiteApproved.addTest(test13) ;
        Test test14 = testSuiteWG
                           .createNegativeTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-001",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-001.rdf",
                                               true, new int[]{205,}) ;
        testSuiteApproved.addTest(test14) ;
        Test test15 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test011",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test011.rdf", true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test011.nt", false) ;
        testSuiteApproved.addTest(test15) ;
        Test test16 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test010",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test010.rdf", true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test010.nt", false) ;
        testSuiteApproved.addTest(test16) ;
        Test test17 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0013",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0013.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0013.nt",
                                               false) ;
        testSuiteApproved.addTest(test17) ;
        Test test18 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0012",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0012.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0012.nt",
                                               false) ;
        testSuiteApproved.addTest(test18) ;
        Test test19 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/unrecognised-xml-attributes/Manifest.rdf#test002",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/unrecognised-xml-attributes/test002.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/unrecognised-xml-attributes/test002.nt",
                                               false) ;
        testSuiteApproved.addTest(test19) ;
        Test test20 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/unrecognised-xml-attributes/Manifest.rdf#test001",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/unrecognised-xml-attributes/test001.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/unrecognised-xml-attributes/test001.nt",
                                               false) ;
        testSuiteApproved.addTest(test20) ;
        Test test21 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test002",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test002.rdf", true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test002.nt", false) ;
        testSuiteApproved.addTest(test21) ;
        Test test22 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test001",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test001.rdf", true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test001.nt", false) ;
        testSuiteApproved.addTest(test22) ;
        Test test23 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfs-domain-and-range/Manifest.rdf#test002",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfs-domain-and-range/test002.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfs-domain-and-range/test002.nt",
                                               false) ;
        testSuiteApproved.addTest(test23) ;
        Test test24 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfs-domain-and-range/Manifest.rdf#test001",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfs-domain-and-range/test001.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfs-domain-and-range/test001.nt",
                                               false) ;
        testSuiteApproved.addTest(test24) ;
        Test test25 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xml-literal-namespaces/Manifest.rdf#test001",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xml-literal-namespaces/test001.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xml-literal-namespaces/test001.nt",
                                               false) ;
        testSuiteApproved.addTest(test25) ;
        Test test26 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test009",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test009.rdf", true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test009.nt", false) ;
        testSuiteApproved.addTest(test26) ;
        Test test27 = testSuiteWG
                           .createNegativeTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-004",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-004.rdf",
                                               true, new int[]{205,}) ;
        testSuiteApproved.addTest(test27) ;
        Test test28 = testSuiteWG
                           .createNegativeTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-005",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-005.rdf",
                                               true, new int[]{205,}) ;
        testSuiteApproved.addTest(test28) ;
        Test test29 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-028",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-028.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-028.nt",
                                               false) ;
        testSuiteApproved.addTest(test29) ;
        Test test30 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-029",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-029.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-029.nt",
                                               false) ;
        testSuiteApproved.addTest(test30) ;
        Test test31 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test012",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test012.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test012.nt",
                                               false) ;
        testSuiteApproved.addTest(test31) ;
        Test test32 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test013",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test013.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test013.nt",
                                               false) ;
        testSuiteApproved.addTest(test32) ;
        Test test33 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/Manifest.rdf#test002",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test002.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test002.nt",
                                               false) ;
        testSuiteApproved.addTest(test33) ;
        Test test34 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/Manifest.rdf#test003",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test003.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test003.nt",
                                               false) ;
        testSuiteApproved.addTest(test34) ;
        Test test35 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0006",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0006.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0006.nt",
                                               false) ;
        testSuiteApproved.addTest(test35) ;
        Test test36 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0005",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0005.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0005.nt",
                                               false) ;
        testSuiteApproved.addTest(test36) ;
        Test test37 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-reification-required/Manifest.rdf#test001",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-reification-required/test001.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-reification-required/test001.nt",
                                               false) ;
        testSuiteApproved.addTest(test37) ;
        Test test38 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-021",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-021.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-021.nt",
                                               false) ;
        testSuiteApproved.addTest(test38) ;
        Test test39 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-020",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-020.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-020.nt",
                                               false) ;
        testSuiteApproved.addTest(test39) ;
        Test test40 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test008",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test008.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test008.nt",
                                               false) ;
        testSuiteApproved.addTest(test40) ;
        Test test41 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test007",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test007.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test007.nt",
                                               false) ;
        testSuiteApproved.addTest(test41) ;
        Test test42 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/Manifest.rdf#test006",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test006.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test006.nt",
                                               false) ;
        testSuiteApproved.addTest(test42) ;
        Test test43 = testSuiteWG
                           .createNegativeTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-016",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-016.rdf",
                                               true, new int[]{205,}) ;
        testSuiteApproved.addTest(test43) ;
        Test test44 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/Manifest.rdf#test007",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test007.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test007.nt",
                                               false) ;
        testSuiteApproved.addTest(test44) ;
        Test test45 = testSuiteWG
                           .createNegativeTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-015",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-015.rdf",
                                               true, new int[]{205,}) ;
        testSuiteApproved.addTest(test45) ;
        Test test46 = testSuiteWG
                           .createNegativeTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#error005",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/error005.rdf",
                                               true, null) ;
        testSuiteApproved.addTest(test46) ;
        Test test47 = testSuiteWG
                           .createNegativeTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#error004",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/error004.rdf",
                                               true, null) ;
        testSuiteApproved.addTest(test47) ;
        Test test48 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0003",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0003.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0003.nt",
                                               false) ;
        testSuiteApproved.addTest(test48) ;
        Test test49 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0004",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0004.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0004.nt",
                                               false) ;
        testSuiteApproved.addTest(test49) ;
        Test test50 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/Manifest.rdf#test005",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test005.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test005.nt",
                                               false) ;
        testSuiteApproved.addTest(test50) ;
        Test test51 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test014",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test014.rdf", true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test014.nt", false) ;
        testSuiteApproved.addTest(test51) ;
        Test test52 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0009",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0009.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0009.nt",
                                               false) ;
        testSuiteApproved.addTest(test52) ;
        Test test53 = testSuiteWG
                           .createNegativeTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/Manifest.rdf#error004",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/error004.rdf",
                                               true, null) ;
        testSuiteApproved.addTest(test53) ;
        Test test54 = testSuiteWG
                           .createNegativeTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/Manifest.rdf#error005",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/error005.rdf",
                                               true, null) ;
        testSuiteApproved.addTest(test54) ;
        Test test55 = testSuiteWG
                           .createNegativeTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/Manifest.rdf#error007",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/error007.rdf",
                                               true, null) ;
        testSuiteApproved.addTest(test55) ;
        Test test56 = testSuiteWG
                           .createNegativeTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/Manifest.rdf#error006",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/error006.rdf",
                                               true, null) ;
        testSuiteApproved.addTest(test56) ;
        Test test57 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/amp-in-url/Manifest.rdf#test001",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/amp-in-url/test001.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/amp-in-url/test001.nt",
                                               false) ;
        testSuiteApproved.addTest(test57) ;
        Test test58 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#test001",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/test001.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/test001.nt",
                                               false) ;
        testSuiteApproved.addTest(test58) ;
        Test test59 = testSuiteWG
                           .createNegativeTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-abouteach/Manifest.rdf#error002",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-abouteach/error002.rdf",
                                               true, new int[]{206,}) ;
        testSuiteApproved.addTest(test59) ;
        Test test60 = testSuiteWG
                           .createNegativeTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/Manifest.rdf#error002",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/error002.rdf",
                                               true, null) ;
        testSuiteApproved.addTest(test60) ;
        Test test61 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/Manifest.rdf#test004",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test004.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test004.nt",
                                               false) ;
        testSuiteApproved.addTest(test61) ;
        Test test62 = testSuiteWG
                           .createNegativeTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/Manifest.rdf#error003",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/error003.rdf",
                                               true, null) ;
        testSuiteApproved.addTest(test62) ;
        Test test63 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/datatypes/Manifest.rdf#test002",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/datatypes/test002.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/datatypes/test002.nt",
                                               false) ;
        testSuiteApproved.addTest(test63) ;
        Test test64 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test004",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test004.rdf", true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test004.nt", false) ;
        testSuiteApproved.addTest(test64) ;
        Test test65 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test003",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test003.rdf", true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test003.nt", false) ;
        testSuiteApproved.addTest(test65) ;
        Test test66 = testSuiteWG
                           .createNegativeTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/Manifest.rdf#error1",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/error1.rdf",
                                               true, new int[]{105,}) ;
        testSuiteApproved.addTest(test66) ;
        Test test67 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-030",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-030.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-030.nt",
                                               false) ;
        testSuiteApproved.addTest(test67) ;
        Test test68 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/Manifest.rdf#test001",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test001.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test001.nt",
                                               false) ;
        testSuiteApproved.addTest(test68) ;
        Test test69 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/Manifest.rdf#test002",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test002.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test002.nt",
                                               false) ;
        testSuiteApproved.addTest(test69) ;
        Test test70 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#test004",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/test004.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/test004.nt",
                                               false) ;
        testSuiteApproved.addTest(test70) ;
        Test test71 = testSuiteWG
                           .createWarningTest(
                                              "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#warn-001",
                                              "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/warn-001.rdf",
                                              true,
                                              "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/warn-001.nt",
                                              false, null) ;
        testSuiteApproved.addTest(test71) ;
        Test test72 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xml-canon/Manifest.rdf#test001",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xml-canon/test001.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xml-canon/test001.nt",
                                               false) ;
        testSuiteApproved.addTest(test72) ;
        Test test73 = testSuiteWG
                           .createNegativeTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-abouteach/Manifest.rdf#error001",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-abouteach/error001.rdf",
                                               true, new int[]{206,}) ;
        testSuiteApproved.addTest(test73) ;
        Test test74 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/Manifest.rdf#test001",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/test001.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/test001.nt",
                                               false) ;
        testSuiteApproved.addTest(test74) ;
        Test test75 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-para196/Manifest.rdf#test001",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-para196/test001.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-para196/test001.nt",
                                               false) ;
        testSuiteApproved.addTest(test75) ;
        Test test76 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-006",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-006.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-006.nt",
                                               false) ;
        testSuiteApproved.addTest(test76) ;
        Test test77 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-007",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-007.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-007.nt",
                                               false) ;
        testSuiteApproved.addTest(test77) ;
        Test test78 = testSuiteWG
                           .createNegativeTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/Manifest.rdf#error002",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/error002.rdf",
                                               true, new int[]{204,}) ;
        testSuiteApproved.addTest(test78) ;
        Test test79 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/Manifest.rdf#test005",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/test005.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/test005.nt",
                                               false) ;
        testSuiteApproved.addTest(test79) ;
        Test test80 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/Manifest.rdf#test004",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/test004.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/test004.nt",
                                               false) ;
        testSuiteApproved.addTest(test80) ;
        Test test81 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-charmod-uris/Manifest.rdf#test001",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-charmod-uris/test001.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-charmod-uris/test001.nt",
                                               false) ;
        testSuiteApproved.addTest(test81) ;
        Test test82 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-002",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-002.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-002.nt",
                                               false) ;
        testSuiteApproved.addTest(test82) ;
        Test test83 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-duplicate-member-props/Manifest.rdf#test001",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-duplicate-member-props/test001.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-duplicate-member-props/test001.nt",
                                               false) ;
        testSuiteApproved.addTest(test83) ;
        Test test84 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-003",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-003.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-003.nt",
                                               false) ;
        testSuiteApproved.addTest(test84) ;
        Test test85 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-seq-representation/Manifest.rdf#test001",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-seq-representation/test001.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-seq-representation/test001.nt",
                                               false) ;
        testSuiteApproved.addTest(test85) ;
        Test test86 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/Manifest.rdf#test001",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test001.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test001.nt",
                                               false) ;
        testSuiteApproved.addTest(test86) ;
        Test test87 = testSuiteWG
                           .createNegativeTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-020",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-020.rdf",
                                               true, new int[]{205,}) ;
        testSuiteApproved.addTest(test87) ;
        Test test88 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-015",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-015.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-015.nt",
                                               false) ;
        testSuiteApproved.addTest(test88) ;
        Test test89 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-016",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-016.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-016.nt",
                                               false) ;
        testSuiteApproved.addTest(test89) ;
        Test test90 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/Manifest.rdf#test1",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/test1.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/test1.nt",
                                               false) ;
        testSuiteApproved.addTest(test90) ;
        Test test91 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test007",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test007.rdf", true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test007.nt", false) ;
        testSuiteApproved.addTest(test91) ;
        Test test92 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test008",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test008.rdf", true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test008.nt", false) ;
        testSuiteApproved.addTest(test92) ;
        Test test93 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test005",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test005.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test005.nt",
                                               false) ;
        testSuiteApproved.addTest(test93) ;
        Test test94 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test006",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test006.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test006.nt",
                                               false) ;
        testSuiteApproved.addTest(test94) ;
        Test test95 = testSuiteWG
                           .createNegativeTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/Manifest.rdf#error001",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/error001.rdf",
                                               true, null) ;
        testSuiteApproved.addTest(test95) ;
        Test test96 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-010",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-010.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-010.nt",
                                               false) ;
        testSuiteApproved.addTest(test96) ;
        Test test97 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/Manifest.rdf#test003",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test003.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test003.nt",
                                               false) ;
        testSuiteApproved.addTest(test97) ;
        Test test98 = testSuiteWG
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/Manifest.rdf#test004",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test004.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test004.nt",
                                               false) ;
        testSuiteApproved.addTest(test98) ;
        Test test99 = testSuiteWG
                           .createNegativeTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#error001",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/error001.rdf",
                                               true, new int[]{201,}) ;
        testSuiteApproved.addTest(test99) ;
        Test test100 = testSuiteWG
                            .createNegativeTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#error002",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/error002.rdf",
                                                true, new int[]{201,}) ;
        testSuiteApproved.addTest(test100) ;
        Test test101 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-013",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-013.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-013.nt",
                                                false) ;
        testSuiteApproved.addTest(test101) ;
        Test test102 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-014",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-014.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-014.nt",
                                                false) ;
        testSuiteApproved.addTest(test102) ;
        Test test103 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-001",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-001.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-001.nt",
                                                false) ;
        testSuiteApproved.addTest(test103) ;
        Test test104 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-charmod-literals/Manifest.rdf#test001",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-charmod-literals/test001.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-charmod-literals/test001.nt",
                                                false) ;
        testSuiteApproved.addTest(test104) ;
        Test test105 = testSuiteWG
                            .createNegativeTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-007",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-007.rdf",
                                                true, new int[]{205,}) ;
        testSuiteApproved.addTest(test105) ;
        Test test106 = testSuiteWG
                            .createNegativeTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-006",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-006.rdf",
                                                true, new int[]{205,}) ;
        testSuiteApproved.addTest(test106) ;
        Test test107 = testSuiteWG
                            .createWarningTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#warn-003",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/warn-003.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/warn-003.nt",
                                               false, null) ;
        testSuiteApproved.addTest(test107) ;
        Test test108 = testSuiteWG
                            .createWarningTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#warn-002",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/warn-002.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/warn-002.nt",
                                               false, null) ;
        testSuiteApproved.addTest(test108) ;
        Test test109 = testSuiteWG
                            .createNegativeTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-014",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-014.rdf",
                                                true, new int[]{205,}) ;
        testSuiteApproved.addTest(test109) ;
        Test test110 = testSuiteWG
                            .createNegativeTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-013",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-013.rdf",
                                                true, new int[]{205,}) ;
        testSuiteApproved.addTest(test110) ;
        Test test111 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/Manifest.rdf#test003",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test003.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test003.nt",
                                                false) ;
        testSuiteApproved.addTest(test111) ;
        Test test112 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/Manifest.rdf#test004",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test004.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test004.nt",
                                                false) ;
        testSuiteApproved.addTest(test112) ;
        Test test113 = testSuiteWG
                            .createNegativeTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-017",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-017.rdf",
                                                true, new int[]{205,}) ;
        testSuiteApproved.addTest(test113) ;
        Test test114 = testSuiteWG
                            .createNegativeTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-018",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-018.rdf",
                                                true, new int[]{205,}) ;
        testSuiteApproved.addTest(test114) ;
        Test test115 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test009",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test009.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test009.nt",
                                                false) ;
        testSuiteApproved.addTest(test115) ;
        Test test116 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xml-literal-namespaces/Manifest.rdf#test002",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xml-literal-namespaces/test002.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xml-literal-namespaces/test002.nt",
                                                false) ;
        testSuiteApproved.addTest(test116) ;
        Test test117 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/Manifest.rdf#test001",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test001.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test001.nt",
                                                false) ;
        testSuiteApproved.addTest(test117) ;
        Test test118 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/Manifest.rdf#test002",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test002.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test002.nt",
                                                false) ;
        testSuiteApproved.addTest(test118) ;
        Test test119 = testSuiteWG
                            .createNegativeTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#error006",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/error006.rdf",
                                                true, null) ;
        testSuiteApproved.addTest(test119) ;
        Test test120 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/Manifest.rdf#test008",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test008.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test008.nt",
                                                false) ;
        testSuiteApproved.addTest(test120) ;
        Test test121 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-026",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-026.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-026.nt",
                                                false) ;
        testSuiteApproved.addTest(test121) ;
        Test test122 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-027",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-027.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-027.nt",
                                                false) ;
        testSuiteApproved.addTest(test122) ;
        Test test123 = testSuiteWG
                            .createNegativeTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-010",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-010.rdf",
                                                true, new int[]{205,}) ;
        testSuiteApproved.addTest(test123) ;
        Test test124 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test011",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test011.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test011.nt",
                                                false) ;
        testSuiteApproved.addTest(test124) ;
        Test test125 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test010",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test010.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test010.nt",
                                                false) ;
        testSuiteApproved.addTest(test125) ;
        Test test126 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-019",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-019.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-019.nt",
                                                false) ;
        testSuiteApproved.addTest(test126) ;
        Test test127 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test001",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test001.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test001.nt",
                                                false) ;
        testSuiteApproved.addTest(test127) ;
        Test test128 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test002",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test002.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test002.nt",
                                                false) ;
        testSuiteApproved.addTest(test128) ;
        Test test129 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0014",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0014.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0014.nt",
                                                false) ;
        testSuiteApproved.addTest(test129) ;
        Test test130 = testSuiteWG
                            .createNegativeTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/Manifest.rdf#error001",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/error001.rdf",
                                                true, new int[]{206,}) ;
        testSuiteApproved.addTest(test130) ;
        Test test131 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#test003",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/test003.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/test003.nt",
                                                false) ;
        testSuiteApproved.addTest(test131) ;
        Test test132 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#test002",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/test002.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/test002.nt",
                                                false) ;
        testSuiteApproved.addTest(test132) ;
        Test test133 = testSuiteWG
                            .createNegativeTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-012",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-012.rdf",
                                                true, new int[]{205,}) ;
        testSuiteApproved.addTest(test133) ;
        Test test134 = testSuiteWG
                            .createNegativeTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-011",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-011.rdf",
                                                true, new int[]{205,}) ;
        testSuiteApproved.addTest(test134) ;
        Test test135 = testSuiteWG
                            .createNegativeTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#error001",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/error001.rdf",
                                                true, null) ;
        testSuiteApproved.addTest(test135) ;
        Test test136 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-034",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-034.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-034.nt",
                                                false) ;
        testSuiteApproved.addTest(test136) ;
        Test test137 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-033",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-033.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-033.nt",
                                                false) ;
        testSuiteApproved.addTest(test137) ;
        Test test138 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-037",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-037.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-037.nt",
                                                false) ;
        testSuiteApproved.addTest(test138) ;
        Test test139 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-036",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-036.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-036.nt",
                                                false) ;
        testSuiteApproved.addTest(test139) ;
        Test test140 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-035",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-035.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-035.nt",
                                                false) ;
        testSuiteApproved.addTest(test140) ;
        Test test141 = testSuiteWG
                            .createNegativeTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#error003",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/error003.rdf",
                                                true, null) ;
        testSuiteApproved.addTest(test141) ;
        Test test142 = testSuiteWG
                            .createNegativeTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#error002",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/error002.rdf",
                                                true, null) ;
        testSuiteApproved.addTest(test142) ;
        Test test143 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/Manifest.rdf#test2",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/test2.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/test2.nt",
                                                false) ;
        testSuiteApproved.addTest(test143) ;
        Test test144 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-008",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-008.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-008.nt",
                                                false) ;
        testSuiteApproved.addTest(test144) ;
        Test test145 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-009",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-009.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-009.nt",
                                                false) ;
        testSuiteApproved.addTest(test145) ;
        Test test146 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-032",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-032.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-032.nt",
                                                false) ;
        testSuiteApproved.addTest(test146) ;
        Test test147 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-031",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-031.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-031.nt",
                                                false) ;
        testSuiteApproved.addTest(test147) ;
        Test test148 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0001",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0001.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0001.nt",
                                                false) ;
        testSuiteApproved.addTest(test148) ;
        Test test149 = testSuiteWG
                            .createNegativeTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-008",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-008.rdf",
                                                true, new int[]{204,}) ;
        testSuiteApproved.addTest(test149) ;
        Test test150 = testSuiteWG
                            .createNegativeTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-009",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-009.rdf",
                                                true, new int[]{205,}) ;
        testSuiteApproved.addTest(test150) ;
        Test test151 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test006",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test006.rdf",
                                                true, "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test006.nt",
                                                false) ;
        testSuiteApproved.addTest(test151) ;
        Test test152 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-uri-substructure/Manifest.rdf#test001",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-uri-substructure/test001.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-uri-substructure/test001.nt",
                                                false) ;
        testSuiteApproved.addTest(test152) ;
        Test test153 = testSuiteWG
                            .createNegativeTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#error003",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/error003.rdf",
                                                true, new int[]{201,}) ;
        testSuiteApproved.addTest(test153) ;
        Test test154 = testSuiteWG
                            .createNegativeTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-002",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-002.rdf",
                                                true, new int[]{205,}) ;
        testSuiteApproved.addTest(test154) ;
        Test test155 = testSuiteWG
                            .createNegativeTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-003",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-003.rdf",
                                                true, new int[]{205,}) ;
        testSuiteApproved.addTest(test155) ;
        Test test156 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test013",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test013.rdf",
                                                true, "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test013.nt",
                                                false) ;
        testSuiteApproved.addTest(test156) ;
        Test test157 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-022",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-022.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-022.nt",
                                                false) ;
        testSuiteApproved.addTest(test157) ;
        Test test158 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-023",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-023.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-023.nt",
                                                false) ;
        testSuiteApproved.addTest(test158) ;
        Test test159 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-004",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-004.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-004.nt",
                                                false) ;
        testSuiteApproved.addTest(test159) ;
        Test test160 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-005",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-005.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-005.nt",
                                                false) ;
        testSuiteApproved.addTest(test160) ;
        Test test161 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/Manifest.rdf#test002",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/test002.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/test002.nt",
                                                false) ;
        testSuiteApproved.addTest(test161) ;
        Test test162 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test014",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test014.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test014.nt",
                                                false) ;
        testSuiteApproved.addTest(test162) ;
        Test test163 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test015",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test015.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test015.nt",
                                                false) ;
        testSuiteApproved.addTest(test163) ;
        Test test164 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-charmod-uris/Manifest.rdf#test002",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-charmod-uris/test002.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-charmod-uris/test002.nt",
                                                false) ;
        testSuiteApproved.addTest(test164) ;
        Test test165 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0011",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0011.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0011.nt",
                                                false) ;
        testSuiteApproved.addTest(test165) ;
        Test test166 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0010",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0010.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0010.nt",
                                                false) ;
        testSuiteApproved.addTest(test166) ;
        Test test167 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-012",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-012.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-012.nt",
                                                false) ;
        testSuiteApproved.addTest(test167) ;
        Test test168 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-011",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-011.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-011.nt",
                                                false) ;
        testSuiteApproved.addTest(test168) ;
        testSuiteWG.addTest(testSuiteApproved) ;
        TestSuite testSuitePending = new TestSuite("PENDING") ;
        Test test170 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test016",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test016.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test016.nt",
                                                false) ;
        testSuitePending.addTest(test170) ;
        Test test171 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test017",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test017.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test017.nt",
                                                false) ;
        testSuitePending.addTest(test171) ;
        Test test172 = testSuiteWG
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/Manifest.rdf#test3",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/test3.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/test3.nt",
                                                false) ;
        testSuitePending.addTest(test172) ;
        testSuiteWG.addTest(testSuitePending) ;
        // TestSuite test173 = new TestSuite("WITHDRAWN");
        // Test test174 =
        // test1.createPositiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test012","http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test012.rdf",true,"http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test012.nt",false);
        // test173.addTest(test174);
        // test1.addTest(test173);
        testSuite.addTest(testSuiteWG) ;

        return testSuite ;
    }
}
