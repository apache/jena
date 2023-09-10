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

import static org.apache.jena.riot.lang.rdfxml.converted_legacy.LibTestARP1.*;

import org.junit.Test;

/**
 * Tests from the jena-core ARP test suite, local tests.
 * Ones relating to DOM usage (embedded RDF/XML) are excluded.
 * RIOT only supports whole document parsing.
 */
public class TestARP1_Local {

    // The tests removed in DOM tests in jena-core, not in RIOT usage as a document parser.
    // They have a <foo></foo> wrapper.
//    @Test public void test_comments_test01() {
//        positiveTest("http://jcarroll.hpl.hp.com/arp-tests/comments/test01",
//                     "http://jcarroll.hpl.hp.com/arp-tests/comments/test01.rdf",
//                     "http://jcarroll.hpl.hp.com/arp-tests/comments/test0X.nt");
//    }
//
//    @Test public void test_comments_test02() {
//        positiveTest("http://jcarroll.hpl.hp.com/arp-tests/comments/test02",
//                     "http://jcarroll.hpl.hp.com/arp-tests/comments/test02.rdf",
//                     "http://jcarroll.hpl.hp.com/arp-tests/comments/test0X.nt");
//    }

    @Test public void test_comments_test03() {
        positiveTest("http://jcarroll.hpl.hp.com/arp-tests/comments/test03",
                     "http://jcarroll.hpl.hp.com/arp-tests/comments/test03.rdf",
                     "http://jcarroll.hpl.hp.com/arp-tests/comments/test0X.nt");
    }

    @Test public void test_comments_test04() {
        positiveTest("http://jcarroll.hpl.hp.com/arp-tests/comments/test04",
                     "http://jcarroll.hpl.hp.com/arp-tests/comments/test04.rdf",
                     "http://jcarroll.hpl.hp.com/arp-tests/comments/test0X.nt");
    }

//    @Test public void test_comments_test05() {
//        positiveTest("http://jcarroll.hpl.hp.com/arp-tests/comments/test05",
//                     "http://jcarroll.hpl.hp.com/arp-tests/comments/test05.rdf",
//                     "http://jcarroll.hpl.hp.com/arp-tests/comments/test0X.nt");
//    }

    @Test public void test_comments_test06() {
        positiveTest("http://jcarroll.hpl.hp.com/arp-tests/comments/test06",
                     "http://jcarroll.hpl.hp.com/arp-tests/comments/test06.rdf",
                     "http://jcarroll.hpl.hp.com/arp-tests/comments/test0X.nt");
    }

    @Test public void test_comments_test07() {
        positiveTest("http://jcarroll.hpl.hp.com/arp-tests/comments/test07",
                     "http://jcarroll.hpl.hp.com/arp-tests/comments/test07.rdf",
                     "http://jcarroll.hpl.hp.com/arp-tests/comments/test0X.nt");
    }

    @Test public void test_comments_test08() {
        positiveTest("http://jcarroll.hpl.hp.com/arp-tests/comments/test08",
                     "http://jcarroll.hpl.hp.com/arp-tests/comments/test08.rdf",
                     "http://jcarroll.hpl.hp.com/arp-tests/comments/test0X.nt");
    }

//    @Test public void test_comments_test09() {
//        positiveTest("http://jcarroll.hpl.hp.com/arp-tests/comments/test09",
//                     "http://jcarroll.hpl.hp.com/arp-tests/comments/test09.rdf",
//                     "http://jcarroll.hpl.hp.com/arp-tests/comments/test0X.nt");
//    }
//
//    @Test public void test_comments_test10() {
//        positiveTest("http://jcarroll.hpl.hp.com/arp-tests/comments/test10",
//                     "http://jcarroll.hpl.hp.com/arp-tests/comments/test10.rdf",
//                     "http://jcarroll.hpl.hp.com/arp-tests/comments/test1X.nt");
//    }
//
//    @Test public void test_comments_test11() {
//        positiveTest("http://jcarroll.hpl.hp.com/arp-tests/comments/test11",
//                     "http://jcarroll.hpl.hp.com/arp-tests/comments/test11.rdf",
//                     "http://jcarroll.hpl.hp.com/arp-tests/comments/test1X.nt");
//    }
//
//    @Test public void test_comments_test12() {
//        positiveTest("http://jcarroll.hpl.hp.com/arp-tests/comments/test12",
//                     "http://jcarroll.hpl.hp.com/arp-tests/comments/test12.rdf",
//                     "http://jcarroll.hpl.hp.com/arp-tests/comments/test1X.nt");
//    }
//
//    @Test public void test_comments_test13() {
//        positiveTest("http://jcarroll.hpl.hp.com/arp-tests/comments/test13",
//                     "http://jcarroll.hpl.hp.com/arp-tests/comments/test13.rdf",
//                     "http://jcarroll.hpl.hp.com/arp-tests/comments/test1X.nt");
//    }

