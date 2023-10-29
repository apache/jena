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

package org.apache.jena.riot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.lang.LabelToNode;
import org.apache.jena.riot.system.ErrorHandlerFactory;
import org.apache.jena.riot.system.FactoryRDFStd;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.riot.system.stream.LocatorFile;
import org.apache.jena.riot.system.stream.StreamManager;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.sse.SSE;
import org.junit.Test;

public class TestRDFParser {

    // Location of test files.
    private static String DIR = "testing/RIOT/Parser/";
    private static String testdata = "@prefix : <http://example/ns#> . :x :x _:b .";

    @Test public void source_not_uri_01() {
        Graph graph = GraphFactory.createGraphMem();
        RDFParserBuilder.create().lang(Lang.TTL).fromString(testdata).parse(graph);
        assertEquals(1, graph.size());
    }

    @Test public void source_not_uri_02() {
        Graph graph = GraphFactory.createGraphMem();
        InputStream input = new ByteArrayInputStream(testdata.getBytes(StandardCharsets.UTF_8));
        RDFParser.create().lang(Lang.TTL).source(input).parse(graph);
        assertEquals(1, graph.size());
    }

    @Test public void source_uri_01() {
        Graph graph = GraphFactory.createGraphMem();
        RDFParser.create().source("file:"+DIR+"data.ttl").parse(graph);
        assertEquals(3, graph.size());
    }

    @Test(expected=RiotException.class)
    public void source_uri_02() {
        Graph graph = GraphFactory.createGraphMem();
        RDFParser.create().source("file:"+DIR+"data.unknown").parse(graph);
    }

    @Test
    public void source_uri_03() {
        Graph graph = GraphFactory.createGraphMem();
        RDFParser.create().source("file:"+DIR+"data.unknown").lang(Lang.TTL).parse(graph);
        assertEquals(3, graph.size());
    }

    @Test
    public void source_uri_04() {
        Graph graph = GraphFactory.createGraphMem();
        RDFParser.create()
            .source(Path.of(DIR+"data.ttl"))
            .parse(graph);
        assertEquals(3, graph.size());
    }

    @Test
    public void source_uri_05() {
        // Last source wins.
        Graph graph = GraphFactory.createGraphMem();
        RDFParser.create()
            .source("http://example/")
            .source(DIR+"data.ttl")
            .parse(graph);
        assertEquals(3, graph.size());
    }

    // Shortcut source
    @Test public void source_shortcut_01() {
        Graph graph = GraphFactory.createGraphMem();
        RDFParser.fromString(testdata, Lang.TTL).parse(graph);
        assertEquals(1, graph.size());
    }

    @Test(expected=RiotNotFoundException.class)
    public void source_notfound_1() {
        // Last source wins.
        Graph graph = GraphFactory.createGraphMem();
        RDFParser.create()
            .source(Path.of(DIR+"data.nosuchfile.ttl"))
            .parse(graph);
        assertEquals(3, graph.size());
    }

    @Test(expected=RiotNotFoundException.class)
    public void source_notfound_2() {
        // Last source wins.
        Graph graph = GraphFactory.createGraphMem();
        RDFParser.create()
            .source(DIR+"data.nosuchfile.ttl")
            .parse(graph);
        assertEquals(3, graph.size());
    }

    @Test(expected=RiotException.class)
    public void source_uri_hint_lang() {
        Graph graph = GraphFactory.createGraphMem();
        RDFParser.create().source("file:data.rdf")
            .lang(Lang.RDFXML)
            .errorHandler(ErrorHandlerFactory.errorHandlerNoLogging)
            .parse(graph);
        assertEquals(3, graph.size());
    }

    @Test
    public void source_string() {
        Graph graph = GraphFactory.createGraphMem();
        RDFParser.create().fromString("<x> <p> <z> .")
            .lang(Lang.NT)
            .parse(graph);
        assertEquals(1, graph.size());
    }

    @Test(expected=RiotException.class)
    public void errorHandler() {
        Graph graph = GraphFactory.createGraphMem();
        // This test file contains Turtle.
        RDFParser.create().source(DIR+"data.rdf")
            // and no test log output.
            .errorHandler(ErrorHandlerFactory.errorHandlerNoLogging)
            .parse(graph);
    }

