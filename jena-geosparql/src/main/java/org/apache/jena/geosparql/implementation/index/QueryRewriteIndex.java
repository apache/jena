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

import io.github.galbiston.expiring_map.ExpiringMap;
import static io.github.galbiston.expiring_map.MapDefaultValues.MAP_EXPIRY_INTERVAL;
import static io.github.galbiston.expiring_map.MapDefaultValues.UNLIMITED_MAP;
import java.util.Map.Entry;
import org.apache.jena.geosparql.configuration.GeoSPARQLConfig;
import org.apache.jena.geosparql.geo.topological.GenericPropertyFunction;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;

/**
 *
 */
public class QueryRewriteIndex {

    private boolean indexActive;
    private final String queryRewriteLabel;
    private ExpiringMap<String, Boolean> index;
    private static String LABEL_DEFAULT = "Query Rewrite";
    private static int MAP_SIZE_DEFAULT = UNLIMITED_MAP;
    private static long MAP_EXPIRY_INTERVAL_DEFAULT = MAP_EXPIRY_INTERVAL;
    private static final String KEY_SEPARATOR = "@";

    public static final Symbol QUERY_REWRITE_INDEX_SYMBOL = Symbol.create("http://jena.apache.org/spatial#query-index");

    public QueryRewriteIndex() {
        this.queryRewriteLabel = LABEL_DEFAULT;
        this.indexActive = GeoSPARQLConfig.isQueryRewriteEnabled();
        this.index = new ExpiringMap<>(queryRewriteLabel, MAP_SIZE_DEFAULT, MAP_EXPIRY_INTERVAL_DEFAULT);
        if (indexActive) {
            index.startExpiry();
        }
    }

    public QueryRewriteIndex(String queryRewriteLabel, int maxSize, long expiryInterval) {
        this.queryRewriteLabel = queryRewriteLabel;
        this.indexActive = true;
        this.index = new ExpiringMap<>(queryRewriteLabel, maxSize, expiryInterval);
        this.index.startExpiry();
    }

    /**
     *
     * @param subjectGeometryLiteral
     * @param predicate
     * @param objectGeometryLiteral
     * @param propertyFunction
     * @return Result of relation between subject and object.
     */
    public final Boolean test(Node subjectGeometryLiteral, Property predicate, Node objectGeometryLiteral, GenericPropertyFunction propertyFunction) {

        if (!subjectGeometryLiteral.isLiteral() || !objectGeometryLiteral.isLiteral()) {
            return false;
        }

        if (indexActive) {
            String key = subjectGeometryLiteral.getLiteralLexicalForm() + KEY_SEPARATOR + predicate.getURI() + KEY_SEPARATOR + objectGeometryLiteral.getLiteralLexicalForm();
            try {
                Boolean result;
                if (index.containsKey(key)) {
                    result = index.get(key);
                } else {
                    result = propertyFunction.testFilterFunction(subjectGeometryLiteral, objectGeometryLiteral);
                    index.put(key, result);
                }
                return result;
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
     * Sets whether the Query Rewrite Index is active.
     * <br> The index will be empty after this process.
     *
     * @param maxSize : use -1 for unlimited size
     */
    public final void setMapSize(int maxSize) {
        index.setMaxSize(maxSize);
    }

    /**
     * Sets the expiry time in milliseconds of the Query Rewrite Index, if
     * active.
     *
     * @param expiryInterval : use 0 or negative for unlimited timeout
     */
    public final void setMapExpiry(long expiryInterval) {
        index.setExpiryInterval(expiryInterval);
    }

    /**
     *
     * @return True if index is active.
     */
    public boolean isIndexActive() {
        return indexActive;
    }

    /**
     * COnverts the index to a model of asserted spatial relation statements.
     *
     * @return Model containing all true assertions.
     */
    public Model toModel() {
        Model model = ModelFactory.createDefaultModel();
        for (Entry<String, Boolean> entry : index.entrySet()) {
            Boolean value = entry.getValue();
            if (value) {
                String[] parts = entry.getKey().split(KEY_SEPARATOR);
                Resource subject = ResourceFactory.createResource(parts[0]);
                Property property = ResourceFactory.createProperty(parts[1]);
                Resource object = ResourceFactory.createResource(parts[2]);
                model.add(subject, property, object);
            }
        }

        return model;
    }

    /**
     * Sets whether the index is active.
     *
     * @param indexActive
     */
    public final void setActive(boolean indexActive) {
        this.indexActive = indexActive;

        if (indexActive) {
            index.startExpiry();
        } else {
            index.stopExpiry();
        }
    }

    /**
     *
     * @return Number of items in the index.
     */
    public final long getIndexSize() {
        return index.mappingCount();
    }

    /**
     * Reset the index to the provided max size and expiry interval.<br>
     * All contents will be lost.
     *
     * @param maxSize
     * @param expiryInterval
     */
    public void reset(int maxSize, long expiryInterval) {
        index = new ExpiringMap<>(queryRewriteLabel, maxSize, expiryInterval);
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
        context.set(QUERY_REWRITE_INDEX_SYMBOL, createDefault());
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
        context.set(QUERY_REWRITE_INDEX_SYMBOL, new QueryRewriteIndex(queryRewriteLabel, maxSize, expiryInterval));
    }

    /**
     * Retrieve the QueryRewriteIndex from the Context.<br>
     * If no index has been setup then QueryRewriteIndex is created.
     *
     * @param execCxt
     * @return QueryRewriteIndex contained in the Context.
     */
    public static final QueryRewriteIndex retrieve(ExecutionContext execCxt) {

        Context context = execCxt.getContext();
        return retrieve(context);
    }

    /**
     * Retrieve the QueryRewriteIndex from the Dataset Context.<br>
     * If no index has been setup then QueryRewriteIndex is created.
     *
     * @param dataset
     * @return QueryRewriteIndex contained in the Context.
     */
    public static final QueryRewriteIndex retrieve(Dataset dataset) {

        Context context = dataset.getContext();
        return retrieve(context);
    }

    /**
     * Retrieve the QueryRewriteIndex from the Dataset Context.<br>
     * If no index has been setup then QueryRewriteIndex is created.
     *
     * @param context
     * @return QueryRewriteIndex contained in the Context.
     */
    public static final QueryRewriteIndex retrieve(Context context) {
        QueryRewriteIndex queryRewriteIndex = (QueryRewriteIndex) context.get(QUERY_REWRITE_INDEX_SYMBOL, null);

        if (queryRewriteIndex == null) {
            queryRewriteIndex = createDefault();
            context.set(QUERY_REWRITE_INDEX_SYMBOL, queryRewriteIndex);
        }

        return queryRewriteIndex;
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
