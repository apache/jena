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

package org.apache.jena.riot.system;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.*;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sys.JenaSystem;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestSerializable {
    @BeforeClass
    public static void beforeClass() {
        JenaSystem.init();    
    }
    
    private static <X> X roundTrip(X n) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(out);
        // writeReplace.
        oos.writeObject(n);

        byte b[] = out.toByteArray();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(b));

        // readResolve
        @SuppressWarnings("unchecked")
        X x = (X)ois.readObject();
        return x;
    }

    @Test
    public void serialize_node_01() throws Exception {
        Node n = SSE.parseNode("<http://example/>");
        Node n1 = roundTrip(n);
        assertTrue(n1.isURI());
        assertEquals(n, n1);
    }
    
    @Test
    public void serialize_node_02() throws Exception {
        Node n = SSE.parseNode("123");
        Node n1 = roundTrip(n);
        assertTrue(n1.isLiteral());
        assertEquals(n, n1);
    }
    
    @Test
    public void serialize_node_03() throws Exception {
        Node n = SSE.parseNode("_:b");
        Node n1 = roundTrip(n);
        assertTrue(n1.isBlank());
        assertEquals(n, n1);
    }

    @Test
    public void serialize_node_04() throws Exception {
        Node n = Node.ANY;
        Node n1 = roundTrip(n);
        assertEquals(n, n1);
        assertSame(n, n1);
    }
    
    @Test
    public void serialize_node_05() throws Exception {
        Var v = Var.alloc("X");
        Var v1 = roundTrip(v);
        assertEquals(v, v1);
    }
    
    @Test
    public void serialize_node_06() throws Exception {
        Node v = NodeFactory.createVariable("Foo");
        Node v1 = roundTrip(v); // This will be a "Var".
        assertEquals(v, v1);
    }

    @Test
    public void serialize_triple_01() throws Exception {
        Triple t = SSE.parseTriple("(:s :p :o)");
        Triple t1 = roundTrip(t);
        assertEquals(t, t1);
    }
    
    @Test
    public void serialize_triple_02() throws Exception {
        Triple t = SSE.parseTriple("(:x :x :x)");
        Triple t1 = roundTrip(t);
        assertEquals(t, t1);
        assertEquals(t1.getSubject(), t1.getObject());
    }

    @Test
    public void serialize_triple_03() throws Exception {
        Triple t = SSE.parseTriple("(?a _:b 123)");
        Triple t1 = roundTrip(t);
        assertEquals(t, t1);
    }

    @Test
    public void serialize_triple_04() throws Exception {
        Triple t = SSE.parseTriple("(_:b _:b _:c)");
        Triple t1 = roundTrip(t);
        assertEquals(t, t1);
        assertEquals(t1.getSubject(), t1.getPredicate());
        assertNotEquals(t1.getSubject(), t1.getObject());
        assertTrue(t1.getSubject().isBlank());
    }

    @Test
    public void serialize_quad_01() throws Exception {
        Quad q = SSE.parseQuad("(:g :s :p :o)");
        Quad q1 = roundTrip(q);
        assertEquals(q, q1);
    }

    @Test
    public void serialize_quad_02() throws Exception {
        Quad q = SSE.parseQuad("(_ :s :p :o)");
        Quad q1 = roundTrip(q);
        assertEquals(q, q1);
        assertNotEquals(q.getSubject(), q1.getObject());
    }

    @Test
    public void serialize_quad_03() throws Exception {
        Quad q = SSE.parseQuad("(<_:abc> ?y ?x <_:abc>)");
        Quad q1 = roundTrip(q);
        assertEquals(q, q1);
        assertEquals(q.getGraph(), q1.getObject());
    }

    @Test
    public void serialize_quad_04() throws Exception {
        Quad q = SSE.parseQuad("(<_:abc> <_:abc> <_:def> <_:abc>)");
        Quad q1 = roundTrip(q);
        assertEquals(q, q1);
        assertEquals(q.getSubject(), q1.getObject());
        assertNotEquals(q.getSubject(), q1.getPredicate());
    }
}
