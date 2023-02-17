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
import org.apache.jena.rdfxml.libtest.InputStreamFactoryTests;

public class TestSuiteWG_RDFXML_ARP
{
    static public Test suite()
    {
        String base = "http://jcarroll.hpl.hp.com/arp-tests/";

        WGTestSuite testSuite = new org.apache.jena.rdfxml.xmlinput.WGTestSuite
                (new InputStreamFactoryTests(base, "arp")
                ,"ARP RDF/XML Tests"
                ,false) ;
        Test test177 = testSuite.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/xml-literals/reported3",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/xml-literals/reported3.rdf",
                                                  true,
                                                  "http://jcarroll.hpl.hp.com/arp-tests/xml-literals/reported3.nt",
                                                  false) ;
        testSuite.addTest(test177) ;
        Test test177x = testSuite.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/xml-literals/html",
                                                   "http://jcarroll.hpl.hp.com/arp-tests/xml-literals/html.rdf", true,
                                                   "http://jcarroll.hpl.hp.com/arp-tests/xml-literals/html.nt", false) ;
        testSuite.addTest(test177x) ;
        Test test178 = testSuite.createWarningTest("http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/67_6",
                                                 "http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/bad-bug67_6.rdf", true,
                                                 "http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/bad-bug67_6.nt", false,
                                                 new int[]{103,}) ;
        testSuite.addTest(test178) ;
        Test test179 = testSuite.createWarningTest("http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/67_5",
                                                 "http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/bad-bug67_5.rdf", true,
                                                 "http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/bad-bug67_5.nt", false,
                                                 new int[]{103,}) ;
        testSuite.addTest(test179) ;
        Test test180 = testSuite.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/comments/test10",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test10.rdf", true,
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test1X.nt", false) ;
        testSuite.addTest(test180) ;
        Test test181 = testSuite.createWarningTest("http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/67_7",
                                                 "http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/bad-bug67_7.rdf", true,
                                                 "http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/bad-bug67_7.nt", false,
                                                 new int[]{113,}) ;
        testSuite.addTest(test181) ;
        Test test182 = testSuite.createWarningTest("http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/67_8",
                                                 "http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/bad-bug67_8.rdf", true,
                                                 "http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/bad-bug67_8.nt", false,
                                                 new int[]{103, 113,}) ;
        testSuite.addTest(test182) ;
        Test test183 = testSuite.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/comments/test01",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test01.rdf", true,
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test0X.nt", false) ;
        testSuite.addTest(test183) ;
        Test test184 = testSuite.createWarningTest("http://jcarroll.hpl.hp.com/arp-tests/parsetype/bug68",
                                                 "http://jcarroll.hpl.hp.com/arp-tests/parsetype/bug68_0.rdf", true,
                                                 "http://jcarroll.hpl.hp.com/arp-tests/parsetype/bug68_0.nt", false,
                                                 new int[]{106,}) ;
        testSuite.addTest(test184) ;
        Test test185 = testSuite.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/i18n/bug73a",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/i18n/eq-bug73_0.rdf", true,
                                                  "http://jcarroll.hpl.hp.com/arp-tests/i18n/eq-bug73_1.rdf", true) ;
        testSuite.addTest(test185) ;
        Test test186 = testSuite.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/comments/test12",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test12.rdf", true,
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test1X.nt", false) ;
        testSuite.addTest(test186) ;
        Test test187 = testSuite.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/comments/test11",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test11.rdf", true,
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test1X.nt", false) ;
        testSuite.addTest(test187) ;
        Test test188 = testSuite.createNegativeTest("http://jcarroll.hpl.hp.com/arp-tests/qname-in-ID/bug74",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/qname-in-ID/bug74_0.rdf", true,
                                                  new int[]{108,}) ;
        testSuite.addTest(test188) ;
        Test test189 = testSuite.createNegativeTest(
                                                  "http://jcarroll.hpl.hp.com/arp-tests/relative-namespaces/50_0",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/relative-namespaces/bad-bug50_0.rdf",
                                                  true, new int[]{109, 136,}) ;
        testSuite.addTest(test189) ;
        Test test190 = testSuite.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/rfc2396-issue/bug51",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/rfc2396-issue/bug51_0.rdf",
                                                  true,
                                                  "http://jcarroll.hpl.hp.com/arp-tests/rfc2396-issue/bug51_0.nt",
                                                  false) ;
        testSuite.addTest(test190) ;
        Test test191 = testSuite.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/comments/test03",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test03.rdf", true,
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test0X.nt", false) ;
        testSuite.addTest(test191) ;
        Test test192 = testSuite.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/comments/test02",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test02.rdf", true,
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test0X.nt", false) ;
        testSuite.addTest(test192) ;

        // This has become a parse error (URI checknamespace)
