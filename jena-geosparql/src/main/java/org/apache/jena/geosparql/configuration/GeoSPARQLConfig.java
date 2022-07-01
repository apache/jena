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
package org.apache.jena.geosparql.configuration;

import java.io.File;
import org.apache.jena.geosparql.geof.topological.RelateFF;
import org.apache.jena.geosparql.implementation.datatype.GeometryDatatype;
import org.apache.jena.geosparql.implementation.function_registration.*;
import org.apache.jena.geosparql.implementation.index.IndexConfiguration;
import org.apache.jena.geosparql.implementation.index.IndexConfiguration.IndexOption;
import org.apache.jena.geosparql.implementation.index.QueryRewriteIndex;
import org.apache.jena.geosparql.implementation.registry.SRSRegistry;
import org.apache.jena.geosparql.implementation.vocabulary.Geo;
import org.apache.jena.geosparql.spatial.SpatialIndex;
import org.apache.jena.geosparql.spatial.SpatialIndexException;
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
     * Precision of calculations. Inaccuracies exist in these calculations and a
     * higher value may not improve results.
     */
    public static int DECIMAL_PLACES_PRECISION = 6;

    /**
     * Precision of created coordinates. Determines the coordinates of certain
     * JTS geometry calculations, e.g. buffer.
     */
    public static int PRECISION_MODEL_SCALE_FACTOR = 1000000;

    /**
     * Option to dynamically transform GeometryLiteral SRS in calculations.
     */
    public static boolean ALLOW_GEOMETRY_SRS_TRANSFORMATION = true;

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

        loadFunctions();
    }

    public static final void loadFunctions() {
        //Only register functions once.
        if (!IS_FUNCTIONS_REGISTERED) {
            // loading is actually idempotent.
            IS_FUNCTIONS_REGISTERED = true;

            /*
             * If jul-to-slf4j SLF4JBridgeHandler has not been setup,
             * then this can generate a warning via java.util.logging.
             *
             *   Dec 28, 2021 10:56:27 AM org.apache.sis.referencing.factory.sql.EPSGFactory <init>
             *   WARNING: The “SIS_DATA” environment variable is not set.
             */
            //Setup Default Coordinate Reference Systems
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
     * @throws SpatialIndexException
     */
    public static final void setupSpatialIndex(Dataset dataset) throws SpatialIndexException {
        SpatialIndex.buildSpatialIndex(dataset);
    }

    /**
     * Setup the precomputed Spatial Index using Dataset Dataset.<br>
     * We assume that the spatial index was computed before and written to the given file.
     *
     * @param dataset the dataset
     * @param spatialIndexFile the file containing the serialized spatial index
     * @throws SpatialIndexException
     */
    public static final void setupPrecomputedSpatialIndex(Dataset dataset, File spatialIndexFile) throws SpatialIndexException {
        SpatialIndex si = SpatialIndex.load(spatialIndexFile);
        SpatialIndex.setSpatialIndex(dataset, si);
    }

    /**
     * Setup Spatial Index using Dataset and most frequent SRS URI in
     * Dataset.<br>
     * Spatial Index written to file once created.
     *
     * @param dataset
     * @param spatialIndexFile
     * @throws SpatialIndexException
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
     * @throws SpatialIndexException
     */
    public static final void setupSpatialIndex(Dataset dataset, String srsURI, File spatialIndexFile) throws SpatialIndexException {
        SpatialIndex.buildSpatialIndex(dataset, srsURI, spatialIndexFile);
    }

    /**
     * Set the number of decimal places precision used in calculations and
     * coordinate transformations. Inaccuracies exist in these calculations and
     * a higher value may not improve results.
     *
     * @param decimalPlaces
     */
    public static final void setCalculationPrecision(int decimalPlaces) {
        DECIMAL_PLACES_PRECISION = decimalPlaces;
    }

    /**
     * Set the scale factor of the precision model used to create coordinate
     * sequences used in Geometries. Default: 1000000 for 6 d.p. precision.
     *
     * @param scaleFactor
     */
    public static final void setPrecisionModelScaleFactor(int scaleFactor) {
        PRECISION_MODEL_SCALE_FACTOR = scaleFactor;
    }

    /**
     * Sets whether transformation for mismatching Geometry SRS is allowed.
     *
     * @param allowTransformation
     */
    public static final void allowGeometrySRSTransformation(boolean allowTransformation) {
        ALLOW_GEOMETRY_SRS_TRANSFORMATION = allowTransformation;
    }

}
