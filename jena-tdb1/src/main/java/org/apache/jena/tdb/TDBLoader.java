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
import java.util.List ;

import org.apache.jena.rdf.model.Model ;
import org.apache.jena.riot.Lang;
import org.apache.jena.tdb1.TDB1Loader;
import org.apache.jena.tdb1.store.DatasetGraphTDB;
import org.apache.jena.tdb1.store.GraphTDB;

/** Public interface to the loader functionality.
 * The bulk loader is not transactional.
 *
 * @deprecated Use {@link org.apache.jena.tdb1.TDB1Loader}
 */
@Deprecated
public class TDBLoader
{
    /** Load the contents of URL into a dataset.
     * @deprecated Use {@link TDB1Loader}
     */
    @Deprecated
    public static void load(DatasetGraphTDB dataset, String url) {
        load(dataset, url, false);
    }

    /**
     * Load the contents of URL into a dataset.
     * @deprecated Use {@link TDB1Loader}
     */
    @Deprecated
    public static void load(DatasetGraphTDB dataset, String url, boolean showProgress) {
        load(dataset, List.of(url), showProgress, true);
    }

    /**
     * Load the contents of URL into a dataset.
     * @deprecated Use {@link TDB1Loader}
     */
    @Deprecated
    public static void load(DatasetGraphTDB dataset, List<String> urls) {
        load(dataset, urls, false, true);
    }

    /**
     * Load the contents of URL into a dataset.
     * @deprecated Use {@link TDB1Loader}
     */
    @Deprecated
    public static void load(DatasetGraphTDB dataset, List<String> urls, boolean showProgress, boolean generateStats) {
        TDB1Loader.load(dataset, urls, showProgress, generateStats);
    }

    /** Load a dataset from an input stream which must be in N-Quads form
     * @deprecated Use {@link TDB1Loader}
     */
    @Deprecated
    public static void load(DatasetGraphTDB dataset, InputStream input, Lang lang, boolean showProgress, boolean generateStats) {
        TDB1Loader.load(dataset, input, lang, showProgress, generateStats);
    }

    /** Load the contents of URL into a graph
     * @deprecated Use {@link TDB1Loader}
     */
    @Deprecated
    public static void load(GraphTDB graph, String url) {
        load(graph, url, false);
    }

    /** Load the contents of URL into a graph
     * @deprecated Use {@link TDB1Loader}
     */
    @Deprecated
    public static void load(GraphTDB graph, String url, boolean showProgress) {
        load(graph, List.of(url), showProgress);
    }

    /** Load the contents of URL into a graph
     * @deprecated Use {@link TDB1Loader}
     */
    @Deprecated
    public static void load(GraphTDB graph, List<String> urls) {
        load(graph, urls, false);
    }

    /** Load the contents of URL into a graph */
    @Deprecated
    public static void load(GraphTDB graph, List<String> urls, boolean showProgress) {
        TDB1Loader.load(graph, urls, showProgress);
    }

    /**
     * Load the contents of URL into a model - may not be as efficient as bulk
     * loading into a TDB graph
     * @deprecated Use {@link TDB1Loader}
     */
    @Deprecated
    public static void loadModel(Model model, String url) {
        TDB1Loader.loadModel(model, url);
    }

    /**
     * Load the contents of URL into a model - may not be as efficient as bulk
     * loading into a TDB graph
     * @deprecated Use {@link TDB1Loader}
     */
    @Deprecated
    public static void loadModel(Model model, String url, boolean showProgress) {
        TDB1Loader.loadModel(model, url, showProgress);
    }

    /**
     * Load the contents of a list of URLs into a model - may not be as efficient as
     * bulk loading into a TDB graph
     * @deprecated Use {@link TDB1Loader}
     */
    @Deprecated
    public static void loadModel(Model model, List<String> urls, boolean showProgress) {
        TDB1Loader.loadModel(model, urls, showProgress);
    }

    // ---- The class itself.

    // ---- The class itself.

    private final TDB1Loader tdb1loader;
    @Deprecated
    public TDBLoader() {
        this.tdb1loader = new TDB1Loader();
    }

    /** Load a graph from a URL - assumes URL names a triples format document
     * @deprecated Use {@link TDB1Loader}
     */
    @Deprecated
    public void loadGraph(GraphTDB graph, String url) {
        tdb1loader.loadGraph(graph, List.of(url));
    }

    /**
     * Load a graph from a list of URL - assumes the URLs name triples format
     * documents
     * @deprecated Use {@link TDB1Loader}
     */
    @Deprecated
    public void loadGraph(GraphTDB graph, List<String> urls) {
        tdb1loader.loadGraph(graph, urls);
    }

    /**
     * Load a graph from a list of URL - assumes the URLs name triples format
     * documents
     */
    @Deprecated
    public void loadGraph(GraphTDB graph, InputStream in) {
        tdb1loader.loadGraph(graph, in);
    }

    /** Load a dataset from a URL - assumes URL names a quads format
     * @deprecated Use {@link TDB1Loader}
     */
    @Deprecated
    public void loadDataset(DatasetGraphTDB dataset, String url) {
        tdb1loader.loadDataset(dataset, List.of(url));
    }

    /**
     * Load a dataset from a list of URL - assumes the URLs name quads format
     * documents
     * @deprecated Use {@link TDB1Loader}
     */
    @Deprecated
    public void loadDataset(DatasetGraphTDB dataset, List<String> urls) {
        // Triples languages are quads languages so no test for quad-ness needed.
        tdb1loader.loadDataset(dataset, urls);
    }

    /** Load a dataset from an input stream
     * @deprecated Use {@link TDB1Loader}
     */
    @Deprecated
    public void loadDataset(DatasetGraphTDB dataset, InputStream input, Lang lang) {
        tdb1loader.loadDataset(dataset, input, lang);
    }

    /** @deprecated Use {@link TDB1Loader} */
    @Deprecated
    public boolean getChecking() {
        return tdb1loader.getChecking();
    }

    /** @deprecated Use {@link TDB1Loader} */
    @Deprecated
    public void setChecking(boolean checking) {
        tdb1loader.setChecking(checking);
    }

    /** @deprecated Use {@link TDB1Loader} */
    @Deprecated
    public boolean getShowProgress() {
        return tdb1loader.getShowProgress();
    }

    /** @deprecated Use {@link TDB1Loader} */
    @Deprecated
    public void setShowProgress(boolean showProgress) {
        tdb1loader.setChecking(showProgress);
    }

    /** @deprecated Use {@link TDB1Loader} */
    @Deprecated
    public boolean getGenerateStats() {
        return tdb1loader.getGenerateStats();
    }

    /** @deprecated Use {@link TDB1Loader} */
    @Deprecated
    public void setGenerateStats(boolean generateStats) {
        tdb1loader.setGenerateStats(generateStats);
    }
}