    @Test public void test_error_qname_in_ID_bug74() {
        negativeTest("http://jcarroll.hpl.hp.com/arp-tests/qname-in-ID/bug74",
                     "http://jcarroll.hpl.hp.com/arp-tests/qname-in-ID/bug74_0.rdf");
    }

    @Test public void test_error_relative_namespaces_50_0() {
        negativeTest("http://jcarroll.hpl.hp.com/arp-tests/relative-namespaces/50_0",
                     "http://jcarroll.hpl.hp.com/arp-tests/relative-namespaces/bad-bug50_0.rdf");
    }

    @Test public void test_error_unqualified_attribute() {
        negativeTest("http://jcarroll.hpl.hp.com/arp-tests/unqualified/attribute",
                     "http://jcarroll.hpl.hp.com/arp-tests/unqualified/attribute.rdf");
    }

    @Test public void test_error_unqualified_property() {
        negativeTest("http://jcarroll.hpl.hp.com/arp-tests/unqualified/property",
                     "http://jcarroll.hpl.hp.com/arp-tests/unqualified/property.rdf");
    }

    @Test public void test_error_unqualified_relative_namespace() {
        negativeTest("http://jcarroll.hpl.hp.com/arp-tests/unqualified/relative-namespace",
                     "http://jcarroll.hpl.hp.com/arp-tests/unqualified/relative-namespace.rdf");
    }

    @Test public void test_error_unqualified_typedNode() {
        negativeTest("http://jcarroll.hpl.hp.com/arp-tests/unqualified/typedNode",
                     "http://jcarroll.hpl.hp.com/arp-tests/unqualified/typedNode.rdf");
    }

    @Test public void test_i18n_bug73a() {
        positiveTest("http://jcarroll.hpl.hp.com/arp-tests/i18n/bug73a",
                     "http://jcarroll.hpl.hp.com/arp-tests/i18n/eq-bug73_0.rdf",
                     "http://jcarroll.hpl.hp.com/arp-tests/i18n/eq-bug73_1.rdf");
    }

    @Test public void test_i18n_bug73b() {
        positiveTest("http://jcarroll.hpl.hp.com/arp-tests/i18n/bug73b",
                     "http://jcarroll.hpl.hp.com/arp-tests/i18n/eq-bug73_0.rdf",
                     "http://jcarroll.hpl.hp.com/arp-tests/i18n/eq-bug73_2.rdf");
    }

    @Test public void test_rfc2396_issue_bug51() {
        positiveTest("http://jcarroll.hpl.hp.com/arp-tests/rfc2396-issue/bug51",
                     "http://jcarroll.hpl.hp.com/arp-tests/rfc2396-issue/bug51_0.rdf",
                     "http://jcarroll.hpl.hp.com/arp-tests/rfc2396-issue/bug51_0.nt");
    }

    @Test public void test_warn_parsetype_bug68() {
        warningTest("http://jcarroll.hpl.hp.com/arp-tests/parsetype/bug68",
                    "http://jcarroll.hpl.hp.com/arp-tests/parsetype/bug68_0.rdf",
                    "http://jcarroll.hpl.hp.com/arp-tests/parsetype/bug68_0.nt");
    }

