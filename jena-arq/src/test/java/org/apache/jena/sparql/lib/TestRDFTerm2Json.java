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

package org.apache.jena.sparql.lib;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.apache.jena.atlas.json.*;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.sse.SSE;
import org.junit.Test;

public class TestRDFTerm2Json {
    
    @Test public void n2j_1() { test("'abc'", new JsonString("abc")); }

    @Test public void n2j_2() { test("<http://jena.apache.org/>", new JsonString("http://jena.apache.org/")); }
    
    @Test public void n2j_3() { test("123", JsonNumber.value(123)); }

    @Test public void n2j_4() { test("123.0", JsonNumber.value(new BigDecimal("123.0"))); }
    
    @Test public void n2j_5() { test("123e0", JsonNumber.value(/*(double)*/123e0)); }

    @Test public void n2j_6() { test("'123e0'^^xsd:float", JsonNumber.value((float)123.0)); }

    @Test public void n2j_7() { test("true", new JsonBoolean(true)); }
    
    @Test public void n2j_8() { test("'text'@en", new JsonString("text")) ; }

    @Test public void n2j_9() { assertEquals(JsonNull.instance, RDFTerm2Json.fromNode(null)) ; }

    
    private void test(String nodeStr, JsonValue expected) {
        Node n = SSE.parseNode(nodeStr);
        JsonValue jv = RDFTerm2Json.fromNode(n);
        assertEquals(expected, jv);
    }
    
}
