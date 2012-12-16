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


package org.apache.jena.fuseki.migrate;

import java.io.InputStream ;

import org.apache.jena.atlas.lib.Sink ;
import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.riot.lang.LangRIOT ;
import org.apache.jena.riot.lang.RDFParserOutput ;
import org.apache.jena.riot.lang.RDFParserOutputLib ;
import org.apache.jena.riot.lang.SinkTriplesToGraph ;
import org.openjena.riot.Lang ;
import org.openjena.riot.RiotException ;
import org.openjena.riot.RiotReader ;

import com.hp.hpl.jena.graph.Factory ;
import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;

/** A packaging of code to do a controlled read of a graph or model */

public class GraphLoadUtils
{
    // ---- Model level
    
    public static Model readModel(String uri, int limit)
    {
        Graph g = Factory.createGraphMem() ;
        readUtil(g, uri, limit) ;
        return ModelFactory.createModelForGraph(g) ;
    }
    
    public static void loadModel(Model model, String uri, int limit) 
    {
        Graph g = model.getGraph() ;
        readUtil(g, uri, limit) ;
    }

    // ---- Graph level
    
    public static Graph readGraph(String uri, int limit)
    {
        Graph g = Factory.createGraphMem() ;
        readUtil(g, uri, limit) ;
        return g ;
    }
    
    public static void loadGraph(Graph g, String uri, int limit) 
    {
        readUtil(g, uri, limit) ;
    }
    
    // ** Worker.
    private static void readUtil(Graph graph, String uri, int limit)
    {
        Lang lang = Lang.guess(uri, Lang.RDFXML) ;
        
        Sink<Triple> sink = new SinkTriplesToGraph(graph) ;
        sink = new SinkLimited<Triple>(sink, limit) ;
        
        // TODO Conneg - awaiting RIOT code upgrade.
        InputStream input = Fuseki.webFileManager.open(uri) ;
        
        RDFParserOutput dest = RDFParserOutputLib.sinkTriples(sink) ;
        LangRIOT parser = RiotReader.createParserTriples(input, lang, uri, dest) ;
        try {
            parser.parse() ;
        } catch (RiotException ex) { throw ex ; }
    }
}
