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

import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.geosparql.implementation.datatype.GMLDatatype;
import org.apache.jena.geosparql.implementation.datatype.GeometryDatatype;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.geosparql.implementation.vocabulary.Geo;
import org.apache.jena.geosparql.implementation.vocabulary.SRS_URI;
import org.apache.jena.geosparql.implementation.vocabulary.SpatialExtension;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 *
 */
public class GeoSPARQLOperationsTest {

    public GeoSPARQLOperationsTest() {
    }

    private static final Model ORIGINAL_DATA = ModelFactory.createDefaultModel();
    private static final Model CONVERTED_DATATYPE_DATA = ModelFactory.createDefaultModel();
    private static final Model CONVERTED_SRS_DATA = ModelFactory.createDefaultModel();
    private static final Model CONVERTED_SRS_DATATYPE_DATA = ModelFactory.createDefaultModel();
    private static final Model GEO_DATA = ModelFactory.createDefaultModel();
    private static final Model GEO_ALL_DATA = ModelFactory.createDefaultModel();

    private static final Dataset ORIGINAL_DATASET = DatasetFactory.createTxnMem();
    private static final Dataset CONVERTED_DATATYPE_DATASET = DatasetFactory.createTxnMem();
    private static final Dataset CONVERTED_SRS_DATASET = DatasetFactory.createTxnMem();
    private static final Dataset CONVERTED_SRS_DATATYPE_DATASET = DatasetFactory.createTxnMem();
    private static final Dataset GEO_DATASET = DatasetFactory.createTxnMem();
    private static final Dataset GEO_ALL_DATASET = DatasetFactory.createTxnMem();

