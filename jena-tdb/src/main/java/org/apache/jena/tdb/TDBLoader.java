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

package org.apache.jena.tdb;

import java.io.InputStream ;
import java.util.ArrayList ;
import java.util.List ;

import org.apache.jena.atlas.lib.Timer ;
import org.apache.jena.graph.Node ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.tdb.store.DatasetGraphTDB ;
import org.apache.jena.tdb.store.GraphNonTxnTDB ;
import org.apache.jena.tdb.store.bulkloader.BulkLoader ;
import org.slf4j.Logger ;

/** Public interface to the loader functionality.
 * The bulk loader is not transactional. 
 */ 
public class TDBLoader
{
    /** Load the contents of URL into a dataset. */
    public static void load(DatasetGraphTDB dataset, String url)
    {
        load(dataset, url, false) ;
    }

    /**
     * Load the contents of URL into a dataset.
     */
    public static void load(DatasetGraphTDB dataset, String url, boolean showProgress)
    {
        load(dataset, asList(url), showProgress, true) ;
    }

    /**
     * Load the contents of URL into a dataset.
     */
    public static void load(DatasetGraphTDB dataset, List<String> urls)
    {
        load(dataset, urls, false, true) ;
    }
    
    /**
     * Load the contents of URL into a dataset.
     */
    public static void load(DatasetGraphTDB dataset, List<String> urls, boolean showProgress, boolean generateStats)
    {
        TDBLoader loader = new TDBLoader() ;
        loader.setShowProgress(showProgress) ;
        loader.setGenerateStats(generateStats);
        loader.loadDataset(dataset, urls) ;
    }
    
    /**
     *  Load the contents of URL into a dataset.  Input is N-Quads format.
     */
    public static void load(DatasetGraphTDB dataset, InputStream input, boolean showProgress)
    {
        TDBLoader loader = new TDBLoader() ;
        loader.setShowProgress(showProgress) ;
        // TODO Lang version
        loader.loadDataset(dataset, input) ;
    }
    
    /** Load the contents of URL into a graph */
    public static void load(GraphNonTxnTDB graph, String url)
    {
        load(graph, url, false) ;
    }
    
    /** Load the contents of URL into a graph */
    public static void load(GraphNonTxnTDB graph, String url, boolean showProgress)
    {
        load(graph, asList(url), showProgress) ;
    }

    /** Load the contents of URL into a graph */
    public static void load(GraphNonTxnTDB graph, List<String> urls)
    {
        load(graph, urls, false) ;
    }
    
    /** Load the contents of URL into a graph */
    public static void load(GraphNonTxnTDB graph, List<String> urls, boolean showProgress)
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
    private boolean generateStats = true ;
    private Logger loaderLog  = TDB.logLoader ;
    private boolean checking ;
    
    // ---- The class itself.
    
    public TDBLoader() {}

    /** Load a graph from a URL - assumes URL names a triples format document*/
    public void loadGraph(GraphNonTxnTDB graph, String url)
    {
        loadGraph(graph, asList(url)) ;
    }
    
    /** Load a graph from a list of URL - assumes the URLs name triples format documents */
    public void loadGraph(GraphNonTxnTDB graph, List<String> urls)
    {
        loadGraph$(graph, urls, showProgress, generateStats) ;
    }
    
    /** Load a graph from a list of URL - assumes the URLs name triples format documents */
    public void loadGraph(GraphNonTxnTDB graph, InputStream in)
    {
        loadGraph$(graph, in, showProgress, generateStats) ;
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
        loadDataset$(dataset, urls, showProgress, generateStats) ;
    }
    
    /** Load a dataset from an input stream which must be in N-Quads form */
    public void loadDataset(DatasetGraphTDB dataset, InputStream input)
    {
        // Triples languages are quads languages so no test for quad-ness needed.
        loadDataset$(dataset, input, showProgress, generateStats) ;
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
    
    private static void loadGraph$(GraphNonTxnTDB graph, List<String> urls, boolean showProgress, boolean collectStats) {
        if ( graph.getGraphName() == null )
            loadDefaultGraph$(graph.getDatasetGraphTDB(), urls, showProgress, collectStats) ;
        else
            loadNamedGraph$(graph.getDatasetGraphTDB(), graph.getGraphName(), urls, showProgress, collectStats) ;
    }

    // These are the basic operations for TDBLoader.

    private static void loadGraph$(GraphNonTxnTDB graph, InputStream input, boolean showProgress, boolean collectStats) {
        if ( graph.getGraphName() == null )
            loadDefaultGraph$(graph.getDatasetGraphTDB(), input, showProgress, collectStats) ;
        else
            loadNamedGraph$(graph.getDatasetGraphTDB(), graph.getGraphName(), input, showProgress, collectStats) ;
    }

    private static void loadDefaultGraph$(DatasetGraphTDB dataset, List<String> urls, boolean showProgress, boolean collectStats) {
        BulkLoader.loadDefaultGraph(dataset, urls, showProgress, collectStats) ;
    }

    private static void loadDefaultGraph$(DatasetGraphTDB dataset, InputStream input, boolean showProgress, boolean collectStats) {
        BulkLoader.loadDefaultGraph(dataset, input, showProgress, collectStats) ;
    }

    private static void loadNamedGraph$(DatasetGraphTDB dataset, Node graphName, List<String> urls, boolean showProgress, boolean collectStats) {
        BulkLoader.loadNamedGraph(dataset, graphName, urls, showProgress, collectStats) ;
    }

    private static void loadNamedGraph$(DatasetGraphTDB dataset, Node graphName, InputStream input, boolean showProgress, boolean collectStats) {
        BulkLoader.loadNamedGraph(dataset, graphName, input, showProgress, collectStats) ;
    }

    private static void loadDataset$(DatasetGraphTDB dataset, List<String> urls, boolean showProgress, boolean collectStats) {
        BulkLoader.loadDataset(dataset, urls, showProgress, collectStats) ;
    }

    private static void loadDataset$(DatasetGraphTDB dataset, InputStream input, boolean showProgress, boolean collectStats) {
        BulkLoader.loadDataset(dataset, input, showProgress, collectStats) ;
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
