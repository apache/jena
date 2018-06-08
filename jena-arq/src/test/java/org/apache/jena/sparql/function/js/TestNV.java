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

package org.apache.jena.sparql.function.js;

import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.sse.SSE;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestNV {

    @Test public void nv_1() { test("'abc'"); }
    @Test public void nv_2() { test("true"); }
    @Test public void nv_3() { test("123"); }
    @Test public void nv_4() { test("123.5"); }
    
    // No conversion to JS - becomes an NV.
    @Test public void nv_5() { test("'2018-01-06T17:56:41.293+00:00'^^xsd:dateTime"); }
    @Test public void nv_6() { test("<http://jena.apache.org/>"); }
    @Test public void nv_7() { test("_:abc123"); }
    
    @Test public void nv_10() {
        NodeValue nodeValue = nv("'abc'");
        NV nv = new NV(nodeValue);
        assertEquals("abc", nv.getLex());
        assertEquals("abc", nv.getValue());
        assertEquals("Literal", nv.getTermType());
    }

    @Test public void nv_12() {
        NodeValue nodeValue = nv("<http://jena.apache.org/>");
        NV nv = new NV(nodeValue);
        assertEquals("http://jena.apache.org/", nv.getUri());
        assertEquals("http://jena.apache.org/", nv.getValue());
        assertEquals("NamedNode", nv.getTermType());
    }

    @Test public void nv_13() {
        NodeValue nodeValue = nv("_:a");
        NV nv = new NV(nodeValue);
        assertEquals(nv.getLabel(), nv.getValue());
        assertEquals("BlankNode", nv.getTermType());
    }
    
    private void test(String str) {
        NodeValue nv = nv(str);
        Object x = NV.fromNodeValue(nv);
        NodeValue nv2 = NV.toNodeValue(x);
        assertEquals(nv,nv2);
    }

    private static NodeValue nv(String str) {
        return NodeValue.makeNode(SSE.parseNode(str)); 
    }
}
