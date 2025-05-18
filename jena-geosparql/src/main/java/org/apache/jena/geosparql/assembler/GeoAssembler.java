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

package org.apache.jena.geosparql.assembler;

import static org.apache.jena.geosparql.assembler.VocabGeoSPARQL.pApplyDefaultGeometry;
import static org.apache.jena.geosparql.assembler.VocabGeoSPARQL.pDataset;
import static org.apache.jena.geosparql.assembler.VocabGeoSPARQL.pIndexEnabled;
import static org.apache.jena.geosparql.assembler.VocabGeoSPARQL.pIndexExpiries;
import static org.apache.jena.geosparql.assembler.VocabGeoSPARQL.pIndexSizes;
import static org.apache.jena.geosparql.assembler.VocabGeoSPARQL.pInference;
import static org.apache.jena.geosparql.assembler.VocabGeoSPARQL.pQueryRewrite;
import static org.apache.jena.geosparql.assembler.VocabGeoSPARQL.pSpatialIndexFile;
import static org.apache.jena.geosparql.assembler.VocabGeoSPARQL.pSrsUri;
import static org.apache.jena.sparql.util.graph.GraphUtils.getBooleanValue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.geosparql.configuration.GeoSPARQLConfig;
import org.apache.jena.geosparql.configuration.GeoSPARQLOperations;
import org.apache.jena.geosparql.configuration.SrsException;
import org.apache.jena.geosparql.spatial.SpatialIndexConstants;
import org.apache.jena.geosparql.spatial.SpatialIndexException;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.assembler.DatasetAssembler;
import org.apache.jena.sparql.util.graph.GraphUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeoAssembler extends DatasetAssembler {

    private static Logger LOG = LoggerFactory.getLogger(GeoAssembler.class);

    @Override
    public DatasetGraph createDataset(Assembler a, Resource root) {

        // Base dataset.
        DatasetGraph base = super.createBaseDataset(root, pDataset);

        // GeoSPARQL RDFS inference. CLI: names = {"--inference", "-i"}
        boolean inference = true;
        if (root.hasProperty(pInference) )
            inference = getBooleanValue(root, pInference);

        // Apply default geometry to single Feature-Geometry. CLI: names = {"--default_geometry", "-dg"}
        boolean applyDefaultGeometry = false;
        if (root.hasProperty(pApplyDefaultGeometry) )
            applyDefaultGeometry = getBooleanValue(root, pApplyDefaultGeometry);

        // These can be deleted when it is confirmed by users that features are not used.
        // Note: jena-fuseki-geosparql seems not to have them.

//        // Validate geometry literals in the data. CLI: names = {"--validate", "-v"}
//        boolean validategeometryliteral = false;
//        if (root.hasProperty(pValidateGeometryLiteral) )
//            validateGeometryLiteral = getBooleanValue(root, pValidateGeometryLiteral);

        // Modifies dataset
//        // Convert Geo predicates in the data to Geometry with WKT WGS84 Point GeometryLiteral. CLI: names = {"--convert_geo", "-c"}
//        boolean convertGeoPredicates = false;
//        if (root.hasProperty(pConvertGeoPredicates) )
//            convertGeoPredicates = getBooleanValue(root, pConvertGeoPredicates);

        // Code returns modified dataset which is ignored so this is a no-op.
//        //Remove Geo predicates in the data after combining to Geometry. CLI: names = {"--remove_geo", "-rg"}
//        boolean removeGeoPredicates = false;
//        if (root.hasProperty(pRemoveGeoPredicates) )
//            removeGeoPredicates = getBooleanValue(root, pRemoveGeoPredicates);

        // Query Rewrite enabled. CLI: names = {"--rewrite", "-r"}
        boolean queryRewrite = true;
        if (root.hasProperty(pQueryRewrite) )
            queryRewrite = getBooleanValue(root, pQueryRewrite);

        // Indexing enabled. CLI: names = {"--index", "-x"}
        boolean indexEnabled = true;
        if (root.hasProperty(pIndexEnabled) )
            indexEnabled = getBooleanValue(root, pIndexEnabled);

        // Index sizes. names = {"--index_sizes", "-xs"}
        List<Integer> indexSizes = Arrays.asList(-1, -1, -1);
        // "List of Index item sizes: [Geometry Literal, Geometry Transform, Query Rewrite]. Unlimited: -1, Off: 0"
        if (root.hasProperty(pIndexSizes) )
            indexSizes = getListInteger(root, pIndexSizes, 3);

        //Index expiry. names = {"--index_expiry", "-xe"}
        List<Integer> indexExpiries = Arrays.asList(5000, 5000, 5000);
        // "List of Index item expiry in milliseconds: [Geometry Literal, Geometry Transform, Query Rewrite]. Off: 0, Minimum: 1001"
        if (root.hasProperty(pIndexExpiries) )
            indexExpiries = getListInteger(root, pIndexSizes, 3);

        // Spatial Index file. CLI: names = {"--spatial_index", "-si"}
        String spatialIndexFilename = null;
        // "File to load or store the spatial index. Default to " + SPATIAL_INDEX_FILE + " in TDB folder if using TDB and not set. Otherwise spatial index is not stored.
        if (root.hasProperty(pSpatialIndexFile) )
            spatialIndexFilename = GraphUtils.getStringValue(root, pSpatialIndexFile);

        if (spatialIndexFilename == null) {
            LOG.warn(root + ": No spatial index file location is specified via " + pSpatialIndexFile + ". Spatial index will not be persisted.");
        }

        // SRS URI
        String srsURI = null;
        if (root.hasProperty(pSrsUri) ) {
            srsURI = GraphUtils.getResourceValue(root, pSrsUri).getURI();
        }

        if (srsURI == null) {
            LOG.warn(root + ": No preferred SRS is configured via " + pSrsUri);
        }


        // ---- Build
        Dataset dataset = DatasetFactory.wrap(base);
        dataset.getContext().set(SpatialIndexConstants.symSrsUri, srsURI);

        // Conversion of data. Startup-only.
        // needed for w3c:geo/wgs84_pos#lat/log.
        // (names = {"--convert_geo", "-c"}
        // "Convert Geo predicates in the data to Geometry with WKT WGS84 Point Geometry Literal."

        // GeoSPARQLOperations.convertGeoPredicates returns the different (in-memory) dataset but which is ignored.
//        //Convert Geo predicates to Geometry Literals.
//        if ( convertGeoPredicates ) //Apply validation of Geometry Literal.
//            GeoSPARQLOperations.convertGeoPredicates(dataset, removeGeoPredicates);

        //Apply hasDefaultGeometry relations to single Feature hasGeometry Geometry.
        if (applyDefaultGeometry)
            GeoSPARQLOperations.applyDefaultGeometry(dataset);

        //Apply GeoSPARQL schema and RDFS inferencing to the dataset.
        if (inference)
            GeoSPARQLOperations.applyInferencing(dataset);

        //Setup GeoSPARQL
        if (indexEnabled) {
            GeoSPARQLConfig.setupMemoryIndex(indexSizes.get(0), indexSizes.get(1), indexSizes.get(2),
                    (long)indexExpiries.get(0), (long)indexExpiries.get(1), (long)indexExpiries.get(2),
                    queryRewrite);
        } else {
            GeoSPARQLConfig.setupNoIndex(queryRewrite);
        }

        prepareSpatialExtension(dataset, spatialIndexFilename);
        return base;
    }

    private static List<Integer> getListInteger(Resource r, Property p, int len) {
        String integers = GraphUtils.getStringValue(r, p);
        String[] values = integers.split(",");
        List<Integer> integerList = new ArrayList<>();
        for (String val : values) {
            val = val.trim();
            integerList.add(Integer.parseInt(val));
        }
        if ( len >= 0 && integerList.size() != len)
            throw new JenaException("Expected list of exactly "+len+" integers");
        return integerList;
    }

    private static void prepareSpatialExtension(Dataset dataset, String spatialIndex){
        boolean isEmpty = dataset.calculateRead(dataset::isEmpty);
        if ( isEmpty ) {
            LOG.warn("Dataset is empty. Constructing an empty spatial index that needs to be updated once data is added.");
        }

        try {
            // no file given, i.e. in-memory index only
            if ( spatialIndex == null ) {
                GeoSPARQLConfig.setupSpatialIndex(dataset);
                return;
            }

            // file given but empty -> compute and serialize index
            Path spatialIndexPath = Path.of(spatialIndex);
            if ( ! Files.exists(spatialIndexPath) || Files.size(spatialIndexPath) == 0 ) {
                GeoSPARQLConfig.setupSpatialIndex(dataset, spatialIndexPath);
                return;
            }

            // load and setup the precomputed index
            GeoSPARQLConfig.setupPrecomputedSpatialIndex(dataset, spatialIndexPath);
        }
        catch (SrsException ex) {
            // Data but no spatial data.
            if ( ! ex.getMessage().startsWith("No SRS found") )
                throw ex;
            LOG.warn(ex.getMessage(), ex);
        }
        catch (IOException ex) { IO.exception(ex); return; }
        catch (SpatialIndexException ex) {
            String msg = "Failed to create spatial index: "+ex.getMessage();
            LOG.error(msg, ex);
            throw new JenaException(msg, ex);
        }
    }
}
