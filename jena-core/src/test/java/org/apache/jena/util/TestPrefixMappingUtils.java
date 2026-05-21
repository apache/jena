/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.util;

import java.io.StringReader;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.graph.GraphMemFactory;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.vocabulary.XSD;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestPrefixMappingUtils {

    static Graph create(String data) {
        Graph graph = GraphMemFactory.createDefaultGraph();
        Model m = ModelFactory.createModelForGraph(graph);
        m.read(new StringReader(data), null, "TTL");
        return graph;
    }

    static int size(PrefixMapping pmap) {
        return pmap.getNsPrefixMap().size();
    }

    @Test
    public void prefixes1() {
        String data1 = StrUtils.strjoinNL("@prefix : <http://example/> .", "@prefix ex: <http://example/ex#> .",
                                          "@prefix xsd: <" + XSD.getURI() + "> .", "", ":s1 :p :x1 .", ":s1 ex:p :x1 .", ":s1 ex:p 1 .");
        Graph graph1 = create(data1);
        PrefixMapping pmap = PrefixMappingUtils.calcInUsePrefixMapping(graph1);
        PrefixMapping pmapExpected = graph1.getPrefixMapping();
        assertEquals(3, size(pmap));
        assertEquals(pmapExpected, pmap);
    }

    @Test
    public void prefixes2() {
        String data2 = StrUtils.strjoinNL("@prefix : <http://example/> .", "@prefix ex: <http://example/ex#> .",
                                          "@prefix notinuse: <http://example/whatever/> .", "", ":s1 :p :x1 .", ":s1 ex:p :x1 .");

        Graph graph1 = create(data2);
        PrefixMapping pmap = PrefixMappingUtils.calcInUsePrefixMapping(graph1);
        PrefixMapping pmapExpected = new PrefixMappingImpl();
        pmapExpected.setNsPrefix("", "http://example/");
        pmapExpected.setNsPrefix("ex", "http://example/ex#");
        assertEquals(2, size(pmap));
        assertTrue(sameMapping(pmapExpected, pmap));
        assertNull(pmap.getNsPrefixURI("notinuse"));
    }

    @Test
    public void prefixes3() {
        String data = StrUtils.strjoinNL("@prefix : <http://example/> .", "", "<http://other/s1> :p :x1 .");
        Graph graph1 = create(data);
        PrefixMapping pmap = PrefixMappingUtils.calcInUsePrefixMapping(graph1);
        PrefixMapping pmapExpected = new PrefixMappingImpl();
        pmapExpected.setNsPrefix("", "http://example/");
        assertTrue(sameMapping(pmapExpected, pmap));
    }

    @Test
    public void prefixes4() {
        String data = StrUtils.strjoinNL("<http://other/s1> <http://example/p> 123 .");
        Graph graph1 = create(data);
        PrefixMapping pmap = PrefixMappingUtils.calcInUsePrefixMapping(graph1);
        assertEquals(0, size(pmap));
        PrefixMapping pmapExpected = new PrefixMappingImpl();
        assertTrue(sameMapping(pmapExpected, pmap));
    }

    @Test
    public void prefixesN() {
        String data = StrUtils.strjoinNL("@prefix : <http://example/> .", "@prefix ex: <http://example/ex#> .",
                                          "@prefix notinuse: <http://example/whatever/> .", "@prefix indirect: <urn:foo:> .",
                                          "@prefix indirectx: <urn:ex:> .",

                                          "@prefix ns: <http://host/ns> .", "@prefix ns1: <http://host/ns1> .",
                                          "@prefix ns2: <http://host/nspace> .", "", ":s1 :p :x1 .", ":s1 ex:p :x1 .",

                                          "<urn:foo:bar> :p 1 . ", "<urn:ex:a:b> :p 2 . ",

                                          "<urn:ex:verybad#.> :p 1 . ",

                                          "ns:x ns1:p 'ns1' . ",

                                          "<http://examp/abberev> indirect:p 'foo' . ");

        Graph graph = create(data);
        PrefixMapping pmap = PrefixMappingUtils.calcInUsePrefixMapping(graph);
        PrefixMapping pmapExpected = new PrefixMappingImpl();
        pmapExpected.setNsPrefix("", "http://example/");
        pmapExpected.setNsPrefix("ex", "http://example/ex#");
        pmapExpected.setNsPrefix("indirect", "urn:foo:");
        pmapExpected.setNsPrefix("ns", "http://host/ns");
        pmapExpected.setNsPrefix("ns1", "http://host/ns1");
        pmapExpected.setNsPrefix("indirectx", "urn:ex:");
        assertTrue(sameMapping(pmapExpected, pmap));
        assertNull(pmap.getNsPrefixURI("notinuse"));
    }

    @Test
    public void prefixesTTL1() {
        String data1 = StrUtils.strjoinNL("@prefix : <http://example/> .", "@prefix ex: <http://example/ex#> .",
                                          "@prefix xsd: <" + XSD.getURI() + "> .", "", ":s1 :p :x1 .", ":s1 ex:p :x1 .", ":s1 ex:p 1 .");
        Graph graph1 = create(data1);
        PrefixMapping pmap = PrefixMappingUtils.calcInUsePrefixMappingTTL(graph1);
        PrefixMapping pmapExpected = graph1.getPrefixMapping();
        assertEquals(3, size(pmap));
        assertEquals(pmapExpected, pmap);
    }

    @Test
    public void prefixesTTL2() {
        String data2 = StrUtils.strjoinNL("@prefix : <http://example/> .", "@prefix ex: <http://example/ex#> .",
                                          "@prefix notinuse: <http://example/whatever/> .", "", ":s1 :p :x1 .", ":s1 ex:p :x1 .");

        Graph graph1 = create(data2);
        PrefixMapping pmap = PrefixMappingUtils.calcInUsePrefixMappingTTL(graph1);
        PrefixMapping pmapExpected = new PrefixMappingImpl();
        pmapExpected.setNsPrefix("", "http://example/");
        pmapExpected.setNsPrefix("ex", "http://example/ex#");
        assertEquals(2, size(pmap));
        assertTrue(sameMapping(pmapExpected, pmap));
        assertNull(pmap.getNsPrefixURI("notinuse"));
    }

    @Test
    public void prefixesTTL3() {
        String data = StrUtils.strjoinNL("@prefix : <http://example/> .", "", "<http://other/s1> :p :x1 .");
        Graph graph1 = create(data);
        PrefixMapping pmap = PrefixMappingUtils.calcInUsePrefixMappingTTL(graph1);
        PrefixMapping pmapExpected = new PrefixMappingImpl();
        pmapExpected.setNsPrefix("", "http://example/");
        assertTrue(sameMapping(pmapExpected, pmap));
    }

    @Test
    public void prefixesTTL4() {
        String data = StrUtils.strjoinNL("<http://other/s1> <http://example/p> 123 .");
        Graph graph1 = create(data);
        PrefixMapping pmap = PrefixMappingUtils.calcInUsePrefixMappingTTL(graph1);
        assertEquals(0, size(pmap));
        PrefixMapping pmapExpected = new PrefixMappingImpl();
        assertTrue(sameMapping(pmapExpected, pmap));
    }

    @Test
    public void prefixesTTL() {
        String data = StrUtils.strjoinNL("@prefix : <http://example/> .", "@prefix ex: <http://example/ex#> .",
                                          "@prefix notinuse: <http://example/whatever/> .", "@prefix indirect: <urn:foo:> .",
                                          "@prefix indirectx: <urn:x:> .",

                                          "@prefix ns: <http://host/ns> .", "@prefix ns1: <http://host/ns1> .",
                                          "@prefix ns2: <http://host/nspace> .", "", ":s1 :p :x1 .", ":s1 ex:p :x1 .",

                                          "<urn:foo:bar> :p 1 . ", "<urn:x:a:b> :p 2 . ",

                                          "<urn:verybad#.> :p 1 . ",

                                          "ns:x ns1:p 'ns1' . ",

                                          "<http://examp/abberev> indirect:p 'foo' . ");

        Graph graph = create(data);

        PrefixMapping pmap = PrefixMappingUtils.calcInUsePrefixMappingTTL(graph);
        PrefixMapping pmapExpected = new PrefixMappingImpl();
        pmapExpected.setNsPrefix("", "http://example/");
        pmapExpected.setNsPrefix("ex", "http://example/ex#");
        pmapExpected.setNsPrefix("indirect", "urn:foo:");
        assertTrue(sameMapping(pmapExpected, pmap));
        assertNull(pmap.getNsPrefixURI("notinuse"));
    }

    private boolean sameMapping(PrefixMapping pmapExpected, PrefixMapping pmap) {
        return pmapExpected.samePrefixMappingAs(pmap);
    }

    private static void print(String label, PrefixMapping pmap) {
        System.out.println(label);
        pmap.getNsPrefixMap().forEach((p, u) -> System.out.printf("    %s: -> <%s>\n", p, u));
    }
}
