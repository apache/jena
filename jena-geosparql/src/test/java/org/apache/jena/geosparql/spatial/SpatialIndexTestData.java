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
package org.apache.jena.geosparql.spatial;

import org.apache.jena.geosparql.spatial.SpatialIndexException;
import org.apache.jena.geosparql.spatial.SpatialIndex;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.SRSInfo;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.geosparql.implementation.vocabulary.Geo;
import org.apache.jena.geosparql.implementation.vocabulary.SRS_URI;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 *
 *
 */
public class SpatialIndexTestData {

    public static final Resource LONDON_FEATURE = ResourceFactory.createResource("http://example.org/Feature#London");
    public static final Resource LONDON_GEOMETRY = ResourceFactory.createResource("http://example.org/Geometry#London");
    public static final GeometryWrapper LONDON_GEOMETRY_WRAPPER = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(51.50853 -0.12574)", WKTDatatype.URI);

    public static final Resource NEW_YORK_FEATURE = ResourceFactory.createResource("http://example.org/Feature#NewYork");
    public static final Resource NEW_YORK_GEOMETRY = ResourceFactory.createResource("http://example.org/Geometry#NewYork");
    public static final GeometryWrapper NEW_YORK_GEOMETRY_WRAPPER = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(40.7127 -74.0059)", WKTDatatype.URI);

    public static final Resource HONOLULU_FEATURE = ResourceFactory.createResource("http://example.org/Feature#Honolulu");
    public static final Resource HONOLULU_GEOMETRY = ResourceFactory.createResource("http://example.org/Geometry#Honolulu");
    public static final GeometryWrapper HONOLULU_GEOMETRY_WRAPPER = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(21.3 -157.816667)", WKTDatatype.URI);

    public static final Resource PERTH_FEATURE = ResourceFactory.createResource("http://example.org/Feature#Perth");
    public static final Resource PERTH_GEOMETRY = ResourceFactory.createResource("http://example.org/Geometry#Perth");
    public static final GeometryWrapper PERTH_GEOMETRY_WRAPPER = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(-31.952222 115.858889)", WKTDatatype.URI);

    public static final Resource AUCKLAND_FEATURE = ResourceFactory.createResource("http://example.org/Feature#Auckland");
    public static final Resource AUCKLAND_GEOMETRY = ResourceFactory.createResource("http://example.org/Geometry#Auckland");
    public static final GeometryWrapper AUCKLAND_GEOMETRY_WRAPPER = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(-36.840556 174.74)", WKTDatatype.URI);

    public static final GeometryWrapper PARIS_GEOMETRY_WRAPPER = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(48.85341 2.34880)", WKTDatatype.URI);

    public static final SRSInfo WGS_84_SRS_INFO = new SRSInfo(SRS_URI.WGS84_CRS);
    public static final SRSInfo OSGB_SRS_INFO = new SRSInfo(SRS_URI.OSGB36_CRS);

    private static SpatialIndex TEST_SPATIAL_INDEX = null;
    private static Dataset TEST_DATASET = null;

    public static final SpatialIndex createTestIndex() {

        if (TEST_SPATIAL_INDEX == null) {
            try {
                SpatialIndex spatialIndex = new SpatialIndex(100, SRS_URI.WGS84_CRS);
                spatialIndex.insertItem(LONDON_GEOMETRY_WRAPPER.getEnvelope(), LONDON_FEATURE);
                spatialIndex.insertItem(NEW_YORK_GEOMETRY_WRAPPER.getEnvelope(), NEW_YORK_FEATURE);
                spatialIndex.insertItem(HONOLULU_GEOMETRY_WRAPPER.getEnvelope(), HONOLULU_FEATURE);
                spatialIndex.insertItem(PERTH_GEOMETRY_WRAPPER.getEnvelope(), PERTH_FEATURE);
                spatialIndex.insertItem(AUCKLAND_GEOMETRY_WRAPPER.getEnvelope(), AUCKLAND_FEATURE);

                spatialIndex.build();
                TEST_SPATIAL_INDEX = spatialIndex;
            } catch (SpatialIndexException ex) {
                System.out.println("Spatial Index Error: " + ex.getMessage());
            }
        }

        return TEST_SPATIAL_INDEX;
    }

    public static final Dataset createTestDataset() {

        if (TEST_DATASET == null) {
            Dataset dataset = DatasetFactory.createTxnMem();
            Model model = ModelFactory.createDefaultModel();
            model.add(LONDON_FEATURE, Geo.HAS_GEOMETRY_PROP, LONDON_GEOMETRY);
            model.add(NEW_YORK_FEATURE, Geo.HAS_GEOMETRY_PROP, NEW_YORK_GEOMETRY);
            model.add(HONOLULU_FEATURE, Geo.HAS_GEOMETRY_PROP, HONOLULU_GEOMETRY);
            model.add(PERTH_FEATURE, Geo.HAS_GEOMETRY_PROP, PERTH_GEOMETRY);
            model.add(AUCKLAND_FEATURE, Geo.HAS_GEOMETRY_PROP, AUCKLAND_GEOMETRY);
            model.add(LONDON_GEOMETRY, Geo.HAS_SERIALIZATION_PROP, LONDON_GEOMETRY_WRAPPER.asLiteral());
            model.add(NEW_YORK_GEOMETRY, Geo.HAS_SERIALIZATION_PROP, NEW_YORK_GEOMETRY_WRAPPER.asLiteral());
            model.add(HONOLULU_GEOMETRY, Geo.HAS_SERIALIZATION_PROP, HONOLULU_GEOMETRY_WRAPPER.asLiteral());
            model.add(PERTH_GEOMETRY, Geo.HAS_SERIALIZATION_PROP, PERTH_GEOMETRY_WRAPPER.asLiteral());
            model.add(AUCKLAND_GEOMETRY, Geo.HAS_SERIALIZATION_PROP, AUCKLAND_GEOMETRY_WRAPPER.asLiteral());

            dataset.setDefaultModel(model);
            SpatialIndex spatialIndex = createTestIndex();
            SpatialIndex.setSpatialIndex(dataset, spatialIndex);
            TEST_DATASET = dataset;
        }

        return TEST_DATASET;
    }

}
