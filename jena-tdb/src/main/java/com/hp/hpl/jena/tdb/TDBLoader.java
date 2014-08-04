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

package com.hp.hpl.jena.tdb;

import java.io.InputStream ;
import java.util.ArrayList ;
import java.util.List ;

import org.slf4j.Logger ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.sparql.util.Timer ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.store.GraphTDB ;
import com.hp.hpl.jena.tdb.store.bulkloader.BulkLoader ;

/** Public interface to the loader functionality.
 * The bulk loader is not transactional. 
 */ 
public class TDBLoader
{
    /** Load the contents of URL into a dataset.  URL must name a quads format file (NQuads or TriG - NTriples is also accepted).
     *  To a triples format, use {@link #load(GraphTDB, String)}
     *  or {@link #load(DatasetGraphTDB, List, boolean)}
    */
    public static void load(DatasetGraphTDB dataset, String url)
    {
        load(dataset, url, false) ;
    }

    /** Load the contents of URL into a dataset.  URL must name a quads format file (NQuads or TriG - NTriples is also accepted).
     *  To a triples format, use {@link #load(GraphTDB, String, boolean)} 
     *  or {@link #load(DatasetGraphTDB, List, boolean)}
    */
    public static void load(DatasetGraphTDB dataset, String url, boolean showProgress)
    {
        load(dataset, asList(url), showProgress) ;
    }

    /** Load the contents of URL into a dataset.  URL must name a quads format file (NQuads or TriG - NTriples is also accepted).
     *  To load a triples format, use {@link #load(GraphTDB, List, boolean)} 
     *  or {@link #load(DatasetGraphTDB, List, boolean)} 
    */
    public static void load(DatasetGraphTDB dataset, List<String> urls)
    {
        load(dataset, urls, false) ;
    }
    
    /** Load the contents of URL into a dataset.  URL must name a quads format file (NQuads or TriG - NTriples is also accepted).
     *  To load a triples format, use {@link #load(GraphTDB, List, boolean)} 
     *  or {@link #load(DatasetGraphTDB, List, boolean)} 
    */
    public static void load(DatasetGraphTDB dataset, List<String> urls, boolean showProgress)
    {
        TDBLoader loader = new TDBLoader() ;
        loader.setShowProgress(showProgress) ;
        loader.loadDataset(dataset, urls) ;
    }
    
    /** Load the contents of URL into a dataset.  Input is NQUADS.
     *  To load a triples format, use {@link #loadGraph} 
    */
    public static void load(DatasetGraphTDB dataset, InputStream input, boolean showProgress)
    {
        TDBLoader loader = new TDBLoader() ;
        loader.setShowProgress(showProgress) ;
        // TODO Lang version
        loader.loadDataset(dataset, input) ;
    }
    
    /** Load the contents of URL into a graph */
    public static void load(GraphTDB graph, String url)
    {
        load(graph, url, false) ;
    }
    
    /** Load the contents of URL into a graph */
    public static void load(GraphTDB graph, String url, boolean showProgress)
    {
        load(graph, asList(url), showProgress) ;
    }

    /** Load the contents of URL into a graph */
    public static void load(GraphTDB graph, List<String> urls)
    {
        load(graph, urls, false) ;
    }
    
    /** Load the contents of URL into a graph */
    public static void load(GraphTDB graph, List<String> urls, boolean showProgress)
    {
        TDBLoader loader = new TDBLoader() ;
        loader.setShowProgress(showProgress) ;
        loader.loadGraph(graph, urls) ;
    }

    /** Load the contents of URL into a model - may not be as efficient as bulk loading into a TDB graph  */
    public static void loadModel(Model model, String url)
    {
        loadModel(model, url, false) ;
    }
    
    /** Load the contents of URL into a model - may not be as efficient as bulk loading into a TDB graph  */
    public static void loadModel(Model model, String url, boolean showProgress)
    {
        loadAnything(model, url, showProgress) ;
    }

    /** Load the contents of a list of URLs into a model - may not be as efficient as bulk loading into a TDB graph  */
    public static void loadModel(Model model, List<String> urls, boolean showProgress)
    {
        Timer timer = new Timer() ;
        timer.startTimer() ;

        for ( String s : urls )
        {
            if ( showProgress ) 
                System.out.printf("Load: %s\n", s) ;
            loadModel(model, s, showProgress) ;
        }
        
        long time = timer.endTimer() ;
        if ( showProgress )
            System.out.printf("Time for load: %.2fs\n", time/1000.0) ;
        model.close();
    }
    
    // ---- The class itself.
    
