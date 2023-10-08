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

package org.apache.jena.tdb1;

import java.io.InputStream ;
import java.util.List ;

import org.apache.jena.atlas.lib.Timer ;
import org.apache.jena.graph.Node ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.riot.Lang;
import org.apache.jena.tdb1.store.DatasetGraphTDB;
import org.apache.jena.tdb1.store.GraphTDB;
import org.apache.jena.tdb1.store.bulkloader.BulkLoader;
import org.apache.jena.tdb1.sys.TDBInternal;

/** Public interface to the loader functionality.
 * The bulk loader is not transactional.
 */
public class TDB1Loader
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
        load(dataset, List.of(url), showProgress, true) ;
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
        TDB1Loader loader = new TDB1Loader() ;
        loader.setShowProgress(showProgress) ;
        loader.setGenerateStats(generateStats);
        loader.loadDataset(dataset, urls) ;
    }

    /** Load a dataset from an input stream which must be in N-Quads form */
    public static void load(DatasetGraphTDB dataset, InputStream input, Lang lang, boolean showProgress, boolean generateStats) {
        TDB1Loader loader = new TDB1Loader() ;
        loader.setShowProgress(showProgress) ;
        loader.setGenerateStats(generateStats) ;
        loader.loadDataset(dataset, input, lang) ;
    }

    /** Load the contents of URL into a graph */
    public static void load(GraphTDB graph, String url)
    {
        load(graph, url, false) ;
    }

    /** Load the contents of URL into a graph */
    public static void load(GraphTDB graph, String url, boolean showProgress)
    {
        load(graph, List.of(url), showProgress) ;
    }

    /** Load the contents of URL into a graph */
    public static void load(GraphTDB graph, List<String> urls)
    {
        load(graph, urls, false) ;
    }

    /** Load the contents of URL into a graph */
    public static void load(GraphTDB graph, List<String> urls, boolean showProgress)
    {
        TDB1Loader loader = new TDB1Loader() ;
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
    private boolean checking ;

    // ---- The class itself.

    public TDB1Loader() {}

    /** Load a graph from a URL - assumes URL names a triples format document */
    public void loadGraph(GraphTDB graph, String url) {
        loadGraph(graph, List.of(url));
    }

    /**
     * Load a graph from a list of URL - assumes the URLs name triples format
     * documents
     */
    public void loadGraph(GraphTDB graph, List<String> urls) {
        loadGraph$(graph, urls, showProgress, generateStats);
    }

    /**
     * Load a graph from a list of URL - assumes the URLs name triples format
     * documents
     */
    public void loadGraph(GraphTDB graph, InputStream in) {
        loadGraph$(graph, in, showProgress, generateStats);
    }

    /** Load a dataset from a URL - assumes URL names a quads format */
    public void loadDataset(DatasetGraphTDB dataset, String url) {
        loadDataset(dataset, List.of(url));
    }

    /**
     * Load a dataset from a list of URL - assumes the URLs name quads format
     * documents
     */
    public void loadDataset(DatasetGraphTDB dataset, List<String> urls) {
        // Triples languages are quads languages so no test for quad-ness needed.
        loadDataset$(dataset, urls, showProgress, generateStats);
    }

    /** Load a dataset from an input stream */
    public void loadDataset(DatasetGraphTDB dataset, InputStream input, Lang lang) {
        loadDataset$(dataset, input, lang, showProgress, generateStats);
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

    private static void loadGraph$(GraphTDB graph, List<String> urls, boolean showProgress, boolean collectStats) {
        if ( graph.getGraphName() == null )
            loadDefaultGraph$(graph.getDatasetGraphTDB(), urls, showProgress, collectStats) ;
        else
            loadNamedGraph$(graph.getDatasetGraphTDB(), graph.getGraphName(), urls, showProgress, collectStats) ;
    }

    // These are the basic operations for TDBLoader.

    private static void loadGraph$(GraphTDB graph, InputStream input, boolean showProgress, boolean collectStats) {

        DatasetGraphTDB dsgtdb = TDBInternal.getBaseDatasetGraphTDB(graph.getDatasetGraphTDB());

        if ( graph.getGraphName() == null )
            loadDefaultGraph$(dsgtdb, input, showProgress, collectStats) ;
        else
            loadNamedGraph$(dsgtdb, graph.getGraphName(), input, showProgress, collectStats) ;
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

    private static void loadDataset$(DatasetGraphTDB dataset, InputStream input, Lang lang, boolean showProgress, boolean collectStats) {
        BulkLoader.loadDataset(dataset, input, lang, showProgress, collectStats) ;
    }

    /** Load any model, not necessarily efficiently. */
    private static void loadAnything(Model model, String url, boolean showProgress) {
        model.read(url) ;
    }
}
