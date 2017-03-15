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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.RiotNotFoundException;
import org.apache.jena.riot.lang.LabelToNode;
import org.apache.jena.riot.system.ErrorHandlerFactory;
import org.apache.jena.riot.system.FactoryRDFStd;
import org.apache.jena.sparql.graph.GraphFactory;
import org.junit.Test;

public class TestRDFParser {
    
    // Location of test files.
    private static String DIR = "testing/RIOT/Parser/";
    private static String testdata = "@prefix : <http://example/ns#> . :x :x _:b .";  
    
    @Test public void source_not_uri_01() {
        Graph graph = GraphFactory.createGraphMem();
        RDFParserBuilder.create().lang(Lang.TTL).source(new StringReader(testdata)).parse(graph);
        assertEquals(1, graph.size());
    }
    
    @Test public void source_not_uri_02() {
        Graph graph = GraphFactory.createGraphMem();
        InputStream input = new ByteArrayInputStream(testdata.getBytes(StandardCharsets.UTF_8));
        RDFParserBuilder.create().lang(Lang.TTL).source(input).parse(graph);
        assertEquals(1, graph.size());
    }
    
    @Test public void source_uri_01() {
        Graph graph = GraphFactory.createGraphMem();
        RDFParserBuilder.create().source("file:"+DIR+"data.ttl").parse(graph);
        assertEquals(3, graph.size());
    }

    @Test(expected=RiotException.class)
    public void source_uri_02() {
        Graph graph = GraphFactory.createGraphMem();
        RDFParserBuilder.create().source("file:"+DIR+"data.unknown").parse(graph);
    }

    @Test
    public void source_uri_03() {
        Graph graph = GraphFactory.createGraphMem();
        RDFParserBuilder.create().source("file:"+DIR+"data.unknown").lang(Lang.TTL).parse(graph);
        assertEquals(3, graph.size());
    }

    @Test
    public void source_uri_04() {
        Graph graph = GraphFactory.createGraphMem();
        RDFParserBuilder.create()
            .source(Paths.get(DIR+"data.ttl"))
            .parse(graph);
        assertEquals(3, graph.size());
    }

    @Test
    public void source_uri_05() {
        // Last source wins.
        Graph graph = GraphFactory.createGraphMem();
        RDFParserBuilder.create()
            .source("http://example/")
            .source(DIR+"data.ttl")
            .parse(graph);
        assertEquals(3, graph.size());
    }

    @Test(expected=RiotNotFoundException.class)
    public void source_notfound_1() {
        // Last source wins.
        Graph graph = GraphFactory.createGraphMem();
        RDFParserBuilder.create()
            .source(Paths.get(DIR+"data.nosuchfile.ttl"))
            .parse(graph);
        assertEquals(3, graph.size());
    }

    @Test(expected=RiotNotFoundException.class)
    public void source_notfound_2() {
        // Last source wins.
        Graph graph = GraphFactory.createGraphMem();
        RDFParserBuilder.create()
            .source(DIR+"data.nosuchfile.ttl")
            .parse(graph);
        assertEquals(3, graph.size());
    }

    @Test(expected=RiotException.class)
    public void source_uri_hint_lang() {
        Graph graph = GraphFactory.createGraphMem();
        RDFParserBuilder.create().source("file:data.rdf")
            .lang(Lang.RDFXML)
            .errorHandler(ErrorHandlerFactory.errorHandlerNoLogging)
            .parse(graph);
        assertEquals(3, graph.size());
    }

    @Test(expected=RiotException.class)
    public void errorHandler() {
        Graph graph = GraphFactory.createGraphMem();
        // This test file contains Turtle. 
        RDFParserBuilder.create().source(DIR+"data.rdf")
            // and no test log output.  
            .errorHandler(ErrorHandlerFactory.errorHandlerNoLogging)
            .parse(graph);
    }

    @Test
    public void source_uri_force_lang() {
        Graph graph = GraphFactory.createGraphMem();
        RDFParserBuilder.create().source("file:"+DIR+"data.rdf").forceLang(Lang.TTL).parse(graph);
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
}
