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

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class VocabGeoSPARQL {
    static String NS = "http://jena.apache.org/geosparql#";

    private static Property property(String shortName) {
        return ResourceFactory.createProperty(NS+shortName);
    }

    // The class of geosparql datasets
    public static final Resource tGeoDataset    = ResourceFactory.createResource(NS+"GeosparqlDataset");
    public static final Resource tGeoDatasetAlt = ResourceFactory.createResource(NS+"geosparqlDataset");

    // CLI: names = {"--inference", "-i"}
    // "Enable GeoSPARQL RDFS schema and inferencing (class and property hierarchy). Inferences will be applied to the dataset. Updates to dataset may require server restart."
    public static final Property pInference = property("inference");

    // CLI: names = {"--default_geometry", "-dg"}
    // "Apply hasDefaultGeometry to single Feature hasGeometry Geometry statements. Additional properties will be added to the dataset."
    public static final Property pApplyDefaultGeometry = property("applyDefaultGeometry");

//    // CLI: names = {"--validate", "-v"}
//    // "Validate that the Geometry Literals in the dataset are valid."
//    public static final Property pValidateGeometryLiteral = property("validateGeometryLiteral");

//    // CLI: names = {"--convert_geo", "-c"}
//    // "Convert Geo predicates in the data to Geometry with WKT WGS84 Point Geometry Literal."
//    public static final Property pConvertGeoPredicates = property("convertGeoPredicates");
//
//    //CLI: names = {"--remove_geo", "-rg"}
//    // "Remove Geo predicates in the data after converting to Geometry with WKT WGS84 Point Geometry Literal.";
//    public static final Property pRemoveGeoPredicates = property("removeGeoPredicates");

    // CLI: names = {"--rewrite", "-r"}
    // "Enable query rewrite."
    public static final Property pQueryRewrite = property("queryRewrite");

    // CLI: names = {"--index", "-x"}
    // "Indexing enabled."
    public static final Property pIndexEnabled = property("indexEnabled");

    // CLI: names = {"--index_sizes", "-xs"}
    // "List of Index item sizes: [Geometry Literal, Geometry Transform, Query Rewrite]. Unlimited: -1, Off: 0"
    public static final Property pIndexSizes = property("indexSizes");

    // CLI: names = {"--index_expiry", "-xe"}
    // "List of Index item expiry in milliseconds: [Geometry Literal, Geometry Transform, Query Rewrite]. Off: 0, Minimum: 1001"
    public static final Property pIndexExpiries = property("indexExpiries");

    // CLI: names = {"--spatial_index", "-si"}
    // "File to load or store the spatial index. Default to " + SPATIAL_INDEX_FILE + " in TDB folder if using TDB and not set. Otherwise spatial index is not stored.
    public static final Property pSpatialIndexFile = property("spatialIndexFile");

    // Whether to load/generate the spatial index at all (defaults to True). With this off, the value of spatialIndexFile will be ignored.
    public static final Property pSpatialIndexEnabled = property("spatialIndexEnabled");

    // Dataset
    public static final Property pDataset = property("dataset");
}