//        Test test193 = testSuite.createNegativeTest("http://jcarroll.hpl.hp.com/arp-tests/xmlns/bad01",
//                                                  "http://jcarroll.hpl.hp.com/arp-tests/xmlns/bad01.rdf", true,
//                                                  new int[]{124, 107,}) ;
//        testSuite.addTest(test193) ;

        Test tProp = testSuite.createNegativeTest("http://jcarroll.hpl.hp.com/arp-tests/unqualified/property",
                                                "http://jcarroll.hpl.hp.com/arp-tests/unqualified/property.rdf", true,
                                                new int[]{104, 136}) ;
        testSuite.addTest(tProp) ;
        Test tAttr = testSuite.createNegativeTest("http://jcarroll.hpl.hp.com/arp-tests/unqualified/attribute",
                                                "http://jcarroll.hpl.hp.com/arp-tests/unqualified/attribute.rdf", true,
                                                new int[]{102, 136}) ;
        testSuite.addTest(tAttr) ;
        Test tType = testSuite.createNegativeTest("http://jcarroll.hpl.hp.com/arp-tests/unqualified/typedNode",
                                                "http://jcarroll.hpl.hp.com/arp-tests/unqualified/typedNode.rdf", true,
                                                new int[]{104, 136}) ;
        testSuite.addTest(tType) ;
        Test tRelative = testSuite.createNegativeTest(
                                                    "http://jcarroll.hpl.hp.com/arp-tests/unqualified/relative-namespace",
                                                    "http://jcarroll.hpl.hp.com/arp-tests/unqualified/relative-namespace.rdf",
                                                    true, new int[]{109, 136}) ;
        testSuite.addTest(tRelative) ;

        Test test194 = testSuite.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/comments/test09",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test09.rdf", true,
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test0X.nt", false) ;
        testSuite.addTest(test194) ;
        Test test195 = testSuite.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/comments/test08",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test08.rdf", true,
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test0X.nt", false) ;
        testSuite.addTest(test195) ;
        Test test196 = testSuite.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/comments/test13",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test13.rdf", true,
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test1X.nt", false) ;
        testSuite.addTest(test196) ;
        Test test197 = testSuite.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/i18n/bug73b",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/i18n/eq-bug73_0.rdf", true,
                                                  "http://jcarroll.hpl.hp.com/arp-tests/i18n/eq-bug73_2.rdf", true) ;
        testSuite.addTest(test197) ;
        Test test198 = testSuite.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/comments/test05",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test05.rdf", true,
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test0X.nt", false) ;
        testSuite.addTest(test198) ;
        Test test199 = testSuite.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/comments/test04",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test04.rdf", true,
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test0X.nt", false) ;
        testSuite.addTest(test199) ;
     // This has become a parse warning on file:/
//        Test test200 = testSuite.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/rfc2396-issue/fileURI",
//                                                  "http://jcarroll.hpl.hp.com/arp-tests/rfc2396-issue/fileURI.rdf",
//                                                  true,
//                                                  "http://jcarroll.hpl.hp.com/arp-tests/rfc2396-issue/fileURI.nt",
//                                                  false) ;
//        testSuite.addTest(test200) ;
        Test test201 = testSuite.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/comments/test07",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test07.rdf", true,
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test0X.nt", false) ;
        testSuite.addTest(test201) ;
        Test test202 = testSuite.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/comments/test06",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test06.rdf", true,
                                                  "http://jcarroll.hpl.hp.com/arp-tests/comments/test0X.nt", false) ;
        testSuite.addTest(test202) ;
        Test test203 = testSuite.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/xml-literals/reported1",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/xml-literals/reported1.rdf",
                                                  true,
                                                  "http://jcarroll.hpl.hp.com/arp-tests/xml-literals/reported1.nt",
                                                  false) ;
        testSuite.addTest(test203) ;
        Test test204 = testSuite.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/xml-literals/reported2",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/xml-literals/reported2.rdf",
                                                  true,
                                                  "http://jcarroll.hpl.hp.com/arp-tests/xml-literals/reported2.nt",
                                                  false) ;
        testSuite.addTest(test204) ;
        Test test205 = testSuite.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/xmlns/test01",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/xmlns/test01.rdf", true,
                                                  "http://jcarroll.hpl.hp.com/arp-tests/xmlns/test0X.nt", false) ;
        testSuite.addTest(test205) ;

        // IRI error "<" in path
//        Test test206 = testSuite.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/i18n/t9000",
//                                                  "http://jcarroll.hpl.hp.com/arp-tests/i18n/t9000.rdf", true,
//                                                  "http://jcarroll.hpl.hp.com/arp-tests/i18n/t9000.nt", false) ;
//        // new int[]{ //135, 135,135, 135,135, 135,135, 135,135,
//        // 135, });
//        testSuite.addTest(test206) ;
        Test test207 = testSuite.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/xmlns/test03",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/xmlns/test03.rdf", true,
                                                  "http://jcarroll.hpl.hp.com/arp-tests/xmlns/test0X.nt", false) ;
        testSuite.addTest(test207) ;
        Test test208 = testSuite.createPositiveTest("http://jcarroll.hpl.hp.com/arp-tests/xmlns/test02",
                                                  "http://jcarroll.hpl.hp.com/arp-tests/xmlns/test02.rdf", true,
                                                  "http://jcarroll.hpl.hp.com/arp-tests/xmlns/test0X.nt", false) ;
        testSuite.addTest(test208) ;
        Test test209 = testSuite.createWarningTest("http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/67_0",
                                                 "http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/bad-bug67_0.rdf", true,
                                                 "http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/bad-bug67_0.nt", false,
                                                 new int[]{103,}) ;
        testSuite.addTest(test209) ;
        Test test210 = testSuite.createWarningTest("http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/67_9",
                                                 "http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/bad-bug67_9.rdf", true,
                                                 "http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/bad-bug67_9.nt", false,
                                                 new int[]{103, 113,}) ;
        testSuite.addTest(test210) ;
        return testSuite ;
    }
}
