/*
 * Copyright 2019 .
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.geosparql.geo.topological;

import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.geosparql.implementation.vocabulary.Geo;
import org.apache.jena.geosparql.implementation.vocabulary.SRS_URI;
import org.apache.jena.geosparql.implementation.vocabulary.SpatialExtension;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;

/**
 *
 *
 */
public class QueryRewriteTestData {

    public static final Resource GEOMETRY_A = ResourceFactory.createResource("http://example.org#GeometryA");
    public static final Resource GEOMETRY_B = ResourceFactory.createResource("http://example.org#GeometryB");
    public static final Resource GEOMETRY_C_BLANK = ResourceFactory.createResource();
    public static final Resource GEOMETRY_D = ResourceFactory.createResource("http://example.org#GeometryD");
    public static final Resource GEOMETRY_E = ResourceFactory.createResource("http://example.org#GeometryE");
    public static final Resource GEOMETRY_F = ResourceFactory.createResource("http://example.org#GeometryF");
    public static final Resource FEATURE_A = ResourceFactory.createResource("http://example.org#FeatureA");
    public static final Resource FEATURE_B = ResourceFactory.createResource("http://example.org#FeatureB");
    public static final Resource FEATURE_C = ResourceFactory.createResource("http://example.org#FeatureC");
    public static final Resource FEATURE_D = ResourceFactory.createResource("http://example.org#FeatureD");

    public static final Literal LITERAL_B = ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> POINT(1 1)", WKTDatatype.INSTANCE);

    public static final Resource GEO_FEATURE_Y = ResourceFactory.createResource("http://example.org#GeoFeatureY");
    public static final Resource GEO_FEATURE_Z = ResourceFactory.createResource("http://example.org#GeoFeatureZ");
    public static final Literal GEO_FEATURE_LAT = ResourceFactory.createTypedLiteral("60.0", XSDDatatype.XSDfloat);
    public static final Literal GEO_FEATURE_LON = ResourceFactory.createTypedLiteral("70.0", XSDDatatype.XSDfloat);
    public static final Literal GEO_FEATURE_LITERAL = ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(60 70)", WKTDatatype.INSTANCE);

    public static final String TEST_SRS_URI = SRS_URI.OSGB36_CRS;

    private static Model TEST_MODEL = null;

    public static final Model createTestData() {

        if (TEST_MODEL == null) {
            TEST_MODEL = ModelFactory.createDefaultModel();

            //Geometry
            TEST_MODEL.add(GEOMETRY_A, Geo.HAS_SERIALIZATION_PROP, ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> POLYGON((0 0, 10 0, 10 10, 0 10, 0 0))", WKTDatatype.INSTANCE));
            TEST_MODEL.add(GEOMETRY_B, Geo.HAS_SERIALIZATION_PROP, LITERAL_B);
            TEST_MODEL.add(GEOMETRY_C_BLANK, Geo.HAS_SERIALIZATION_PROP, ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> POINT(5 5)", WKTDatatype.INSTANCE));
            TEST_MODEL.add(GEOMETRY_D, Geo.HAS_SERIALIZATION_PROP, ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> POINT(11 11)", WKTDatatype.INSTANCE));
            TEST_MODEL.add(GEOMETRY_A, RDF.type, Geo.GEOMETRY_RES);
            TEST_MODEL.add(GEOMETRY_B, RDF.type, Geo.GEOMETRY_RES);
            TEST_MODEL.add(GEOMETRY_C_BLANK, RDF.type, Geo.GEOMETRY_RES);
            TEST_MODEL.add(GEOMETRY_D, RDF.type, Geo.GEOMETRY_RES);
            TEST_MODEL.add(GEOMETRY_E, RDF.type, Geo.GEOMETRY_RES);
            TEST_MODEL.add(GEOMETRY_F, RDF.type, Geo.GEOMETRY_RES);

            //Feature
            TEST_MODEL.add(FEATURE_A, Geo.HAS_DEFAULT_GEOMETRY_PROP, GEOMETRY_A);
            TEST_MODEL.add(FEATURE_B, Geo.HAS_DEFAULT_GEOMETRY_PROP, GEOMETRY_B);
            TEST_MODEL.add(FEATURE_C, Geo.HAS_DEFAULT_GEOMETRY_PROP, GEOMETRY_C_BLANK);
            TEST_MODEL.add(FEATURE_D, Geo.HAS_DEFAULT_GEOMETRY_PROP, GEOMETRY_D);
            TEST_MODEL.add(FEATURE_A, Geo.HAS_GEOMETRY_PROP, GEOMETRY_A);
            TEST_MODEL.add(FEATURE_B, Geo.HAS_GEOMETRY_PROP, GEOMETRY_B);
            TEST_MODEL.add(FEATURE_C, Geo.HAS_GEOMETRY_PROP, GEOMETRY_C_BLANK);
            TEST_MODEL.add(FEATURE_D, Geo.HAS_GEOMETRY_PROP, GEOMETRY_D);
            TEST_MODEL.add(FEATURE_A, RDF.type, Geo.FEATURE_RES);
            TEST_MODEL.add(FEATURE_B, RDF.type, Geo.FEATURE_RES);
            TEST_MODEL.add(FEATURE_C, RDF.type, Geo.FEATURE_RES);
            TEST_MODEL.add(FEATURE_D, RDF.type, Geo.FEATURE_RES);

            //Spatial Objects
            TEST_MODEL.add(FEATURE_A, RDF.type, Geo.SPATIAL_OBJECT_RES);
            TEST_MODEL.add(FEATURE_B, RDF.type, Geo.SPATIAL_OBJECT_RES);
            TEST_MODEL.add(FEATURE_C, RDF.type, Geo.SPATIAL_OBJECT_RES);
            TEST_MODEL.add(FEATURE_D, RDF.type, Geo.SPATIAL_OBJECT_RES);
            TEST_MODEL.add(GEOMETRY_A, RDF.type, Geo.SPATIAL_OBJECT_RES);
            TEST_MODEL.add(GEOMETRY_B, RDF.type, Geo.SPATIAL_OBJECT_RES);
            TEST_MODEL.add(GEOMETRY_C_BLANK, RDF.type, Geo.SPATIAL_OBJECT_RES);
            TEST_MODEL.add(GEOMETRY_D, RDF.type, Geo.SPATIAL_OBJECT_RES);
            TEST_MODEL.add(GEOMETRY_E, RDF.type, Geo.SPATIAL_OBJECT_RES);
            TEST_MODEL.add(GEOMETRY_F, RDF.type, Geo.SPATIAL_OBJECT_RES);

            //Contains asserted
            TEST_MODEL.add(GEOMETRY_A, Geo.SF_CONTAINS_PROP, GEOMETRY_F);

            //Geo Features
            TEST_MODEL.add(GEO_FEATURE_Y, SpatialExtension.GEO_LAT_PROP, GEO_FEATURE_LAT);
            TEST_MODEL.add(GEO_FEATURE_Y, SpatialExtension.GEO_LON_PROP, GEO_FEATURE_LON);
            TEST_MODEL.add(GEO_FEATURE_Z, SpatialExtension.GEO_LAT_PROP, GEO_FEATURE_LAT);
            TEST_MODEL.add(GEO_FEATURE_Z, SpatialExtension.GEO_LON_PROP, GEO_FEATURE_LON);

        }
        return TEST_MODEL;
    }

}
