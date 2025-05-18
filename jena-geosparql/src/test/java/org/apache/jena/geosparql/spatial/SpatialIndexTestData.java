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

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.SRSInfo;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.geosparql.implementation.vocabulary.Geo;
import org.apache.jena.geosparql.implementation.vocabulary.SRS_URI;
import org.apache.jena.geosparql.spatial.index.v2.STRtreePerGraph;
import org.apache.jena.geosparql.spatial.index.v2.STRtreeUtils;
import org.apache.jena.geosparql.spatial.index.v2.SpatialIndexPerGraph;
import org.apache.jena.geosparql.spatial.index.v2.SpatialIndexLib;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.locationtech.jts.index.strtree.STRtree;

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

    private static SpatialIndexPerGraph TEST_SPATIAL_INDEX = null;
    private static Dataset TEST_DATASET = null;

    public static final List<SpatialIndexItem> getTestItems() {
        List<SpatialIndexItem> items = List.of(
                SpatialIndexItem.of(LONDON_GEOMETRY_WRAPPER.getEnvelope(), LONDON_FEATURE.asNode()),
                SpatialIndexItem.of(NEW_YORK_GEOMETRY_WRAPPER.getEnvelope(), NEW_YORK_FEATURE.asNode()),
                SpatialIndexItem.of(HONOLULU_GEOMETRY_WRAPPER.getEnvelope(), HONOLULU_FEATURE.asNode()),
                SpatialIndexItem.of(PERTH_GEOMETRY_WRAPPER.getEnvelope(), PERTH_FEATURE.asNode()),
                SpatialIndexItem.of(AUCKLAND_GEOMETRY_WRAPPER.getEnvelope(), AUCKLAND_FEATURE.asNode()));
        return items;
    }

    public static final SpatialIndexPerGraph createTestIndex() {

        if (TEST_SPATIAL_INDEX == null) {
            try {
                // SpatialIndexPerGraph spatialIndex = new SpatialIndexPerGraph(100, SRS_URI.WGS84_CRS);
                List<SpatialIndexItem> items = getTestItems();
                STRtree tree = STRtreeUtils.buildSpatialIndexTree(items);
                STRtreePerGraph index = new STRtreePerGraph(tree);
                SpatialIndexPerGraph spatialIndex = new SpatialIndexPerGraph(index);
                TEST_SPATIAL_INDEX = spatialIndex;
            } catch (SpatialIndexException ex) {

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
            SpatialIndexLib.setSpatialIndex(dataset, spatialIndex);
            TEST_DATASET = dataset;
        }

        return TEST_DATASET;
    }

    public static Set<Node> asNodes(Collection<Resource> resources) {
        return resources.stream().map(Resource::asNode).collect(Collectors.toSet());
    }

}
