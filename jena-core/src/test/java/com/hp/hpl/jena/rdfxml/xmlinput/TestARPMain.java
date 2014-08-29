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

package com.hp.hpl.jena.rdfxml.xmlinput ;

import junit.framework.Test ;
import junit.framework.TestSuite ;

import org.apache.jena.iri.IRIFactory ;
import com.hp.hpl.jena.shared.wg.TestInputStreamFactory ;

public class TestARPMain
{
    static public Test suite()
    {
        TestSuite test0 = new TestSuite("ARP") ;
        WGTestSuite test1 = 
            new com.hp.hpl.jena.rdfxml.xmlinput.WGTestSuite(
                                                         new TestInputStreamFactory(
                                                                                    IRIFactory
                                                                                    .iriImplementation()
                                                                                    .create(
                                                                                    "http://www.w3.org/2000/10/rdf-tests/rdfcore/"),
                                                         "wg"),
                                                         "WG Parser Tests", false) ;
        TestSuite test2 = new TestSuite("APPROVED") ;
        Test test3 = test1
                          .createPositiveTest(
                                              "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-024",
                                              "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-024.rdf",
                                              true,
                                              "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-024.nt",
                                              false) ;
        test2.addTest(test3) ;
        Test test4 = test1
                          .createPositiveTest(
                                              "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-025",
                                              "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-025.rdf",
                                              true,
                                              "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-025.nt",
                                              false) ;
        test2.addTest(test4) ;
        Test test5 = test1
                          .createPositiveTest(
                                              "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test003",
                                              "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test003.rdf",
                                              true,
                                              "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test003.nt",
                                              false) ;
        test2.addTest(test5) ;
        Test test6 = test1
                          .createPositiveTest(
                                              "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test004",
                                              "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test004.rdf",
                                              true,
                                              "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test004.nt",
                                              false) ;
        test2.addTest(test6) ;
        Test test7 = test1
                          .createNegativeTest(
                                              "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-019",
                                              "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-019.rdf",
                                              true, new int[]{205,}) ;
        test2.addTest(test7) ;
        Test test8 = test1
                          .createPositiveTest(
                                              "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-017",
                                              "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-017.rdf",
                                              true,
                                              "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-017.nt",
                                              false) ;
        test2.addTest(test8) ;
        Test test9 = test1
                          .createPositiveTest(
                                              "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-018",
                                              "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-018.rdf",
                                              true,
                                              "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-018.nt",
                                              false) ;
        test2.addTest(test9) ;
        Test test10 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-element-not-mandatory/Manifest.rdf#test001",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-element-not-mandatory/test001.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-element-not-mandatory/test001.nt",
                                               false) ;
        test2.addTest(test10) ;
        Test test11 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/datatypes/Manifest.rdf#test001",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/datatypes/test001.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/datatypes/test001.nt",
                                               false) ;
        test2.addTest(test11) ;
        Test test12 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/Manifest.rdf#test005",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test005.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test005.nt",
                                               false) ;
        test2.addTest(test12) ;
        Test test13 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/Manifest.rdf#test006",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test006.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test006.nt",
                                               false) ;
        test2.addTest(test13) ;
        Test test14 = test1
                           .createNegativeTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-001",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-001.rdf",
                                               true, new int[]{205,}) ;
        test2.addTest(test14) ;
        Test test15 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test011",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test011.rdf", true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test011.nt", false) ;
        test2.addTest(test15) ;
        Test test16 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test010",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test010.rdf", true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test010.nt", false) ;
        test2.addTest(test16) ;
        Test test17 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0013",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0013.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0013.nt",
                                               false) ;
        test2.addTest(test17) ;
        Test test18 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0012",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0012.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0012.nt",
                                               false) ;
        test2.addTest(test18) ;
        Test test19 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/unrecognised-xml-attributes/Manifest.rdf#test002",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/unrecognised-xml-attributes/test002.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/unrecognised-xml-attributes/test002.nt",
                                               false) ;
        test2.addTest(test19) ;
        Test test20 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/unrecognised-xml-attributes/Manifest.rdf#test001",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/unrecognised-xml-attributes/test001.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/unrecognised-xml-attributes/test001.nt",
                                               false) ;
        test2.addTest(test20) ;
        Test test21 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test002",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test002.rdf", true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test002.nt", false) ;
        test2.addTest(test21) ;
        Test test22 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test001",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test001.rdf", true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test001.nt", false) ;
        test2.addTest(test22) ;
        Test test23 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfs-domain-and-range/Manifest.rdf#test002",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfs-domain-and-range/test002.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfs-domain-and-range/test002.nt",
                                               false) ;
        test2.addTest(test23) ;
        Test test24 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfs-domain-and-range/Manifest.rdf#test001",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfs-domain-and-range/test001.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfs-domain-and-range/test001.nt",
                                               false) ;
        test2.addTest(test24) ;
        Test test25 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xml-literal-namespaces/Manifest.rdf#test001",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xml-literal-namespaces/test001.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xml-literal-namespaces/test001.nt",
                                               false) ;
        test2.addTest(test25) ;
        Test test26 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test009",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test009.rdf", true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test009.nt", false) ;
        test2.addTest(test26) ;
        Test test27 = test1
                           .createNegativeTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-004",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-004.rdf",
                                               true, new int[]{205,}) ;
        test2.addTest(test27) ;
        Test test28 = test1
                           .createNegativeTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-005",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-005.rdf",
                                               true, new int[]{205,}) ;
        test2.addTest(test28) ;
        Test test29 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-028",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-028.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-028.nt",
                                               false) ;
        test2.addTest(test29) ;
        Test test30 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-029",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-029.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-029.nt",
                                               false) ;
        test2.addTest(test30) ;
        Test test31 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test012",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test012.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test012.nt",
                                               false) ;
        test2.addTest(test31) ;
        Test test32 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test013",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test013.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test013.nt",
                                               false) ;
        test2.addTest(test32) ;
        Test test33 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/Manifest.rdf#test002",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test002.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test002.nt",
                                               false) ;
        test2.addTest(test33) ;
        Test test34 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/Manifest.rdf#test003",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test003.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test003.nt",
                                               false) ;
        test2.addTest(test34) ;
        Test test35 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0006",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0006.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0006.nt",
                                               false) ;
        test2.addTest(test35) ;
        Test test36 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0005",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0005.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0005.nt",
                                               false) ;
        test2.addTest(test36) ;
        Test test37 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-reification-required/Manifest.rdf#test001",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-reification-required/test001.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-reification-required/test001.nt",
                                               false) ;
        test2.addTest(test37) ;
        Test test38 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-021",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-021.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-021.nt",
                                               false) ;
        test2.addTest(test38) ;
        Test test39 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-020",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-020.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-020.nt",
                                               false) ;
        test2.addTest(test39) ;
        Test test40 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test008",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test008.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test008.nt",
                                               false) ;
        test2.addTest(test40) ;
        Test test41 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test007",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test007.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test007.nt",
                                               false) ;
        test2.addTest(test41) ;
        Test test42 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/Manifest.rdf#test006",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test006.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test006.nt",
                                               false) ;
        test2.addTest(test42) ;
        Test test43 = test1
                           .createNegativeTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-016",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-016.rdf",
                                               true, new int[]{205,}) ;
        test2.addTest(test43) ;
        Test test44 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/Manifest.rdf#test007",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test007.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test007.nt",
                                               false) ;
        test2.addTest(test44) ;
        Test test45 = test1
                           .createNegativeTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-015",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-015.rdf",
                                               true, new int[]{205,}) ;
        test2.addTest(test45) ;
        Test test46 = test1
                           .createNegativeTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#error005",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/error005.rdf",
                                               true, null) ;
        test2.addTest(test46) ;
        Test test47 = test1
                           .createNegativeTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#error004",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/error004.rdf",
                                               true, null) ;
        test2.addTest(test47) ;
        Test test48 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0003",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0003.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0003.nt",
                                               false) ;
        test2.addTest(test48) ;
        Test test49 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0004",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0004.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0004.nt",
                                               false) ;
        test2.addTest(test49) ;
        Test test50 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/Manifest.rdf#test005",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test005.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test005.nt",
                                               false) ;
        test2.addTest(test50) ;
        Test test51 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test014",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test014.rdf", true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test014.nt", false) ;
        test2.addTest(test51) ;
        Test test52 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0009",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0009.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0009.nt",
                                               false) ;
        test2.addTest(test52) ;
        Test test53 = test1
                           .createNegativeTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/Manifest.rdf#error004",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/error004.rdf",
                                               true, null) ;
        test2.addTest(test53) ;
        Test test54 = test1
                           .createNegativeTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/Manifest.rdf#error005",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/error005.rdf",
                                               true, null) ;
        test2.addTest(test54) ;
        Test test55 = test1
                           .createNegativeTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/Manifest.rdf#error007",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/error007.rdf",
                                               true, null) ;
        test2.addTest(test55) ;
        Test test56 = test1
                           .createNegativeTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/Manifest.rdf#error006",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/error006.rdf",
                                               true, null) ;
        test2.addTest(test56) ;
        Test test57 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/amp-in-url/Manifest.rdf#test001",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/amp-in-url/test001.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/amp-in-url/test001.nt",
                                               false) ;
        test2.addTest(test57) ;
        Test test58 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#test001",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/test001.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/test001.nt",
                                               false) ;
        test2.addTest(test58) ;
        Test test59 = test1
                           .createNegativeTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-abouteach/Manifest.rdf#error002",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-abouteach/error002.rdf",
                                               true, new int[]{206,}) ;
        test2.addTest(test59) ;
        Test test60 = test1
                           .createNegativeTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/Manifest.rdf#error002",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/error002.rdf",
                                               true, null) ;
        test2.addTest(test60) ;
        Test test61 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/Manifest.rdf#test004",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test004.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test004.nt",
                                               false) ;
        test2.addTest(test61) ;
        Test test62 = test1
                           .createNegativeTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/Manifest.rdf#error003",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/error003.rdf",
                                               true, null) ;
        test2.addTest(test62) ;
        Test test63 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/datatypes/Manifest.rdf#test002",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/datatypes/test002.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/datatypes/test002.nt",
                                               false) ;
        test2.addTest(test63) ;
        Test test64 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test004",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test004.rdf", true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test004.nt", false) ;
        test2.addTest(test64) ;
        Test test65 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test003",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test003.rdf", true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test003.nt", false) ;
        test2.addTest(test65) ;
        Test test66 = test1
                           .createNegativeTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/Manifest.rdf#error1",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/error1.rdf",
                                               true, new int[]{105,}) ;
        test2.addTest(test66) ;
        Test test67 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-030",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-030.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-030.nt",
                                               false) ;
        test2.addTest(test67) ;
        Test test68 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/Manifest.rdf#test001",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test001.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test001.nt",
                                               false) ;
        test2.addTest(test68) ;
        Test test69 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/Manifest.rdf#test002",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test002.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test002.nt",
                                               false) ;
        test2.addTest(test69) ;
        Test test70 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#test004",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/test004.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/test004.nt",
                                               false) ;
        test2.addTest(test70) ;
        Test test71 = test1
                           .createWarningTest(
                                              "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#warn-001",
                                              "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/warn-001.rdf",
                                              true,
                                              "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/warn-001.nt",
                                              false, null) ;
        test2.addTest(test71) ;
        Test test72 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xml-canon/Manifest.rdf#test001",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xml-canon/test001.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xml-canon/test001.nt",
                                               false) ;
        test2.addTest(test72) ;
        Test test73 = test1
                           .createNegativeTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-abouteach/Manifest.rdf#error001",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-abouteach/error001.rdf",
                                               true, new int[]{206,}) ;
        test2.addTest(test73) ;
        Test test74 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/Manifest.rdf#test001",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/test001.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/test001.nt",
                                               false) ;
        test2.addTest(test74) ;
        Test test75 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-para196/Manifest.rdf#test001",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-para196/test001.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-para196/test001.nt",
                                               false) ;
        test2.addTest(test75) ;
        Test test76 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-006",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-006.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-006.nt",
                                               false) ;
        test2.addTest(test76) ;
        Test test77 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-007",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-007.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-007.nt",
                                               false) ;
        test2.addTest(test77) ;
        Test test78 = test1
                           .createNegativeTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/Manifest.rdf#error002",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/error002.rdf",
                                               true, new int[]{204,}) ;
        test2.addTest(test78) ;
        Test test79 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/Manifest.rdf#test005",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/test005.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/test005.nt",
                                               false) ;
        test2.addTest(test79) ;
        Test test80 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/Manifest.rdf#test004",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/test004.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/test004.nt",
                                               false) ;
        test2.addTest(test80) ;
        Test test81 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-charmod-uris/Manifest.rdf#test001",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-charmod-uris/test001.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-charmod-uris/test001.nt",
                                               false) ;
        test2.addTest(test81) ;
        Test test82 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-002",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-002.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-002.nt",
                                               false) ;
        test2.addTest(test82) ;
        Test test83 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-duplicate-member-props/Manifest.rdf#test001",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-duplicate-member-props/test001.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-duplicate-member-props/test001.nt",
                                               false) ;
        test2.addTest(test83) ;
        Test test84 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-003",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-003.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-003.nt",
                                               false) ;
        test2.addTest(test84) ;
        Test test85 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-seq-representation/Manifest.rdf#test001",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-seq-representation/test001.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-seq-representation/test001.nt",
                                               false) ;
        test2.addTest(test85) ;
        Test test86 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/Manifest.rdf#test001",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test001.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test001.nt",
                                               false) ;
        test2.addTest(test86) ;
        Test test87 = test1
                           .createNegativeTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-020",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-020.rdf",
                                               true, new int[]{205,}) ;
        test2.addTest(test87) ;
        Test test88 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-015",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-015.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-015.nt",
                                               false) ;
        test2.addTest(test88) ;
        Test test89 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-016",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-016.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-016.nt",
                                               false) ;
        test2.addTest(test89) ;
        Test test90 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/Manifest.rdf#test1",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/test1.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/test1.nt",
                                               false) ;
        test2.addTest(test90) ;
        Test test91 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test007",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test007.rdf", true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test007.nt", false) ;
        test2.addTest(test91) ;
        Test test92 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test008",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test008.rdf", true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test008.nt", false) ;
        test2.addTest(test92) ;
        Test test93 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test005",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test005.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test005.nt",
                                               false) ;
        test2.addTest(test93) ;
        Test test94 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test006",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test006.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test006.nt",
                                               false) ;
        test2.addTest(test94) ;
        Test test95 = test1
                           .createNegativeTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/Manifest.rdf#error001",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/error001.rdf",
                                               true, null) ;
        test2.addTest(test95) ;
        Test test96 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-010",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-010.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-010.nt",
                                               false) ;
        test2.addTest(test96) ;
        Test test97 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/Manifest.rdf#test003",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test003.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test003.nt",
                                               false) ;
        test2.addTest(test97) ;
        Test test98 = test1
                           .createPositiveTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/Manifest.rdf#test004",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test004.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test004.nt",
                                               false) ;
        test2.addTest(test98) ;
        Test test99 = test1
                           .createNegativeTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#error001",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/error001.rdf",
                                               true, new int[]{201,}) ;
        test2.addTest(test99) ;
        Test test100 = test1
                            .createNegativeTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#error002",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/error002.rdf",
                                                true, new int[]{201,}) ;
        test2.addTest(test100) ;
        Test test101 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-013",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-013.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-013.nt",
                                                false) ;
        test2.addTest(test101) ;
        Test test102 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-014",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-014.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-014.nt",
                                                false) ;
        test2.addTest(test102) ;
        Test test103 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-001",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-001.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-001.nt",
                                                false) ;
        test2.addTest(test103) ;
        Test test104 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-charmod-literals/Manifest.rdf#test001",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-charmod-literals/test001.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-charmod-literals/test001.nt",
                                                false) ;
        test2.addTest(test104) ;
        Test test105 = test1
                            .createNegativeTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-007",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-007.rdf",
                                                true, new int[]{205,}) ;
        test2.addTest(test105) ;
        Test test106 = test1
                            .createNegativeTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-006",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-006.rdf",
                                                true, new int[]{205,}) ;
        test2.addTest(test106) ;
        Test test107 = test1
                            .createWarningTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#warn-003",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/warn-003.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/warn-003.nt",
                                               false, null) ;
        test2.addTest(test107) ;
        Test test108 = test1
                            .createWarningTest(
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#warn-002",
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/warn-002.rdf",
                                               true,
                                               "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/warn-002.nt",
                                               false, null) ;
        test2.addTest(test108) ;
        Test test109 = test1
                            .createNegativeTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-014",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-014.rdf",
                                                true, new int[]{205,}) ;
        test2.addTest(test109) ;
        Test test110 = test1
                            .createNegativeTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-013",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-013.rdf",
                                                true, new int[]{205,}) ;
        test2.addTest(test110) ;
        Test test111 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/Manifest.rdf#test003",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test003.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test003.nt",
                                                false) ;
        test2.addTest(test111) ;
        Test test112 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/Manifest.rdf#test004",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test004.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test004.nt",
                                                false) ;
        test2.addTest(test112) ;
        Test test113 = test1
                            .createNegativeTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-017",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-017.rdf",
                                                true, new int[]{205,}) ;
        test2.addTest(test113) ;
        Test test114 = test1
                            .createNegativeTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-018",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-018.rdf",
                                                true, new int[]{205,}) ;
        test2.addTest(test114) ;
        Test test115 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test009",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test009.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test009.nt",
                                                false) ;
        test2.addTest(test115) ;
        Test test116 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xml-literal-namespaces/Manifest.rdf#test002",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xml-literal-namespaces/test002.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xml-literal-namespaces/test002.nt",
                                                false) ;
        test2.addTest(test116) ;
        Test test117 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/Manifest.rdf#test001",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test001.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test001.nt",
                                                false) ;
        test2.addTest(test117) ;
        Test test118 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/Manifest.rdf#test002",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test002.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test002.nt",
                                                false) ;
        test2.addTest(test118) ;
        Test test119 = test1
                            .createNegativeTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#error006",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/error006.rdf",
                                                true, null) ;
        test2.addTest(test119) ;
        Test test120 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/Manifest.rdf#test008",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test008.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test008.nt",
                                                false) ;
        test2.addTest(test120) ;
        Test test121 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-026",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-026.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-026.nt",
                                                false) ;
        test2.addTest(test121) ;
        Test test122 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-027",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-027.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-027.nt",
                                                false) ;
        test2.addTest(test122) ;
        Test test123 = test1
                            .createNegativeTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-010",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-010.rdf",
                                                true, new int[]{205,}) ;
        test2.addTest(test123) ;
        Test test124 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test011",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test011.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test011.nt",
                                                false) ;
        test2.addTest(test124) ;
        Test test125 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test010",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test010.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test010.nt",
                                                false) ;
        test2.addTest(test125) ;
        Test test126 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-019",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-019.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-019.nt",
                                                false) ;
        test2.addTest(test126) ;
        Test test127 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test001",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test001.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test001.nt",
                                                false) ;
        test2.addTest(test127) ;
        Test test128 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test002",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test002.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test002.nt",
                                                false) ;
        test2.addTest(test128) ;
        Test test129 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0014",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0014.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0014.nt",
                                                false) ;
        test2.addTest(test129) ;
        Test test130 = test1
                            .createNegativeTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/Manifest.rdf#error001",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/error001.rdf",
                                                true, new int[]{206,}) ;
        test2.addTest(test130) ;
        Test test131 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#test003",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/test003.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/test003.nt",
                                                false) ;
        test2.addTest(test131) ;
        Test test132 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#test002",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/test002.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/test002.nt",
                                                false) ;
        test2.addTest(test132) ;
        Test test133 = test1
                            .createNegativeTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-012",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-012.rdf",
                                                true, new int[]{205,}) ;
        test2.addTest(test133) ;
        Test test134 = test1
                            .createNegativeTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-011",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-011.rdf",
                                                true, new int[]{205,}) ;
        test2.addTest(test134) ;
        Test test135 = test1
                            .createNegativeTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#error001",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/error001.rdf",
                                                true, null) ;
        test2.addTest(test135) ;
        Test test136 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-034",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-034.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-034.nt",
                                                false) ;
        test2.addTest(test136) ;
        Test test137 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-033",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-033.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-033.nt",
                                                false) ;
        test2.addTest(test137) ;
        Test test138 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-037",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-037.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-037.nt",
                                                false) ;
        test2.addTest(test138) ;
        Test test139 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-036",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-036.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-036.nt",
                                                false) ;
        test2.addTest(test139) ;
        Test test140 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-035",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-035.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-035.nt",
                                                false) ;
        test2.addTest(test140) ;
        Test test141 = test1
                            .createNegativeTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#error003",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/error003.rdf",
                                                true, null) ;
        test2.addTest(test141) ;
        Test test142 = test1
                            .createNegativeTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#error002",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/error002.rdf",
                                                true, null) ;
        test2.addTest(test142) ;
        Test test143 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/Manifest.rdf#test2",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/test2.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/test2.nt",
                                                false) ;
        test2.addTest(test143) ;
        Test test144 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-008",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-008.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-008.nt",
                                                false) ;
        test2.addTest(test144) ;
        Test test145 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-009",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-009.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-009.nt",
                                                false) ;
        test2.addTest(test145) ;
        Test test146 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-032",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-032.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-032.nt",
                                                false) ;
        test2.addTest(test146) ;
        Test test147 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-031",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-031.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-031.nt",
                                                false) ;
        test2.addTest(test147) ;
        Test test148 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0001",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0001.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0001.nt",
                                                false) ;
        test2.addTest(test148) ;
        Test test149 = test1
                            .createNegativeTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-008",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-008.rdf",
                                                true, new int[]{204,}) ;
        test2.addTest(test149) ;
        Test test150 = test1
                            .createNegativeTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-009",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-009.rdf",
                                                true, new int[]{205,}) ;
        test2.addTest(test150) ;
        Test test151 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test006",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test006.rdf",
                                                true, "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test006.nt",
                                                false) ;
        test2.addTest(test151) ;
        Test test152 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-uri-substructure/Manifest.rdf#test001",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-uri-substructure/test001.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-uri-substructure/test001.nt",
                                                false) ;
        test2.addTest(test152) ;
        Test test153 = test1
                            .createNegativeTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#error003",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/error003.rdf",
                                                true, new int[]{201,}) ;
        test2.addTest(test153) ;
        Test test154 = test1
                            .createNegativeTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-002",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-002.rdf",
                                                true, new int[]{205,}) ;
        test2.addTest(test154) ;
        Test test155 = test1
                            .createNegativeTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-003",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-003.rdf",
                                                true, new int[]{205,}) ;
        test2.addTest(test155) ;
        Test test156 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test013",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test013.rdf",
                                                true, "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test013.nt",
                                                false) ;
        test2.addTest(test156) ;
        Test test157 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-022",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-022.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-022.nt",
                                                false) ;
        test2.addTest(test157) ;
        Test test158 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-023",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-023.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-023.nt",
                                                false) ;
        test2.addTest(test158) ;
        Test test159 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-004",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-004.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-004.nt",
                                                false) ;
        test2.addTest(test159) ;
        Test test160 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-005",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-005.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-005.nt",
                                                false) ;
        test2.addTest(test160) ;
        Test test161 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/Manifest.rdf#test002",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/test002.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/test002.nt",
                                                false) ;
        test2.addTest(test161) ;
        Test test162 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test014",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test014.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test014.nt",
                                                false) ;
        test2.addTest(test162) ;
        Test test163 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test015",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test015.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test015.nt",
                                                false) ;
        test2.addTest(test163) ;
        Test test164 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-charmod-uris/Manifest.rdf#test002",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-charmod-uris/test002.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-charmod-uris/test002.nt",
                                                false) ;
        test2.addTest(test164) ;
        Test test165 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0011",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0011.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0011.nt",
                                                false) ;
        test2.addTest(test165) ;
        Test test166 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0010",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0010.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0010.nt",
                                                false) ;
        test2.addTest(test166) ;
        Test test167 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-012",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-012.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-012.nt",
                                                false) ;
        test2.addTest(test167) ;
        Test test168 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-011",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-011.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-011.nt",
                                                false) ;
        test2.addTest(test168) ;
        test1.addTest(test2) ;
        TestSuite test169 = new TestSuite("PENDING") ;
        Test test170 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test016",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test016.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test016.nt",
                                                false) ;
        test169.addTest(test170) ;
        Test test171 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test017",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test017.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test017.nt",
                                                false) ;
        test169.addTest(test171) ;
        Test test172 = test1
                            .createPositiveTest(
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/Manifest.rdf#test3",
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/test3.rdf",
                                                true,
                                                "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/test3.nt",
                                                false) ;
        test169.addTest(test172) ;
        test1.addTest(test169) ;
        // TestSuite test173 = new TestSuite("WITHDRAWN");
        // Test test174 =
        // test1.createPositiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test012","http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test012.rdf",true,"http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test012.nt",false);
        // test173.addTest(test174);
        // test1.addTest(test173);
        test0.addTest(test1) ;
        WGTestSuite test175 = new com.hp.hpl.jena.rdfxml.xmlinput.WGTestSuite(
           new TestInputStreamFactory(IRIFactory.iriImplementation().create("http://jcarroll.hpl.hp.com/arp-tests/"),
                                      "arp"),
           "ARP Tests", false) ;
        TestSuite test176 = new TestSuite("ARP") ;
        Test test177 = test175.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/xml-literals/reported3",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/xml-literals/reported3.rdf",
                                                  true,
                                                  "http://jcarroll.hpl.hp.com/arp-tests/xml-literals/reported3.nt",
                                                  false) ;
        test176.addTest(test177) ;
        Test test177x = test175.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/xml-literals/html",
                                                   "http://jcarroll.hpl.hp.com/arp-tests/xml-literals/html.rdf", true,
                                                   "http://jcarroll.hpl.hp.com/arp-tests/xml-literals/html.nt", false) ;
        test176.addTest(test177x) ;
        Test test178 = test175.createWarningTest("http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/67_6",
                                                 "http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/bad-bug67_6.rdf", true,
                                                 "http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/bad-bug67_6.nt", false,
                                                 new int[]{103,}) ;
        test176.addTest(test178) ;
        Test test179 = test175.createWarningTest("http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/67_5",
                                                 "http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/bad-bug67_5.rdf", true,
                                                 "http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/bad-bug67_5.nt", false,
                                                 new int[]{103,}) ;
        test176.addTest(test179) ;
        Test test180 = test175.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/comments/test10",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test10.rdf", true,
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test1X.nt", false) ;
        test176.addTest(test180) ;
        Test test181 = test175.createWarningTest("http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/67_7",
                                                 "http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/bad-bug67_7.rdf", true,
                                                 "http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/bad-bug67_7.nt", false,
                                                 new int[]{113,}) ;
        test176.addTest(test181) ;
        Test test182 = test175.createWarningTest("http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/67_8",
                                                 "http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/bad-bug67_8.rdf", true,
                                                 "http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/bad-bug67_8.nt", false,
                                                 new int[]{103, 113,}) ;
        test176.addTest(test182) ;
        Test test183 = test175.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/comments/test01",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test01.rdf", true,
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test0X.nt", false) ;
        test176.addTest(test183) ;
        Test test184 = test175.createWarningTest("http://jcarroll.hpl.hp.com/arp-tests/parsetype/bug68",
                                                 "http://jcarroll.hpl.hp.com/arp-tests/parsetype/bug68_0.rdf", true,
                                                 "http://jcarroll.hpl.hp.com/arp-tests/parsetype/bug68_0.nt", false,
                                                 new int[]{106,}) ;
        test176.addTest(test184) ;
        Test test185 = test175.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/i18n/bug73a",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/i18n/eq-bug73_0.rdf", true,
                                                  "http://jcarroll.hpl.hp.com/arp-tests/i18n/eq-bug73_1.rdf", true) ;
        test176.addTest(test185) ;
        Test test186 = test175.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/comments/test12",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test12.rdf", true,
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test1X.nt", false) ;
        test176.addTest(test186) ;
        Test test187 = test175.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/comments/test11",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test11.rdf", true,
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test1X.nt", false) ;
        test176.addTest(test187) ;
        Test test188 = test175.createNegativeTest("http://jcarroll.hpl.hp.com/arp-tests/qname-in-ID/bug74",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/qname-in-ID/bug74_0.rdf", true,
                                                  new int[]{108,}) ;
        test176.addTest(test188) ;
        Test test189 = test175.createNegativeTest(
                                                  "http://jcarroll.hpl.hp.com/arp-tests/relative-namespaces/50_0",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/relative-namespaces/bad-bug50_0.rdf",
                                                  true, new int[]{109, 136,}) ;
        test176.addTest(test189) ;
        Test test190 = test175.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/rfc2396-issue/bug51",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/rfc2396-issue/bug51_0.rdf",
                                                  true,
                                                  "http://jcarroll.hpl.hp.com/arp-tests/rfc2396-issue/bug51_0.nt",
                                                  false) ;
        test176.addTest(test190) ;
        Test test191 = test175.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/comments/test03",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test03.rdf", true,
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test0X.nt", false) ;
        test176.addTest(test191) ;
        Test test192 = test175.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/comments/test02",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test02.rdf", true,
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test0X.nt", false) ;
        test176.addTest(test192) ;
        Test test193 = test175.createNegativeTest("http://jcarroll.hpl.hp.com/arp-tests/xmlns/bad01",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/xmlns/bad01.rdf", true,
                                                  new int[]{124, 107,}) ;
        test176.addTest(test193) ;

        Test tProp = test175.createNegativeTest("http://jcarroll.hpl.hp.com/arp-tests/unqualified/property",
                                                "http://jcarroll.hpl.hp.com/arp-tests/unqualified/property.rdf", true,
                                                new int[]{104, 136}) ;
        test176.addTest(tProp) ;
        Test tAttr = test175.createNegativeTest("http://jcarroll.hpl.hp.com/arp-tests/unqualified/attribute",
                                                "http://jcarroll.hpl.hp.com/arp-tests/unqualified/attribute.rdf", true,
                                                new int[]{102, 136}) ;
        test176.addTest(tAttr) ;
        Test tType = test175.createNegativeTest("http://jcarroll.hpl.hp.com/arp-tests/unqualified/typedNode",
                                                "http://jcarroll.hpl.hp.com/arp-tests/unqualified/typedNode.rdf", true,
                                                new int[]{104, 136}) ;
        test176.addTest(tType) ;
        Test tRelative = test175
                                .createNegativeTest(
                                                    "http://jcarroll.hpl.hp.com/arp-tests/unqualified/relative-namespace",
                                                    "http://jcarroll.hpl.hp.com/arp-tests/unqualified/relative-namespace.rdf",
                                                    true, new int[]{109, 136}) ;
        test176.addTest(tRelative) ;

        Test test194 = test175.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/comments/test09",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test09.rdf", true,
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test0X.nt", false) ;
        test176.addTest(test194) ;
        Test test195 = test175.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/comments/test08",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test08.rdf", true,
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test0X.nt", false) ;
        test176.addTest(test195) ;
        Test test196 = test175.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/comments/test13",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test13.rdf", true,
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test1X.nt", false) ;
        test176.addTest(test196) ;
        Test test197 = test175.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/i18n/bug73b",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/i18n/eq-bug73_0.rdf", true,
                                                  "http://jcarroll.hpl.hp.com/arp-tests/i18n/eq-bug73_2.rdf", true) ;
        test176.addTest(test197) ;
        Test test198 = test175.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/comments/test05",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test05.rdf", true,
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test0X.nt", false) ;
        test176.addTest(test198) ;
        Test test199 = test175.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/comments/test04",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test04.rdf", true,
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test0X.nt", false) ;
        test176.addTest(test199) ;
        Test test200 = test175.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/rfc2396-issue/fileURI",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/rfc2396-issue/fileURI.rdf",
                                                  true,
                                                  "http://jcarroll.hpl.hp.com/arp-tests/rfc2396-issue/fileURI.nt",
                                                  false) ;
        test176.addTest(test200) ;
        Test test201 = test175.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/comments/test07",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test07.rdf", true,
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test0X.nt", false) ;
        test176.addTest(test201) ;
        Test test202 = test175.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/comments/test06",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test06.rdf", true,
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test0X.nt", false) ;
        test176.addTest(test202) ;
        Test test203 = test175.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/xml-literals/reported1",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/xml-literals/reported1.rdf",
                                                  true,
                                                  "http://jcarroll.hpl.hp.com/arp-tests/xml-literals/reported1.nt",
                                                  false) ;
        test176.addTest(test203) ;
        Test test204 = test175.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/xml-literals/reported2",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/xml-literals/reported2.rdf",
                                                  true,
                                                  "http://jcarroll.hpl.hp.com/arp-tests/xml-literals/reported2.nt",
                                                  false) ;
        test176.addTest(test204) ;
        Test test205 = test175.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/xmlns/test01",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/xmlns/test01.rdf", true,
                                                  "http://jcarroll.hpl.hp.com/arp-tests/xmlns/test0X.nt", false) ;
        test176.addTest(test205) ;
        Test test206 = test175.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/i18n/t9000",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/i18n/t9000.rdf", true,
                                                  "http://jcarroll.hpl.hp.com/arp-tests/i18n/t9000.nt", false) ;
        // new int[]{ //135, 135,135, 135,135, 135,135, 135,135,
        // 135, });
        test176.addTest(test206) ;
        Test test207 = test175.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/xmlns/test03",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/xmlns/test03.rdf", true,
                                                  "http://jcarroll.hpl.hp.com/arp-tests/xmlns/test0X.nt", false) ;
        test176.addTest(test207) ;
        Test test208 = test175.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/xmlns/test02",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/xmlns/test02.rdf", true,
                                                  "http://jcarroll.hpl.hp.com/arp-tests/xmlns/test0X.nt", false) ;
        test176.addTest(test208) ;
        Test test209 = test175.createWarningTest("http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/67_0",
                                                 "http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/bad-bug67_0.rdf", true,
                                                 "http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/bad-bug67_0.nt", false,
                                                 new int[]{103,}) ;
        test176.addTest(test209) ;
        Test test210 = test175.createWarningTest("http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/67_9",
                                                 "http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/bad-bug67_9.rdf", true,
                                                 "http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/bad-bug67_9.nt", false,
                                                 new int[]{103, 113,}) ;
        test176.addTest(test210) ;
        test175.addTest(test176) ;
        test0.addTest(test175) ;
        WGTestSuite test211 = new com.hp.hpl.jena.rdfxml.xmlinput.NTripleTestSuite(
                                                                                new TestInputStreamFactory(
                                                                                                           IRIFactory
                                                                                                                     .iriImplementation()
                                                                                                                     .create(
                                                                                                                             "http://www.w3.org/2000/10/rdf-tests/rdfcore/"),
                                                                                                           "wg"),
                                                                                "NTriple WG Tests", false) ;
        TestSuite test212 = new TestSuite("APPROVED") ;
        Test test213 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-024",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-024.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-024.nt",
                                                  false) ;
        test212.addTest(test213) ;
        Test test214 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-025",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-025.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-025.nt",
                                                  false) ;
        test212.addTest(test214) ;
        Test test215 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test003",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test003.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test003.nt",
                                                  false) ;
        test212.addTest(test215) ;
        Test test216 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test004",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test004.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test004.nt",
                                                  false) ;
        test212.addTest(test216) ;
        Test test217 = test211
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-019",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-019.rdf",
                                                  true, new int[]{205,}) ;
        test212.addTest(test217) ;
        Test test218 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-017",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-017.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-017.nt",
                                                  false) ;
        test212.addTest(test218) ;
        Test test219 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-018",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-018.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-018.nt",
                                                  false) ;
        test212.addTest(test219) ;
        Test test220 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-element-not-mandatory/Manifest.rdf#test001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-element-not-mandatory/test001.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-element-not-mandatory/test001.nt",
                                                  false) ;
        test212.addTest(test220) ;
        Test test221 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/datatypes/Manifest.rdf#test001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/datatypes/test001.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/datatypes/test001.nt",
                                                  false) ;
        test212.addTest(test221) ;
        Test test222 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/Manifest.rdf#test005",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test005.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test005.nt",
                                                  false) ;
        test212.addTest(test222) ;
        Test test223 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/Manifest.rdf#test006",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test006.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test006.nt",
                                                  false) ;
        test212.addTest(test223) ;
        Test test224 = test211
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-001.rdf",
                                                  true, new int[]{205,}) ;
        test212.addTest(test224) ;
        Test test225 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test011",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test011.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test011.nt",
                                                  false) ;
        test212.addTest(test225) ;
        Test test226 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test010",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test010.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test010.nt",
                                                  false) ;
        test212.addTest(test226) ;
        Test test227 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0013",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0013.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0013.nt",
                                                  false) ;
        test212.addTest(test227) ;
        Test test228 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0012",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0012.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0012.nt",
                                                  false) ;
        test212.addTest(test228) ;
        Test test229 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/unrecognised-xml-attributes/Manifest.rdf#test002",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/unrecognised-xml-attributes/test002.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/unrecognised-xml-attributes/test002.nt",
                                                  false) ;
        test212.addTest(test229) ;
        Test test230 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/unrecognised-xml-attributes/Manifest.rdf#test001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/unrecognised-xml-attributes/test001.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/unrecognised-xml-attributes/test001.nt",
                                                  false) ;
        test212.addTest(test230) ;
        Test test231 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test002",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test002.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test002.nt",
                                                  false) ;
        test212.addTest(test231) ;
        Test test232 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test001.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test001.nt",
                                                  false) ;
        test212.addTest(test232) ;
        Test test233 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfs-domain-and-range/Manifest.rdf#test002",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfs-domain-and-range/test002.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfs-domain-and-range/test002.nt",
                                                  false) ;
        test212.addTest(test233) ;
        Test test234 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfs-domain-and-range/Manifest.rdf#test001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfs-domain-and-range/test001.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfs-domain-and-range/test001.nt",
                                                  false) ;
        test212.addTest(test234) ;
        Test test235 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xml-literal-namespaces/Manifest.rdf#test001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xml-literal-namespaces/test001.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xml-literal-namespaces/test001.nt",
                                                  false) ;
        test212.addTest(test235) ;
        Test test236 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test009",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test009.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test009.nt",
                                                  false) ;
        test212.addTest(test236) ;
        Test test237 = test211
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-004",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-004.rdf",
                                                  true, new int[]{205,}) ;
        test212.addTest(test237) ;
        Test test238 = test211
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-005",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-005.rdf",
                                                  true, new int[]{205,}) ;
        test212.addTest(test238) ;
        Test test239 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-028",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-028.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-028.nt",
                                                  false) ;
        test212.addTest(test239) ;
        Test test240 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-029",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-029.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-029.nt",
                                                  false) ;
        test212.addTest(test240) ;
        Test test241 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test012",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test012.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test012.nt",
                                                  false) ;
        test212.addTest(test241) ;
        Test test242 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test013",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test013.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test013.nt",
                                                  false) ;
        test212.addTest(test242) ;
        Test test243 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/Manifest.rdf#test002",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test002.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test002.nt",
                                                  false) ;
        test212.addTest(test243) ;
        Test test244 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/Manifest.rdf#test003",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test003.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test003.nt",
                                                  false) ;
        test212.addTest(test244) ;
        Test test245 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0006",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0006.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0006.nt",
                                                  false) ;
        test212.addTest(test245) ;
        Test test246 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0005",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0005.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0005.nt",
                                                  false) ;
        test212.addTest(test246) ;
        Test test247 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-reification-required/Manifest.rdf#test001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-reification-required/test001.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-reification-required/test001.nt",
                                                  false) ;
        test212.addTest(test247) ;
        Test test248 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-021",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-021.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-021.nt",
                                                  false) ;
        test212.addTest(test248) ;
        Test test249 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-020",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-020.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-020.nt",
                                                  false) ;
        test212.addTest(test249) ;
        Test test250 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test008",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test008.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test008.nt",
                                                  false) ;
        test212.addTest(test250) ;
        Test test251 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test007",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test007.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test007.nt",
                                                  false) ;
        test212.addTest(test251) ;
        Test test252 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/Manifest.rdf#test006",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test006.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test006.nt",
                                                  false) ;
        test212.addTest(test252) ;
        Test test253 = test211
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-016",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-016.rdf",
                                                  true, new int[]{205,}) ;
        test212.addTest(test253) ;
        Test test254 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/Manifest.rdf#test007",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test007.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test007.nt",
                                                  false) ;
        test212.addTest(test254) ;
        Test test255 = test211
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-015",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-015.rdf",
                                                  true, new int[]{205,}) ;
        test212.addTest(test255) ;
        Test test256 = test211
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#error005",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/error005.rdf",
                                                  true, null) ;
        test212.addTest(test256) ;
        Test test257 = test211
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#error004",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/error004.rdf",
                                                  true, null) ;
        test212.addTest(test257) ;
        Test test258 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0003",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0003.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0003.nt",
                                                  false) ;
        test212.addTest(test258) ;
        Test test259 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0004",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0004.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0004.nt",
                                                  false) ;
        test212.addTest(test259) ;
        Test test260 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/Manifest.rdf#test005",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test005.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test005.nt",
                                                  false) ;
        test212.addTest(test260) ;
        Test test261 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test014",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test014.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test014.nt",
                                                  false) ;
        test212.addTest(test261) ;
        Test test262 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0009",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0009.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0009.nt",
                                                  false) ;
        test212.addTest(test262) ;
        Test test263 = test211
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/Manifest.rdf#error004",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/error004.rdf",
                                                  true, null) ;
        test212.addTest(test263) ;
        Test test264 = test211
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/Manifest.rdf#error005",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/error005.rdf",
                                                  true, null) ;
        test212.addTest(test264) ;
        Test test265 = test211
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/Manifest.rdf#error007",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/error007.rdf",
                                                  true, null) ;
        test212.addTest(test265) ;
        Test test266 = test211
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/Manifest.rdf#error006",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/error006.rdf",
                                                  true, null) ;
        test212.addTest(test266) ;
        Test test267 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/amp-in-url/Manifest.rdf#test001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/amp-in-url/test001.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/amp-in-url/test001.nt",
                                                  false) ;
        test212.addTest(test267) ;
        Test test268 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#test001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/test001.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/test001.nt",
                                                  false) ;
        test212.addTest(test268) ;
        Test test269 = test211
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-abouteach/Manifest.rdf#error002",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-abouteach/error002.rdf",
                                                  true, new int[]{206,}) ;
        test212.addTest(test269) ;
        Test test270 = test211
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/Manifest.rdf#error002",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/error002.rdf",
                                                  true, null) ;
        test212.addTest(test270) ;
        Test test271 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/Manifest.rdf#test004",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test004.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test004.nt",
                                                  false) ;
        test212.addTest(test271) ;
        Test test272 = test211
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/Manifest.rdf#error003",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/error003.rdf",
                                                  true, null) ;
        test212.addTest(test272) ;
        Test test273 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/datatypes/Manifest.rdf#test002",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/datatypes/test002.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/datatypes/test002.nt",
                                                  false) ;
        test212.addTest(test273) ;
        Test test274 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test004",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test004.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test004.nt",
                                                  false) ;
        test212.addTest(test274) ;
        Test test275 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test003",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test003.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test003.nt",
                                                  false) ;
        test212.addTest(test275) ;
        Test test276 = test211
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/Manifest.rdf#error1",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/error1.rdf",
                                                  true, new int[]{105,}) ;
        test212.addTest(test276) ;
        Test test277 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-030",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-030.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-030.nt",
                                                  false) ;
        test212.addTest(test277) ;
        Test test278 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/Manifest.rdf#test001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test001.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test001.nt",
                                                  false) ;
        test212.addTest(test278) ;
        Test test279 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/Manifest.rdf#test002",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test002.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test002.nt",
                                                  false) ;
        test212.addTest(test279) ;
        Test test280 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#test004",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/test004.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/test004.nt",
                                                  false) ;
        test212.addTest(test280) ;
        Test test281 = test211
                              .createWarningTest(
                                                 "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#warn-001",
                                                 "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/warn-001.rdf",
                                                 true,
                                                 "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/warn-001.nt",
                                                 false, null) ;
        test212.addTest(test281) ;
        Test test282 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xml-canon/Manifest.rdf#test001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xml-canon/test001.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xml-canon/test001.nt",
                                                  false) ;
        test212.addTest(test282) ;
        Test test283 = test211
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-abouteach/Manifest.rdf#error001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-abouteach/error001.rdf",
                                                  true, new int[]{206,}) ;
        test212.addTest(test283) ;
        Test test284 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/Manifest.rdf#test001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/test001.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/test001.nt",
                                                  false) ;
        test212.addTest(test284) ;
        Test test285 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-para196/Manifest.rdf#test001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-para196/test001.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-para196/test001.nt",
                                                  false) ;
        test212.addTest(test285) ;
        Test test286 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-006",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-006.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-006.nt",
                                                  false) ;
        test212.addTest(test286) ;
        Test test287 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-007",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-007.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-007.nt",
                                                  false) ;
        test212.addTest(test287) ;
        Test test288 = test211
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/Manifest.rdf#error002",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/error002.rdf",
                                                  true, new int[]{204,}) ;
        test212.addTest(test288) ;
        Test test289 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/Manifest.rdf#test005",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/test005.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/test005.nt",
                                                  false) ;
        test212.addTest(test289) ;
        Test test290 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/Manifest.rdf#test004",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/test004.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/test004.nt",
                                                  false) ;
        test212.addTest(test290) ;
        Test test291 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-charmod-uris/Manifest.rdf#test001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-charmod-uris/test001.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-charmod-uris/test001.nt",
                                                  false) ;
        test212.addTest(test291) ;
        Test test292 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-002",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-002.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-002.nt",
                                                  false) ;
        test212.addTest(test292) ;
        Test test293 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-duplicate-member-props/Manifest.rdf#test001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-duplicate-member-props/test001.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-duplicate-member-props/test001.nt",
                                                  false) ;
        test212.addTest(test293) ;
        Test test294 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-003",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-003.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-003.nt",
                                                  false) ;
        test212.addTest(test294) ;
        Test test295 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-seq-representation/Manifest.rdf#test001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-seq-representation/test001.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-seq-representation/test001.nt",
                                                  false) ;
        test212.addTest(test295) ;
        Test test296 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/Manifest.rdf#test001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test001.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test001.nt",
                                                  false) ;
        test212.addTest(test296) ;
        Test test297 = test211
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-020",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-020.rdf",
                                                  true, new int[]{205,}) ;
        test212.addTest(test297) ;
        Test test298 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-015",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-015.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-015.nt",
                                                  false) ;
        test212.addTest(test298) ;
        Test test299 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-016",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-016.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-016.nt",
                                                  false) ;
        test212.addTest(test299) ;
        Test test300 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/Manifest.rdf#test1",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/test1.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/test1.nt",
                                                  false) ;
        test212.addTest(test300) ;
        Test test301 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test007",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test007.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test007.nt",
                                                  false) ;
        test212.addTest(test301) ;
        Test test302 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test008",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test008.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test008.nt",
                                                  false) ;
        test212.addTest(test302) ;
        Test test303 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test005",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test005.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test005.nt",
                                                  false) ;
        test212.addTest(test303) ;
        Test test304 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test006",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test006.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test006.nt",
                                                  false) ;
        test212.addTest(test304) ;
        Test test305 = test211
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/Manifest.rdf#error001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-id/error001.rdf",
                                                  true, null) ;
        test212.addTest(test305) ;
        Test test306 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-010",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-010.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-010.nt",
                                                  false) ;
        test212.addTest(test306) ;
        Test test307 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/Manifest.rdf#test003",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test003.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test003.nt",
                                                  false) ;
        test212.addTest(test307) ;
        Test test308 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/Manifest.rdf#test004",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test004.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test004.nt",
                                                  false) ;
        test212.addTest(test308) ;
        Test test309 = test211
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#error001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/error001.rdf",
                                                  true, new int[]{201,}) ;
        test212.addTest(test309) ;
        Test test310 = test211
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#error002",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/error002.rdf",
                                                  true, new int[]{201,}) ;
        test212.addTest(test310) ;
        Test test311 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-013",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-013.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-013.nt",
                                                  false) ;
        test212.addTest(test311) ;
        Test test312 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-014",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-014.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-014.nt",
                                                  false) ;
        test212.addTest(test312) ;
        Test test313 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-001.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-001.nt",
                                                  false) ;
        test212.addTest(test313) ;
        Test test314 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-charmod-literals/Manifest.rdf#test001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-charmod-literals/test001.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-charmod-literals/test001.nt",
                                                  false) ;
        test212.addTest(test314) ;
        Test test315 = test211
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-007",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-007.rdf",
                                                  true, new int[]{205,}) ;
        test212.addTest(test315) ;
        Test test316 = test211
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-006",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-006.rdf",
                                                  true, new int[]{205,}) ;
        test212.addTest(test316) ;
        Test test317 = test211
                              .createWarningTest(
                                                 "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#warn-003",
                                                 "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/warn-003.rdf",
                                                 true,
                                                 "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/warn-003.nt",
                                                 false, null) ;
        test212.addTest(test317) ;
        Test test318 = test211
                              .createWarningTest(
                                                 "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#warn-002",
                                                 "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/warn-002.rdf",
                                                 true,
                                                 "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/warn-002.nt",
                                                 false, null) ;
        test212.addTest(test318) ;
        Test test319 = test211
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-014",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-014.rdf",
                                                  true, new int[]{205,}) ;
        test212.addTest(test319) ;
        Test test320 = test211
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-013",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-013.rdf",
                                                  true, new int[]{205,}) ;
        test212.addTest(test320) ;
        Test test321 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/Manifest.rdf#test003",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test003.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test003.nt",
                                                  false) ;
        test212.addTest(test321) ;
        Test test322 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/Manifest.rdf#test004",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test004.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-identity-anon-resources/test004.nt",
                                                  false) ;
        test212.addTest(test322) ;
        Test test323 = test211
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-017",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-017.rdf",
                                                  true, new int[]{205,}) ;
        test212.addTest(test323) ;
        Test test324 = test211
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-018",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-018.rdf",
                                                  true, new int[]{205,}) ;
        test212.addTest(test324) ;
        Test test325 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test009",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test009.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test009.nt",
                                                  false) ;
        test212.addTest(test325) ;
        Test test326 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xml-literal-namespaces/Manifest.rdf#test002",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xml-literal-namespaces/test002.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xml-literal-namespaces/test002.nt",
                                                  false) ;
        test212.addTest(test326) ;
        Test test327 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/Manifest.rdf#test001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test001.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test001.nt",
                                                  false) ;
        test212.addTest(test327) ;
        Test test328 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/Manifest.rdf#test002",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test002.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-xmllang/test002.nt",
                                                  false) ;
        test212.addTest(test328) ;
        Test test329 = test211
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#error006",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/error006.rdf",
                                                  true, null) ;
        test212.addTest(test329) ;
        Test test330 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/Manifest.rdf#test008",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test008.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/test008.nt",
                                                  false) ;
        test212.addTest(test330) ;
        Test test331 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-026",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-026.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-026.nt",
                                                  false) ;
        test212.addTest(test331) ;
        Test test332 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-027",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-027.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-027.nt",
                                                  false) ;
        test212.addTest(test332) ;
        Test test333 = test211
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-010",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-010.rdf",
                                                  true, new int[]{205,}) ;
        test212.addTest(test333) ;
        Test test334 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test011",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test011.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test011.nt",
                                                  false) ;
        test212.addTest(test334) ;
        Test test335 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test010",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test010.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test010.nt",
                                                  false) ;
        test212.addTest(test335) ;
        Test test336 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-019",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-019.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-019.nt",
                                                  false) ;
        test212.addTest(test336) ;
        Test test337 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test001.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test001.nt",
                                                  false) ;
        test212.addTest(test337) ;
        Test test338 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test002",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test002.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test002.nt",
                                                  false) ;
        test212.addTest(test338) ;
        Test test339 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0014",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0014.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0014.nt",
                                                  false) ;
        test212.addTest(test339) ;
        Test test340 = test211
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/Manifest.rdf#error001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-containers-syntax-vs-schema/error001.rdf",
                                                  true, new int[]{206,}) ;
        test212.addTest(test340) ;
        Test test341 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#test003",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/test003.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/test003.nt",
                                                  false) ;
        test212.addTest(test341) ;
        Test test342 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#test002",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/test002.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/test002.nt",
                                                  false) ;
        test212.addTest(test342) ;
        Test test343 = test211
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-012",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-012.rdf",
                                                  true, new int[]{205,}) ;
        test212.addTest(test343) ;
        Test test344 = test211
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-011",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-011.rdf",
                                                  true, new int[]{205,}) ;
        test212.addTest(test344) ;
        Test test345 = test211
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#error001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/error001.rdf",
                                                  true, null) ;
        test212.addTest(test345) ;
        Test test346 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-034",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-034.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-034.nt",
                                                  false) ;
        test212.addTest(test346) ;
        Test test347 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-033",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-033.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-033.nt",
                                                  false) ;
        test212.addTest(test347) ;
        Test test348 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-037",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-037.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-037.nt",
                                                  false) ;
        test212.addTest(test348) ;
        Test test349 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-036",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-036.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-036.nt",
                                                  false) ;
        test212.addTest(test349) ;
        Test test350 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-035",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-035.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-035.nt",
                                                  false) ;
        test212.addTest(test350) ;
        Test test351 = test211
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#error003",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/error003.rdf",
                                                  true, null) ;
        test212.addTest(test351) ;
        Test test352 = test211
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/Manifest.rdf#error002",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-syntax-incomplete/error002.rdf",
                                                  true, null) ;
        test212.addTest(test352) ;
        Test test353 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/Manifest.rdf#test2",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/test2.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/test2.nt",
                                                  false) ;
        test212.addTest(test353) ;
        Test test354 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-008",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-008.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-008.nt",
                                                  false) ;
        test212.addTest(test354) ;
        Test test355 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-009",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-009.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-009.nt",
                                                  false) ;
        test212.addTest(test355) ;
        Test test356 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-032",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-032.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-032.nt",
                                                  false) ;
        test212.addTest(test356) ;
        Test test357 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-031",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-031.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-031.nt",
                                                  false) ;
        test212.addTest(test357) ;
        Test test358 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0001.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0001.nt",
                                                  false) ;
        test212.addTest(test358) ;
        Test test359 = test211
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-008",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-008.rdf",
                                                  true, new int[]{204,}) ;
        test212.addTest(test359) ;
        Test test360 = test211
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-009",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-009.rdf",
                                                  true, new int[]{205,}) ;
        test212.addTest(test360) ;
        Test test361 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test006",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test006.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test006.nt",
                                                  false) ;
        test212.addTest(test361) ;
        Test test362 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-uri-substructure/Manifest.rdf#test001",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-uri-substructure/test001.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-uri-substructure/test001.nt",
                                                  false) ;
        test212.addTest(test362) ;
        Test test363 = test211
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#error003",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/error003.rdf",
                                                  true, new int[]{201,}) ;
        test212.addTest(test363) ;
        Test test364 = test211
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-002",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-002.rdf",
                                                  true, new int[]{205,}) ;
        test212.addTest(test364) ;
        Test test365 = test211
                              .createNegativeTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#error-003",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/error-003.rdf",
                                                  true, new int[]{205,}) ;
        test212.addTest(test365) ;
        Test test366 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test013",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test013.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test013.nt",
                                                  false) ;
        test212.addTest(test366) ;
        Test test367 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-022",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-022.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-022.nt",
                                                  false) ;
        test212.addTest(test367) ;
        Test test368 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-023",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-023.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-023.nt",
                                                  false) ;
        test212.addTest(test368) ;
        Test test369 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-004",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-004.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-004.nt",
                                                  false) ;
        test212.addTest(test369) ;
        Test test370 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-005",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-005.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-005.nt",
                                                  false) ;
        test212.addTest(test370) ;
        Test test371 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/Manifest.rdf#test002",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/test002.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-not-id-and-resource-attr/test002.nt",
                                                  false) ;
        test212.addTest(test371) ;
        Test test372 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test014",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test014.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test014.nt",
                                                  false) ;
        test212.addTest(test372) ;
        Test test373 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test015",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test015.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test015.nt",
                                                  false) ;
        test212.addTest(test373) ;
        Test test374 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-charmod-uris/Manifest.rdf#test002",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-charmod-uris/test002.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-charmod-uris/test002.nt",
                                                  false) ;
        test212.addTest(test374) ;
        Test test375 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0011",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0011.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0011.nt",
                                                  false) ;
        test212.addTest(test375) ;
        Test test376 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/Manifest.rdf#test0010",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0010.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0010.nt",
                                                  false) ;
        test212.addTest(test376) ;
        Test test377 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-012",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-012.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-012.nt",
                                                  false) ;
        test212.addTest(test377) ;
        Test test378 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/Manifest.rdf#test-011",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-011.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-rdf-names-use/test-011.nt",
                                                  false) ;
        test212.addTest(test378) ;
        test211.addTest(test212) ;
        TestSuite test379 = new TestSuite("PENDING") ;
        Test test380 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test016",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test016.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test016.nt",
                                                  false) ;
        test379.addTest(test380) ;
        Test test381 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/Manifest.rdf#test017",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test017.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-empty-property-elements/test017.nt",
                                                  false) ;
        test379.addTest(test381) ;
        Test test382 = test211
                              .createPositiveTest(
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/Manifest.rdf#test3",
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/test3.rdf",
                                                  true,
                                                  "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-difference-between-ID-and-about/test3.nt",
                                                  false) ;
        test379.addTest(test382) ;
        test211.addTest(test379) ;
        // TODO: not for 2.3. stop this being generated.
        // TestSuite test383 = new TestSuite("WITHDRAWN");
        // Test test384 =
        // test211.createPositiveTest("http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/Manifest.rdf#test012","http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test012.rdf",true,"http://www.w3.org/2000/10/rdf-tests/rdfcore/xmlbase/test012.nt",false);
        // test383.addTest(test384);
        // test211.addTest(test383);
        test0.addTest(test211) ;
        return test0 ;
        // return test206;
    }
}
