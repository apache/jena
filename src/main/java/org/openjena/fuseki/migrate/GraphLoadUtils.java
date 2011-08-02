/**
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


package org.openjena.fuseki.migrate;

import java.io.InputStream;

import com.hp.hpl.jena.graph.Factory;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.shared.NotFoundException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFReader;

import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.FileUtils;

/** A packaging of code to do a controlled read of a graph or model */

public class GraphLoadUtils
{
    // ---- Model level
    
    public static Model readModel(String uri, int limit)
    {
        return readModel(uri, limit, FileUtils.guessLang(uri)) ;
    }

    public static Model readModel(String uri, int limit, String syntax) 
    {
        Graph g = Factory.createGraphMem() ;
        return readUtil(g, uri, limit, syntax) ;
    }
    
    public static void loadModel(Model model, String uri, int limit) 
    {
        loadModel(model, uri, limit, null) ;
    }

    public static void loadModel(Model model, String uri, int limit, String syntax) 
    {
        Graph g = model.getGraph() ;
        readUtil(g, uri, limit, syntax) ;
    }

    // ---- Graph level
    
    public static Graph readGraph(String uri, int limit)
    {
        return readGraph(uri, limit, FileUtils.guessLang(uri)) ;
    }
    
    public static Graph readGraph(String uri, int limit, String syntax) 
    {
        Graph g = Factory.createGraphMem() ;
        Model m = readUtil(g, uri, limit, syntax) ;
        return m.getGraph() ;
    }
    
    public static void loadGraph(Graph g, String uri, int limit) 
    {
        loadGraph(g, uri, limit, FileUtils.guessLang(uri)) ;
    }

    public static void loadGraph(Graph g, String uri, int limit, String syntax) 
    {
        Model m = readUtil(g, uri, limit, syntax) ;
    }
    
    
    private static Model readUtil(Graph graph, String uri, int limit, String syntax) 
    {
        // Use the mapped uri as the syntax hint.
        {
            String altURI = FileManager.get().mapURI(uri) ;
            if ( altURI != null )
                syntax = FileUtils.guessLang(uri) ;
        }
        // Temporary model wrapper 
        Graph g = new LimitingGraph(graph, limit) ;
        Model m = ModelFactory.createModelForGraph(g) ;
        
        // If it's RDF/XML, go via Jena and set the HTTP readers.
//        if ( FileUtils.langXML.equals(syntax) )
//        {
//            m.read(uri, uri) ;
//            return m ;
//        }
//        else
        {
            // Otherwise open raw and hope the syntax is right. 
            RDFReader r = m.getReader(syntax) ;
            r.setErrorHandler(new GraphErrorHandler()) ;
            InputStream in = FileManager.get().open(uri) ;
            if ( in == null )
                // Not found.
                throw new NotFoundException("Not found: "+uri) ;
            r.read(m, in, uri) ;
            return m ;
        }
    }
}