    @BeforeClass
    public static void setUpClass() {

        Resource featureA = ResourceFactory.createResource("http://example.org/FeatureA");
        Resource geometryA = ResourceFactory.createResource("http://example.org/GeometryA");
        Literal wktA = ResourceFactory.createTypedLiteral("POINT(0 1)", WKTDatatype.INSTANCE);
        ORIGINAL_DATA.add(featureA, Geo.HAS_GEOMETRY_PROP, geometryA);
        ORIGINAL_DATA.add(geometryA, Geo.HAS_SERIALIZATION_PROP, wktA);
        ORIGINAL_DATASET.setDefaultModel(ORIGINAL_DATA);

        Literal gmlA = ResourceFactory.createTypedLiteral("<gml:Point xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"http://www.opengis.net/def/crs/EPSG/0/4326\"><gml:pos>1 0</gml:pos></gml:Point>", GMLDatatype.INSTANCE);
        CONVERTED_DATATYPE_DATA.add(featureA, Geo.HAS_GEOMETRY_PROP, geometryA);
        CONVERTED_DATATYPE_DATA.add(geometryA, Geo.HAS_SERIALIZATION_PROP, gmlA);
        CONVERTED_DATATYPE_DATASET.setDefaultModel(CONVERTED_DATATYPE_DATA);

        Literal wktWGSA = ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(1 0)", WKTDatatype.INSTANCE);
        CONVERTED_SRS_DATA.add(featureA, Geo.HAS_GEOMETRY_PROP, geometryA);
        CONVERTED_SRS_DATA.add(geometryA, Geo.HAS_SERIALIZATION_PROP, wktWGSA);
        CONVERTED_SRS_DATASET.setDefaultModel(CONVERTED_SRS_DATA);

        Literal gmlWGSA = ResourceFactory.createTypedLiteral("<gml:Point xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"http://www.opengis.net/def/crs/EPSG/0/4326\"><gml:pos>1 0</gml:pos></gml:Point>", GMLDatatype.INSTANCE);
        CONVERTED_SRS_DATATYPE_DATA.add(featureA, Geo.HAS_GEOMETRY_PROP, geometryA);
        CONVERTED_SRS_DATATYPE_DATA.add(geometryA, Geo.HAS_SERIALIZATION_PROP, gmlWGSA);
        CONVERTED_SRS_DATATYPE_DATASET.setDefaultModel(CONVERTED_SRS_DATATYPE_DATA);

        Literal latA = ResourceFactory.createTypedLiteral("1.0", XSDDatatype.XSDfloat);
        Literal lonA = ResourceFactory.createTypedLiteral("0.0", XSDDatatype.XSDfloat);
        GEO_DATA.add(featureA, SpatialExtension.GEO_LAT_PROP, latA);
        GEO_DATA.add(featureA, SpatialExtension.GEO_LON_PROP, lonA);
        GEO_DATASET.setDefaultModel(GEO_DATA);

        GEO_ALL_DATA.add(GEO_DATA);
        GEO_ALL_DATA.add(CONVERTED_SRS_DATA);
        GEO_ALL_DATASET.setDefaultModel(GEO_ALL_DATA);
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of convert method, of class GeoSPARQLOperations.
     */
    @Test
    public void testConvert_Model() {

        Model inputModel = CONVERTED_DATATYPE_DATA;
        TreeSet<String> expResult = extract(CONVERTED_SRS_DATA);
        Model instance = GeoSPARQLOperations.convert(inputModel);
        TreeSet<String> result = extract(instance);
        assertEquals(expResult, result);
    }

    /**
     * Test of convert method, of class GeoSPARQLOperations.
     */
    @Test
    public void testConvert_Model_String() {

        Model inputModel = ORIGINAL_DATA;
        String outputSrsURI = SRS_URI.WGS84_CRS;
        TreeSet<String> expResult = extract(CONVERTED_SRS_DATA);
        Model instance = GeoSPARQLOperations.convert(inputModel, outputSrsURI);
        TreeSet<String> result = extract(instance);
        assertEquals(expResult, result);
    }

    /**
     * Test of convert method, of class GeoSPARQLOperations.
     */
    @Test
    public void testConvert_Model_GeometryDatatype() {

        Model inputModel = CONVERTED_SRS_DATA;
        GeometryDatatype outputDatatype = GMLDatatype.INSTANCE;
        TreeSet<String> expResult = extract(CONVERTED_DATATYPE_DATA);
        Model instance = GeoSPARQLOperations.convert(inputModel, outputDatatype);
        TreeSet<String> result = extract(instance);
        assertEquals(expResult, result);
    }

    /**
     * Test of convert method, of class GeoSPARQLOperations.
     */
    @Test
    public void testConvert_3args_1() {

        Model inputModel = ORIGINAL_DATA;
        String outputSrsURI = SRS_URI.WGS84_CRS;
        GeometryDatatype outputDatatype = GMLDatatype.INSTANCE;
        TreeSet<String> expResult = extract(CONVERTED_SRS_DATATYPE_DATA);
        Model instance = GeoSPARQLOperations.convert(inputModel, outputSrsURI, outputDatatype);
        TreeSet<String> result = extract(instance);
        assertEquals(expResult, result);
    }

    /**
     * Test of convert method, of class GeoSPARQLOperations.
     */
    @Test
    public void testConvert_Dataset() {

        Dataset dataset = CONVERTED_DATATYPE_DATASET;
        TreeSet<String> expResult = extract(CONVERTED_SRS_DATASET);
        Dataset instance = GeoSPARQLOperations.convert(dataset);
        TreeSet<String> result = extract(instance);
        assertEquals(expResult, result);
    }

    /**
     * Test of convert method, of class GeoSPARQLOperations.
     */
    @Test
    public void testConvert_Dataset_String() {

        Dataset dataset = ORIGINAL_DATASET;
        String outputSrsURI = SRS_URI.WGS84_CRS;
        TreeSet<String> expResult = extract(CONVERTED_SRS_DATASET);
        Dataset instance = GeoSPARQLOperations.convert(dataset, outputSrsURI);
        TreeSet<String> result = extract(instance);
        assertEquals(expResult, result);
    }

    /**
     * Test of convert method, of class GeoSPARQLOperations.
     */
    @Test
    public void testConvert_Dataset_GeometryDatatype() {

        Dataset dataset = CONVERTED_SRS_DATASET;
        GeometryDatatype outputDatatype = GMLDatatype.INSTANCE;
        TreeSet<String> expResult = extract(CONVERTED_DATATYPE_DATASET);
        Dataset instance = GeoSPARQLOperations.convert(dataset, outputDatatype);
        TreeSet<String> result = extract(instance);
        assertEquals(expResult, result);
    }

    /**
     * Test of convert method, of class GeoSPARQLOperations.
     */
    @Test
    public void testConvert_3args_2() {

        Dataset inputDataset = ORIGINAL_DATASET;
        String outputSrsURI = SRS_URI.WGS84_CRS;
        GeometryDatatype outputDatatype = GMLDatatype.INSTANCE;
        TreeSet<String> expResult = extract(CONVERTED_SRS_DATATYPE_DATASET);
        Dataset instance = GeoSPARQLOperations.convert(inputDataset, outputSrsURI, outputDatatype);
        TreeSet<String> result = extract(instance);
        assertEquals(expResult, result);
    }

    /**
     * Test of convertGeometryLiterals method, of class GeoSPARQLOperations.
     */
    @Test
    public void testConvertGeometryLiterals() {

        List<String> geometryLiterals = Arrays.asList("POINT(0 1)", "POINT(2 3)");
        String outputSrsURI = SRS_URI.WGS84_CRS;
        GeometryDatatype outputDatatype = WKTDatatype.INSTANCE;
        List<String> expResult = Arrays.asList("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(1 0)", "<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(3 2)");
        List<String> result = GeoSPARQLOperations.convertGeometryLiterals(geometryLiterals, outputSrsURI, outputDatatype);
        assertEquals(expResult, result);
    }

    /**
     * Test of convertGeometryLiteral method, of class GeoSPARQLOperations.
     */
    @Test
    public void testConvertGeometryLiteral() {

        String geometryLiteral = "POINT(0 1)";
        String outputSrsURI = SRS_URI.WGS84_CRS;
        GeometryDatatype outputDatatype = WKTDatatype.INSTANCE;
        String expResult = "<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(1 0)";
        String result = GeoSPARQLOperations.convertGeometryLiteral(geometryLiteral, outputSrsURI, outputDatatype);
        assertEquals(expResult, result);
    }

    /**
     * Test of convertGeoPredicates method, of class GeoSPARQLOperations.
     */
    @Test
    public void testConvertGeoPredicates_Dataset_remove() {

        Dataset dataset = GEO_DATASET;
        boolean isRemoveGeoPredicate = true;
        TreeSet<String> expResult = extract(CONVERTED_SRS_DATASET);
        Dataset instance = GeoSPARQLOperations.convertGeoPredicates(dataset, isRemoveGeoPredicate);
        TreeSet<String> result = extract(instance);
        assertEquals(expResult, result);
    }

    /**
     * Test of convertGeoPredicates method, of class GeoSPARQLOperations.
     */
    @Test
    public void testConvertGeoPredicates_Model_remove() {

        Model model = GEO_DATA;
        boolean isRemoveGeoPredicates = true;
        TreeSet<String> expResult = extract(CONVERTED_SRS_DATA);
        Model instance = GeoSPARQLOperations.convertGeoPredicates(model, isRemoveGeoPredicates);
        TreeSet<String> result = extract(instance);
        assertEquals(expResult, result);
    }

    /**
     * Test of convertGeoPredicates method, of class GeoSPARQLOperations.
     */
    @Test
    public void testConvertGeoPredicates_Dataset_keep() {

        Dataset dataset = GEO_DATASET;
        boolean isRemoveGeoPredicate = false;
        TreeSet<String> expResult = extract(GEO_ALL_DATASET);
        Dataset instance = GeoSPARQLOperations.convertGeoPredicates(dataset, isRemoveGeoPredicate);
        TreeSet<String> result = extract(instance);
        assertEquals(expResult, result);
    }

    /**
     * Test of convertGeoPredicates method, of class GeoSPARQLOperations.
     */
    @Test
    public void testConvertGeoPredicates_Model_keep() {

        Model model = GEO_DATA;
        boolean isRemoveGeoPredicates = false;
        TreeSet<String> expResult = extract(GEO_ALL_DATA);
        Model instance = GeoSPARQLOperations.convertGeoPredicates(model, isRemoveGeoPredicates);
        TreeSet<String> result = extract(instance);
        assertEquals(expResult, result);
    }

    private static TreeSet<String> extract(Model model) {

        TreeSet<String> result = new TreeSet<>();
        StmtIterator iter = model.listStatements();
        while (iter.hasNext()) {
            Statement stmt = iter.nextStatement();
            String str = stmt.toString();
            //Remove the replace the generated Geometry with expected value.
            if (str.contains("FeatureA-Geom-")) {
                int begin = str.indexOf("FeatureA-Geom-");
                int end = str.indexOf(", ", begin);
                if (end == -1) {
                    end = str.length() - 1;
                }
                String replace = str.substring(begin, end);
                str = str.replaceAll(replace, "GeometryA");
            }
            result.add(str);
        }
        return result;
    }

    private static TreeSet<String> extract(Dataset dataset) {
        return extract(dataset.getDefaultModel());
    }

}
