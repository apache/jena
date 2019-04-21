/*
 * Copyright 2018 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.geosparql.configuration;

import org.apache.jena.geosparql.geof.topological.RelateFF;
import org.apache.jena.geosparql.implementation.datatype.GeometryDatatype;
import org.apache.jena.geosparql.implementation.function_registration.Egenhofer;
import org.apache.jena.geosparql.implementation.function_registration.GeometryProperty;
import org.apache.jena.geosparql.implementation.function_registration.NonTopological;
import org.apache.jena.geosparql.implementation.function_registration.RCC8;
import org.apache.jena.geosparql.implementation.function_registration.Relate;
import org.apache.jena.geosparql.implementation.function_registration.SimpleFeatures;
import org.apache.jena.geosparql.implementation.function_registration.Spatial;
import org.apache.jena.geosparql.implementation.index.IndexConfiguration;
import org.apache.jena.geosparql.implementation.index.IndexConfiguration.IndexOption;
import org.apache.jena.geosparql.implementation.index.QueryRewriteIndex;
import org.apache.jena.geosparql.implementation.registry.SRSRegistry;
import org.apache.jena.geosparql.implementation.vocabulary.Geo;
import org.apache.jena.geosparql.spatial.SpatialIndex;
import org.apache.jena.geosparql.spatial.SpatialIndexException;
import java.io.File;
import org.apache.jena.query.Dataset;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;

/**
 *
 *
 */
public class GeoSPARQLConfig {

    /**
     * GeoSPARQL schema
     */
    private static Boolean IS_FUNCTIONS_REGISTERED = false;
    private static Boolean IS_QUERY_REWRITE_ENABLED = true;

    /**
     * Initialise all GeoSPARQL property and filter functions with memory
     * indexing.
     * <br>Use this for in-memory indexing GeoSPARQL setup. Query re-write
     * enabled.
     * <br>This does not affect the use of Spatial Indexes for Datasets.
     *
     */
    public static final void setupMemoryIndex() {
        setup(IndexOption.MEMORY, true);
    }

    /**
     * Initialise all GeoSPARQL property and filter functions with memory
     * indexing.
     * <br>Use this for in-memory indexing GeoSPARQL setup. Query re-write
     * optional.
     * <br>This does not affect the use of Spatial Indexes for Datasets.
     *
     * @param isQueryRewriteEnabled
     */
    public static final void setupMemoryIndex(Boolean isQueryRewriteEnabled) {
        setup(IndexOption.MEMORY, isQueryRewriteEnabled);
    }

    /**
     * Initialise all GeoSPARQL property and filter functions with memory
     * indexing.
     * <br>Use this for in-memory indexing GeoSPARQL setup and to control the
     * index sizes. Expiry is defaulted to 5,000 milliseconds.
     * <br>This does not affect the use of Spatial Indexes for Datasets.
     *
     * @param geometryLiteralIndex
     * @param geometryTransformIndex
     * @param queryRewriteIndex
     */
    public static final void setupMemoryIndexSize(Integer geometryLiteralIndex, Integer geometryTransformIndex, Integer queryRewriteIndex) {
        setup(IndexOption.MEMORY, true);
        IndexConfiguration.setIndexMaxSize(geometryLiteralIndex, geometryTransformIndex, queryRewriteIndex);
    }

    /**
     * Initialise all GeoSPARQL property and filter functions with memory
     * indexing.
     * <br>Use this for in-memory indexing GeoSPARQL setup and to control the
     * index expiry rate (milliseconds). Size is defaulted to unlimited.
     * <br>This does not affect the use of Spatial Indexes for Datasets.
     *
     * @param geometryLiteralIndex
     * @param geometryTransformIndex
     * @param queryRewriteIndex
     */
    public static final void setupMemoryIndexExpiry(Long geometryLiteralIndex, Long geometryTransformIndex, Long queryRewriteIndex) {
        setup(IndexOption.MEMORY, true);
        IndexConfiguration.setIndexExpiry(geometryLiteralIndex, geometryTransformIndex, queryRewriteIndex);
    }

    /**
     * Initialise all GeoSPARQL property and filter functions with memory
     * indexing.
     * <br>Use this for in-memory indexing GeoSPARQL setup and to control the
     * index sizes (default: unlimited) and expiry rate (default: 5,000
     * milliseconds).
     * <br>This does not affect the use of Spatial Indexes for Datasets.
     *
     * @param geometryLiteralIndex
     * @param geometryTransformIndex
     * @param queryRewriteIndex
     * @param geometryLiteralIndexExpiry
     * @param geometryTransformIndexExpiry
     * @param queryRewriteIndexExpiry
     * @param isQueryRewriteEnabled
     */
    public static final void setupMemoryIndex(Integer geometryLiteralIndex, Integer geometryTransformIndex, Integer queryRewriteIndex, Long geometryLiteralIndexExpiry, Long geometryTransformIndexExpiry, Long queryRewriteIndexExpiry, Boolean isQueryRewriteEnabled) {
        setup(IndexOption.MEMORY, isQueryRewriteEnabled);
        IndexConfiguration.setIndexMaxSize(geometryLiteralIndex, geometryTransformIndex, queryRewriteIndex);
        IndexConfiguration.setIndexExpiry(geometryLiteralIndexExpiry, geometryTransformIndexExpiry, queryRewriteIndexExpiry);
    }

