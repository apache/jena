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

package org.apache.jena.riot.out;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.util.NodeFactoryExtra ;
import com.hp.hpl.jena.vocabulary.RDF ;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.riot.out.NodeFmtLib ;
import org.junit.Test ;

public class TestNodeFmtLib extends BaseTest
{
    // : is 3A 
    // - is 2D
    
    // BNode labels.
    
    @Test public void encode_01() { testenc("abc", "Babc") ; }
    @Test public void encode_02() { testenc("-", "BX2D") ; }
    @Test public void encode_03() { testenc("abc:def-ghi", "BabcX3AdefX2Dghi") ; }
    @Test public void encode_04() { testenc("01X", "B01XX") ; }
    @Test public void encode_05() { testenc("-X", "BX2DXX") ; }

    @Test public void rt_01() {  testencdec("a") ; }
    @Test public void rt_02() {  testencdec("") ; }
    @Test public void rt_03() {  testencdec("abc") ; }
    @Test public void rt_04() {  testencdec("000") ; }
    @Test public void rt_05() {  testencdec("-000") ; }
    @Test public void rt_06() {  testencdec("X-") ; }
    @Test public void rt_07() {  testencdec("-123:456:xyz") ; }
    
    private void testenc(String input, String expected)
    {
        String x = NodeFmtLib.encodeBNodeLabel(input) ;
        assertEquals(expected, x) ;
    }
    
    private void testencdec(String input)
    {
        String x = NodeFmtLib.encodeBNodeLabel(input) ;
        String y = NodeFmtLib.decodeBNodeLabel(x) ;
        assertEquals(input, y) ;
    }

    @Test public void fmtNode_01() { test ("<a>", "<a>") ; }
    
    @Test public void fmtNode_02() { test ("<"+RDF.getURI()+"type>", "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>") ; }
    @Test public void fmtNode_03() { test ("'123'^^xsd:integer", "\"123\"^^<http://www.w3.org/2001/XMLSchema#integer>") ; }
    @Test public void fmtNode_04() { test ("'abc'^^xsd:integer", "\"abc\"^^<http://www.w3.org/2001/XMLSchema#integer>") ; }

    @Test public void fmtNode_05() { testDisplay ("<"+RDF.getURI()+"type>", "rdf:type") ; }
    @Test public void fmtNode_06() { testDisplay ("'123'^^xsd:integer", "123") ; }
    @Test public void fmtNode_07() { testDisplay ("'abc'^^xsd:integer", "\"abc\"^^xsd:integer") ; }
    
    private static void test(String node, String output)
    { test(NodeFactoryExtra.parseNode(node) , output) ; }
    
    private static void test(Node node, String output)
    {
        String x = NodeFmtLib.str(node) ;
        assertEquals(output, x) ;
    }
    
    private static void testDisplay(String node, String output)
    { testDisplay(NodeFactoryExtra.parseNode(node) , output) ; }
    
    private static void testDisplay(Node node, String output)
    {
        String x = NodeFmtLib.displayStr(node) ;
        assertEquals(output, x) ;
    }

}