    @Test public void test_warn_rdf_nnn_67_5() {
        warningTest("http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/67_5",
                    "http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/bad-bug67_5.rdf",
                    "http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/bad-bug67_5.nt");
    }

    @Test public void test_warn_rdf_nnn_67_6() {
        warningTest("http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/67_6",
                    "http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/bad-bug67_6.rdf",
                    "http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/bad-bug67_6.nt");
    }

    @Test public void test_warn_rdf_nnn_67_7() {
        warningTest("http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/67_7",
                    "http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/bad-bug67_7.rdf",
                    "http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/bad-bug67_7.nt");
    }

    @Test public void test_warn_rdf_nnn_67_8() {
        warningTest("http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/67_8",
                    "http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/bad-bug67_8.rdf",
                    "http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/bad-bug67_8.nt");
    }

    @Test public void test_warn_rdf_nnn_67_9() {
        warningTest("http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/67_9",
                    "http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/bad-bug67_9.rdf",
                    "http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/bad-bug67_9.nt");
    }

    @Test public void test_xml_literals_html() {
        positiveTest("http://jcarroll.hpl.hp.com/arp-tests/xml-literals/html",
                     "http://jcarroll.hpl.hp.com/arp-tests/xml-literals/html.rdf",
                     "http://jcarroll.hpl.hp.com/arp-tests/xml-literals/html.nt");
    }

    @Test public void test_xml_literals_reported1() {
        positiveTest("http://jcarroll.hpl.hp.com/arp-tests/xml-literals/reported1",
                     "http://jcarroll.hpl.hp.com/arp-tests/xml-literals/reported1.rdf",
                     "http://jcarroll.hpl.hp.com/arp-tests/xml-literals/reported1.nt");
    }

    @Test public void test_xml_literals_reported2() {
        positiveTest("http://jcarroll.hpl.hp.com/arp-tests/xml-literals/reported2",
                     "http://jcarroll.hpl.hp.com/arp-tests/xml-literals/reported2.rdf",
                     "http://jcarroll.hpl.hp.com/arp-tests/xml-literals/reported2.nt");
    }

    @Test public void test_xml_literals_reported3() {
        positiveTest("http://jcarroll.hpl.hp.com/arp-tests/xml-literals/reported3",
                     "http://jcarroll.hpl.hp.com/arp-tests/xml-literals/reported3.rdf",
                     "http://jcarroll.hpl.hp.com/arp-tests/xml-literals/reported3.nt");
    }

    // Run in DOM tests in jena-core, not in RIOT usage as a document parser.
//    @Test public void test_xmlns_test02() {
//        positiveTest("http://jcarroll.hpl.hp.com/arp-tests/xmlns/test02",
//                     "http://jcarroll.hpl.hp.com/arp-tests/xmlns/test02.rdf",
//                     "http://jcarroll.hpl.hp.com/arp-tests/xmlns/test0X.nt");
//    }
//
//    @Test public void test_warn_rdf_nnn_67_0() {
//        warningTest("http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/67_0",
//                    "http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/bad-bug67_0.rdf",
//                    "http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/bad-bug67_0.nt");
//    }
//
//    @Test public void test_xmlns_test01() {
//        positiveTest("http://jcarroll.hpl.hp.com/arp-tests/xmlns/test01",
//                     "http://jcarroll.hpl.hp.com/arp-tests/xmlns/test01.rdf",
//                     "http://jcarroll.hpl.hp.com/arp-tests/xmlns/test0X.nt");
//    }

    @Test public void test_xmlns_test03() {
        positiveTest("http://jcarroll.hpl.hp.com/arp-tests/xmlns/test03",
                     "http://jcarroll.hpl.hp.com/arp-tests/xmlns/test03.rdf",
                     "http://jcarroll.hpl.hp.com/arp-tests/xmlns/test0X.nt");
    }
}