    /**
     * Initialise all GeoSPARQL property and filter functions with no indexing.
     * <br>Use this for no indexing GeoSPARQL setup.
     * <br>This does not affect the use of Spatial Indexes for Datasets.
     */
    public static final void setupNoIndex() {
        setup(IndexOption.NONE, true);
    }

    /**
     * Initialise all GeoSPARQL property and filter functions with no indexing.
     * <br>Use this for no indexing GeoSPARQL setup.
     * <br>This does not affect the use of Spatial Indexes for Datasets.
     *
     * @param isQueryRewriteEnabled
     */
    public static final void setupNoIndex(Boolean isQueryRewriteEnabled) {
        setup(IndexOption.NONE, isQueryRewriteEnabled);
    }

    /**
     * Initialise all GeoSPARQL property and filter functions. Query rewrite
     * enabled.
     * <br>This does not affect the use of Spatial Indexes for Datasets.
     *
     * @param indexOption
     */
    public static final void setup(IndexOption indexOption) {
        setup(indexOption, true);
    }

    /**
     * Initialise all GeoSPARQL property and filter functions.
     * <br>This does not affect the use of Spatial Indexes for Datasets.
     *
     * @param indexOption
     * @param isQueryRewriteEnabled
     */
    public static final void setup(IndexOption indexOption, Boolean isQueryRewriteEnabled) {

        IS_QUERY_REWRITE_ENABLED = isQueryRewriteEnabled;

        //Set the configuration for indexing.
        IndexConfiguration.setConfig(indexOption);

        //Only register functions once.
        if (!IS_FUNCTIONS_REGISTERED) {

            //Setup Default Cordinate Reference Systems
            SRSRegistry.setupDefaultSRS();

            //Register GeometryDatatypes with the TypeMapper.
            GeometryDatatype.registerDatatypes();

            //Register Property and Filter functions.
            PropertyFunctionRegistry propertyRegistry = PropertyFunctionRegistry.get();
            FunctionRegistry functionRegistry = FunctionRegistry.get();
            NonTopological.loadFilterFunctions(functionRegistry);
            functionRegistry.put(Geo.RELATE_NAME, RelateFF.class);
            SimpleFeatures.loadPropertyFunctions(propertyRegistry);
            SimpleFeatures.loadFilterFunctions(functionRegistry);
            Egenhofer.loadPropertyFunctions(propertyRegistry);
            Egenhofer.loadFilterFunctions(functionRegistry);
            RCC8.loadPropertyFunctions(propertyRegistry);
            RCC8.loadFilterFunctions(functionRegistry);
            Relate.loadRelateFunction(functionRegistry);
            GeometryProperty.loadPropertyFunctions(propertyRegistry);
            GeometryProperty.loadFilterFunctions(functionRegistry);
            Spatial.loadPropertyFunctions(propertyRegistry);
            Spatial.loadFilterFunctions(functionRegistry);
            IS_FUNCTIONS_REGISTERED = true;
        }
    }

    /**
     *
     * @return True, if the GeoSPARQL functions have been registered.
     */
    public static final Boolean isFunctionRegistered() {
        return IS_FUNCTIONS_REGISTERED;
    }

    /**
     * Empty all indexes and registries currently in use.
     * <br>This does not affect Spatial Indexes for Datasets.
     */
    public static final void reset() {
        //Convenience method so that setup and clearing in one class.
        IndexConfiguration.resetIndexesAndRegistries();
    }

    /**
     *
     * @return True if query rewrite enabled.
     */
    public static Boolean isQueryRewriteEnabled() {
        return IS_QUERY_REWRITE_ENABLED;
    }

    /**
     * Setup Query Rewrite Index using Dataset.<br>
     * The index will be set active.
     *
     * @param dataset
     * @param queryRewriteLabel
     * @param maxSize
     * @param expiryInterval
     */
    public static final void setupQueryRewriteIndex(Dataset dataset, String queryRewriteLabel, int maxSize, long expiryInterval) {
        QueryRewriteIndex.prepare(dataset, queryRewriteLabel, maxSize, expiryInterval);
    }

    /**
     * Setup Spatial Index using Dataset and most frequent SRS URI in Dataset.
     *
     * @param dataset
     */
    public static final void setupSpatialIndex(Dataset dataset) throws SpatialIndexException {
        SpatialIndex.buildSpatialIndex(dataset);
    }

    /**
     * Setup Spatial Index using Dataset and most frequent SRS URI in
     * Dataset.<br>
     * Spatial Index written to file once created.
     *
     * @param dataset
     * @param spatialIndexFile
     */
    public static final void setupSpatialIndex(Dataset dataset, File spatialIndexFile) throws SpatialIndexException {
        SpatialIndex.buildSpatialIndex(dataset, spatialIndexFile);
    }

    /**
     * Setup Spatial Index using Dataset using provided SRS URI.<br>
     * Spatial Index written to file once created.
     *
     * @param dataset
     * @param srsURI
     * @param spatialIndexFile
     */
    public static final void setupSpatialIndex(Dataset dataset, String srsURI, File spatialIndexFile) throws SpatialIndexException {
        SpatialIndex.buildSpatialIndex(dataset, srsURI, spatialIndexFile);
    }

}
