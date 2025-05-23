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
package org.apache.jena.geosparql.spatial.index.v2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.geosparql.kryo.NodeSerializer;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Node_Graph;
import org.apache.jena.graph.TextDirection;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphMatcher;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.vocabulary.RDF;
import org.junit.Assert;
import org.junit.Test;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class TestNodeSerializer {

    private static final Node S = NodeFactory.createURI("http://www.example.org/s");
    private static final Node B = NodeFactory.createBlankNode("abcde");
    private static final Triple T = Triple.create(S, RDF.Nodes.type, B);
    private static final Graph G = GraphFactory.createDefaultGraph();

    private static final Kryo kryo = new Kryo();

    static {
        G.add(T);
        NodeSerializer.register(kryo);
        KryoRegistratorSpatialIndexV2.registerTripleSerializer(kryo);
        registerGraphSerializer(kryo);
    }

    @Test public void testUri() { assertRoundtrip(S); }

    @Test public void testBnode() { assertRoundtrip(NodeFactory.createBlankNode()); }

    @Test public void testLitStr() { assertRoundtrip(NodeFactory.createLiteralString("hello")); }
    @Test public void testLitDtStd() { assertRoundtrip(NodeFactory.createLiteralDT("1", XSDDatatype.XSDinteger)); }
    @Test public void testLitDtCustom() { assertRoundtrip(NodeFactory.createLiteralDT("1", new BaseDatatype("http://www.example.org/myDatatype"))); }
    @Test public void testLitLang() { assertRoundtrip(NodeFactory.createLiteralLang("hello", "en")); }

    @Test public void testLitDirLangNone() { assertRoundtrip(NodeFactory.createLiteralDirLang("hello", "en", Node.noTextDirection)); }
    @Test public void testLitDirLangLtr() { assertRoundtrip(NodeFactory.createLiteralDirLang("hello", "en", TextDirection.LTR)); }
    @Test public void testLitDirLangRtl() { assertRoundtrip(NodeFactory.createLiteralDirLang("hello", "en", TextDirection.RTL)); }

    @Test public void testStar() { assertRoundtrip(NodeFactory.createTripleTerm(T)); }

    @Test public void testVarRdf() { assertRoundtrip(NodeFactory.createVariable("rdfVar")); }
    @Test public void testVarSparql() { assertRoundtrip(Var.alloc("sparqlVar")); }

    @Test public void testAny() { assertRoundtrip(Node.ANY); }

    @Test public void testGraph() {
        Node_Graph after = (Node_Graph)roundtrip(NodeFactory.createGraphNode(G));
        Assert.assertTrue(GraphMatcher.equals(G, after.getGraph()));
    }

    // For completeness also test Node_Graph serialization
    private static void registerGraphSerializer(Kryo kryo) {
        Graph prototype = GraphFactory.createDefaultGraph();
        Class<?> prototypeClass = prototype.getClass();
        Serializer<Graph> graphSerializer = new SimpleGraphSerializer();
        kryo.register(Graph.class, graphSerializer);
        kryo.register(prototypeClass, graphSerializer);
    }

    public static Node roundtrip(Node expected) {
        byte[] bytes;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (Output out = new Output(baos)) {
                kryo.writeClassAndObject(out, expected);
                out.flush();
            }
            bytes = baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Node result;
        try (Input in = new Input(new ByteArrayInputStream(bytes))) {
            result = (Node)kryo.readClassAndObject(in);
        }
        return result;
    }

    public static void assertRoundtrip(Node expected) {
        Node actual = roundtrip(expected);
        Assert.assertEquals(expected, actual);
    }
}