    private boolean showProgress = true ;
    private boolean generateStats = false ;
    private Logger loaderLog  = TDB.logLoader ;
    private boolean checking ;
    
    // XXX Context control block
    // Checker
    // Error handler
    // verbose flag?
    // logger
    // generateStats
    // checking flag
    
    
    // ---- The class itself.
    
    public TDBLoader() {}

    /** Load a graph from a URL - assumes URL names a triples format document*/
    public void loadGraph(GraphTDB graph, String url)
    {
        loadGraph(graph, asList(url)) ;
    }
    
    /** Load a graph from a list of URL - assumes the URLs name triples format documents */
    public void loadGraph(GraphTDB graph, List<String> urls)
    {
        loadGraph$(graph, urls, showProgress) ;
    }
    
    /** Load a graph from a list of URL - assumes the URLs name triples format documents */
    public void loadGraph(GraphTDB graph, InputStream in)
    {
        loadGraph$(graph, in, showProgress) ;
    }
    
    /** Load a dataset from a URL - assumes URL names a quads format */
    public void loadDataset(DatasetGraphTDB dataset, String url)
    {
        loadDataset(dataset, asList(url)) ;
    }
    
    /** Load a dataset from a list of URL - assumes the URLs name quads format documents */
    public void loadDataset(DatasetGraphTDB dataset, List<String> urls)
    {
        // Triples languages are quads languages so no test for quad-ness needed.
        loadDataset$(dataset, urls, showProgress) ;
    }
    
    /** Load a dataset from an input steram which must be in N-Quads form */
    public void loadDataset(DatasetGraphTDB dataset, InputStream input)
    {
        // Triples languages are quads languages so no test for quad-ness needed.
        loadDataset$(dataset, input, showProgress) ;
    }

    public boolean getChecking()  
    { return checking ; }
    
    public void setChecking(boolean checking)  
    { this.checking = checking ; }

    
    public boolean getShowProgress()  
    { return showProgress ; }

    public final void setShowProgress(boolean showProgress)
    { this.showProgress = showProgress ; }

    public final boolean getGenerateStats()
    { return generateStats ; }

    public final void setGenerateStats(boolean generateStats)
    { this.generateStats = generateStats ; }
    
//    public final Logger getLogger()
//    { return this.loaderLog ; }
//
//    public final void setLogger(Logger log)
//    { this.loaderLog = log ; }
    
    private static void loadGraph$(GraphTDB graph, List<String> urls, boolean showProgress) {
        if ( graph.getGraphName() == null )
            loadDefaultGraph$(graph.getDSG(), urls, showProgress) ;
        else
            loadNamedGraph$(graph.getDSG(), graph.getGraphName(), urls, showProgress) ;
    }

    // These are the basic operations for TDBLoader.

    private static void loadGraph$(GraphTDB graph, InputStream input, boolean showProgress) {
        if ( graph.getGraphName() == null )
            loadDefaultGraph$(graph.getDSG(), input, showProgress) ;
        else
            loadNamedGraph$(graph.getDSG(), graph.getGraphName(), input, showProgress) ;
    }

    private static void loadDefaultGraph$(DatasetGraphTDB dataset, List<String> urls, boolean showProgress) {
        BulkLoader.loadDefaultGraph(dataset, urls, showProgress) ;
    }

    private static void loadDefaultGraph$(DatasetGraphTDB dataset, InputStream input, boolean showProgress) {
        BulkLoader.loadDefaultGraph(dataset, input, showProgress) ;
    }

    private static void loadNamedGraph$(DatasetGraphTDB dataset, Node graphName, List<String> urls, boolean showProgress) {
        BulkLoader.loadNamedGraph(dataset, graphName, urls, showProgress) ;
    }

    private static void loadNamedGraph$(DatasetGraphTDB dataset, Node graphName, InputStream input, boolean showProgress) {
        BulkLoader.loadNamedGraph(dataset, graphName, input, showProgress) ;
    }

    private static void loadDataset$(DatasetGraphTDB dataset, List<String> urls, boolean showProgress) {
        BulkLoader.loadDataset(dataset, urls, showProgress) ;
    }

    private static void loadDataset$(DatasetGraphTDB dataset, InputStream input, boolean showProgress) {
        BulkLoader.loadDataset(dataset, input, showProgress) ;
    }

    /** Load any model, not necessarily efficiently. */
    private static void loadAnything(Model model, String url, boolean showProgress) {
        model.read(url) ;
    }

    private static List<String> asList(String string)
    {
        List<String> list = new ArrayList<>() ;
        list.add(string) ;
        return list ;
    }
}