    @Test
    public void source_uri_force_lang() {
        Graph graph = GraphFactory.createGraphMem();
        RDFParser.create().source("file:"+DIR+"data.rdf").forceLang(Lang.TTL).parse(graph);
        assertEquals(3, graph.size());
    }

    @Test
    public void source_streamManager() {
        StreamManager sMgr = new StreamManager();
        sMgr.addLocator(new LocatorFile(DIR)) ;
        Graph graph = GraphFactory.createGraphMem();
        RDFParser.create().streamManager(sMgr).source("file:data.rdf").forceLang(Lang.TTL).parse(graph);
        assertEquals(3, graph.size());
    }

    private static class TestingFactoryRDF extends FactoryRDFStd {
        int counter = 0;
        @Override
        public Node createURI(String uriStr) {
            counter++;
            return super.createURI(uriStr);
        }
    }

    private RDFParserBuilder builder() {
        InputStream input = new ByteArrayInputStream(testdata.getBytes(StandardCharsets.UTF_8));
        return RDFParserBuilder.create().lang(Lang.TTL).source(input);
    }

    @Test public void labels_01() {
        Graph graph = GraphFactory.createGraphMem();
        //LabelToNode.createUseLabelEncoded() ;

        builder()
            .labelToNode(LabelToNode.createUseLabelAsGiven())
            .parse(graph);
        assertEquals(1, graph.size());
        StringWriter sw = new StringWriter();
        RDFDataMgr.write(sw, graph, Lang.NT);
        String s = sw.toString();
        assertTrue(s.contains("_:Bb"));
    }

    @Test public void factory_01() {
        TestingFactoryRDF f = new TestingFactoryRDF();
        Graph graph = GraphFactory.createGraphMem();
        builder()
            .factory(f)
            .parse(graph);
        assertEquals(1, graph.size());
        assertNotEquals(0, f.counter);
    }

    // Canonical literals.

    @Test public void canonical_value_1() {
        testNormalization("0123", "0123", builder().canonicalValues(false));
    }

    @Test public void canonical_value_2() {
        testNormalization("+123", "123", builder().canonicalValues(true));
    }

    @Test public void canonical_value_3() {
        testNormalization("+123.00", "123.0", builder().canonicalValues(true));
    }

    @Test public void canonical_value_4() {
        testNormalization("+123.00e0", "1.23E2", builder().canonicalValues(true));
    }

    @Test public void canonical_langTag_1() {
        testNormalization("'abc'@En-gB", "'abc'@En-gB", builder().langTagAsGiven());
    }

    @Test public void canonical_langTag_2() {
        testNormalization("'abc'@En-gB", "'abc'@en-gb", builder().langTagLowerCase());
    }

    @Test public void canonical_langTag_3() {
        testNormalization("'abc'@En-gB", "'abc'@en-GB", builder().langTagCanonical());
    }

    @Test
    public void parser_fragment() {
        PrefixMap pmap = PrefixMapFactory.create(Map.of("", "http://example/"));
        Graph g = RDFParser.fromString("<s> :p :o .", Lang.TTL)
                .prefixes(pmap)
                .base("http://base/")
                .toGraph();
        assertFalse(g.isEmpty());
        Graph g2 = GraphFactory.createDefaultGraph();
        g2.add(NodeFactory.createURI("http://base/s"),
               NodeFactory.createURI("http://example/p"),
               NodeFactory.createURI("http://example/o"));
        assertTrue(g2.isIsomorphicWith(g));
    }

    private static String PREFIX = "PREFIX : <http://example/>\n ";
    private static Node s = SSE.parseNode(":s");
    private static Node p = SSE.parseNode(":p");

    private void testNormalization(String input, String output, RDFParserBuilder builder) {
        Graph graph = GraphFactory.createGraphMem();
        String x = PREFIX+":s :p "+input;
        builder.source(new StringReader(x)).parse(graph);
        assertEquals(1, graph.size());
        Node objExpected = SSE.parseNode(output);
        Node objObtained = graph.find(s, p, null).next().getObject();
        assertEquals(objExpected, objObtained);
    }
}
