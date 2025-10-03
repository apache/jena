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
package org.apache.jena.geosparql.implementation.index;

import org.apache.jena.atlas.lib.Cache;
import org.apache.jena.geosparql.configuration.GeoSPARQLConfig;
import org.apache.jena.geosparql.geo.topological.GenericPropertyFunction;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;

import java.util.Iterator;

import static org.apache.jena.geosparql.implementation.index.CacheConfiguration.MAP_EXPIRY_INTERVAL;
import static org.apache.jena.geosparql.implementation.index.CacheConfiguration.UNLIMITED_MAP;

/**
 *
 */
public class QueryRewriteIndex {

    private boolean indexActive;
    private Cache<Triple, Boolean> index;
    private static int MAP_SIZE_DEFAULT = UNLIMITED_MAP;
    private static long MAP_EXPIRY_INTERVAL_DEFAULT = MAP_EXPIRY_INTERVAL;

    public static final Symbol QUERY_REWRITE_INDEX_SYMBOL = Symbol.create("http://jena.apache.org/spatial#query-index");

    public QueryRewriteIndex() {
        this.indexActive = GeoSPARQLConfig.isQueryRewriteEnabled();
        this.index = CacheConfiguration.create(MAP_SIZE_DEFAULT, MAP_EXPIRY_INTERVAL_DEFAULT);
    }

    public QueryRewriteIndex(String queryRewriteLabel, int maxSize, long expiryInterval) {
        this.indexActive = true;
        this.index = CacheConfiguration.create(maxSize, expiryInterval);
    }

    /**
     *
     * @param subjectGeometryLiteral
     * @param predicate
     * @param objectGeometryLiteral
     * @param propertyFunction
     * @return Result of relation between subject and object.
     */
    public final Boolean test(Node subjectGeometryLiteral, Node predicate, Node objectGeometryLiteral, GenericPropertyFunction propertyFunction) {

        if (!subjectGeometryLiteral.isLiteral() || !objectGeometryLiteral.isLiteral()) {
            return false;
        }

        if (indexActive) {
            Triple key = Triple.create(subjectGeometryLiteral, predicate, objectGeometryLiteral);
            try {
                return index.get(key, k -> propertyFunction.testFilterFunction(subjectGeometryLiteral, objectGeometryLiteral));
            } catch (NullPointerException ex) {
                //Catch NullPointerException and fall through to default action.
            }
        }

        return propertyFunction.testFilterFunction(subjectGeometryLiteral, objectGeometryLiteral);
    }

    /**
     * Empty the index.
     */
    public final void clear() {
        index.clear();
    }

    /**
     *
     * @return True if index is active.
     */
    public boolean isIndexActive() {
        return indexActive;
    }

    /**
     * Converts the index to a model of asserted spatial relation statements.
     *
     * @return Model containing all true assertions.
     */
    public Model toModel() {
        Graph graph = GraphFactory.createDefaultGraph();
        for (Iterator<Triple> it = index.keys(); it.hasNext(); ) {
            Triple key = it.next();
            Boolean value = index.getIfPresent(key);
            if (value != null && value) {
                graph.add(key);
            }
        }
        return ModelFactory.createModelForGraph(graph);
    }

    /**
     * Sets whether the index is active.
     *
     * @param indexActive
     */
    public final void setActive(boolean indexActive) {
        this.indexActive = indexActive;
    }

    /**
     *
     * @return Number of items in the index.
     */
    public final long getIndexSize() {
        return index.size();
    }

    /**
     * Reset the index to the provided max size and expiry interval.<br>
     * All contents will be lost.
     *
     * @param maxSize Maximum size
     * @param expiryInterval Expiry interval
     */
    public void reset(int maxSize, long expiryInterval) {
        index = CacheConfiguration.create(maxSize, expiryInterval);
    }

    /**
     * Set the maximum default size of QueryRewriteIndexes. -1 for no limit, 0
     * for no storage.
     *
     * @param mapSizeDefault
     */
    public static final void setMaxSize(int mapSizeDefault) {
        QueryRewriteIndex.MAP_SIZE_DEFAULT = mapSizeDefault;
    }

    /**
     * Set the maximum default expiry interval in millisecond of
     * QueryRewriteIndexes. 0 for no expiry.
     *
     * @param mapExpiryIntervalDefault
     */
    public static final void setExpiry(long mapExpiryIntervalDefault) {
        QueryRewriteIndex.MAP_EXPIRY_INTERVAL_DEFAULT = mapExpiryIntervalDefault;
    }

    /**
     * Create QueryRewriteIndex using the default global settings.
     *
     * @return Query Rewrite Index using default global settings.
     */
    public static final QueryRewriteIndex createDefault() {
        return new QueryRewriteIndex();
    }

    /**
     * Prepare a Dataset with the default QueryRewriteIndex settings.
     *
     * @param dataset
     */
    public static final void prepare(Dataset dataset) {
        Context context = dataset.getContext();
        set(context, createDefault());
    }

    /**
     * Prepare a Dataset with the provided QueryRewriteIndex settings.
     *
     * @param dataset
     * @param queryRewriteLabel
     * @param maxSize
     * @param expiryInterval
     */
    public static final void prepare(Dataset dataset, String queryRewriteLabel, int maxSize, long expiryInterval) {
        Context context = dataset.getContext();
        set(context, new QueryRewriteIndex(queryRewriteLabel, maxSize, expiryInterval));
    }

    /**
     * Retrieve the QueryRewriteIndex from the Context.<br>
     * If no index has been setup then QueryRewriteIndex is created.
     *
     * @param execCxt
     * @return QueryRewriteIndex contained in the Context.
     */
    public static final QueryRewriteIndex getOrCreate(ExecutionContext execCxt) {
        Context context = execCxt.getContext();
        return getOrCreate(context);
    }

    /**
     * Retrieve the QueryRewriteIndex from the Dataset Context.<br>
     * If no index has been setup then QueryRewriteIndex is created.
     *
     * @param dataset
     * @return QueryRewriteIndex contained in the Context.
     */
    public static final QueryRewriteIndex getOrCreate(Dataset dataset) {
        Context context = dataset.getContext();
        return getOrCreate(context);
    }

    /**
     * Retrieve the QueryRewriteIndex from the Dataset Context.<br>
     * If no index has been setup then QueryRewriteIndex is created.
     *
     * @param context
     * @return QueryRewriteIndex contained in the Context.
     */
    public static final QueryRewriteIndex getOrCreate(Context context) {
        QueryRewriteIndex queryRewriteIndex = context.computeIfAbsent(QUERY_REWRITE_INDEX_SYMBOL, k -> createDefault());
        return queryRewriteIndex;
    }

    public static final QueryRewriteIndex get(Context context) {
        return (context == null) ? null : context.get(QUERY_REWRITE_INDEX_SYMBOL);
    }

    public static final Context set(Context context, QueryRewriteIndex queryRewriteIndex) {
        context.set(QUERY_REWRITE_INDEX_SYMBOL, queryRewriteIndex);
        return context;
    }

    /**
     * Wrap Model in a Dataset and include QueryRewriteIndex.
     *
     * @param model
     * @return Dataset with default Model and QueryRewriteIndex in Context.
     */
    public static final Dataset wrapModel(Model model) {
        Dataset dataset = DatasetFactory.createTxnMem();
        dataset.setDefaultModel(model);
        prepare(dataset);

        return dataset;
    }
}
