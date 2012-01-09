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

package org.openjena.riot.out;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.Test;
import org.openjena.atlas.json.JSON;
import org.openjena.atlas.json.JsonObject;
import org.openjena.atlas.junit.BaseTest;
import org.openjena.atlas.lib.Sink;
import org.openjena.riot.RiotReader;
import org.openjena.riot.lang.LangRIOT;
import org.openjena.riot.lang.SinkTriplesToGraph;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.graph.GraphFactory;
import com.hp.hpl.jena.sparql.sse.SSE;

public class TestOutputRDFJSON extends BaseTest
{

    @Test public void rdfjson_01()
    {
    	test ("(base <http://example/> (graph (<s> <p> 1)))") ;
    }

    @Test public void rdfjson_02()
    {
    	test ("(base <http://example/> (graph (<s> <p> 1)(<s> <p> 2)))") ;
    }

    private void test (String str) 
    {
        Graph g = SSE.parseGraph(str) ;
        ByteArrayOutputStream bout = serializeAsJSON(g) ;
        parseAsJSON(bout) ; // make sure valid JSON
        Graph g2 = parseAsRDFJSON(bout) ; 

        assertTrue(g.isIsomorphicWith(g2)) ;    	
    }

    private ByteArrayOutputStream serializeAsJSON (Graph graph) 
    {
        ByteArrayOutputStream bout = new ByteArrayOutputStream() ;
        RDFJSONWriter.write(bout, graph) ;
        return bout ;
    }

    private Graph parseAsRDFJSON (ByteArrayOutputStream bout) 
    {
        ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray()) ;
        Graph graph = GraphFactory.createGraphMem() ;
        Sink<Triple> sink = new SinkTriplesToGraph(graph) ; 
        LangRIOT parser = RiotReader.createParserRdfJson(bin, sink) ;
        parser.parse() ;
        return graph ;
    }

    private JsonObject parseAsJSON (ByteArrayOutputStream bout) 
    {
    	return JSON.parse(new ByteArrayInputStream(bout.toByteArray())) ;
    }

}
