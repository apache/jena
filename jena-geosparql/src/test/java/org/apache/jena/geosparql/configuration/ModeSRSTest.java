/*
 * Copyright 2019 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
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
package org.apache.jena.geosparql.configuration;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.geosparql.implementation.vocabulary.Geo;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
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
public class ModeSRSTest {

    public ModeSRSTest() {
    }
    private static final Model MODEL = ModelFactory.createDefaultModel();

    @BeforeClass
    public static void setUpClass() {

        Resource res = MODEL.createResource("http://example.org/GeometryA");
        MODEL.addLiteral(res, Geo.HAS_SERIALIZATION_PROP, ResourceFactory.createTypedLiteral("POINT(1.0 1.0)", WKTDatatype.INSTANCE));
        MODEL.addLiteral(res, Geo.HAS_SERIALIZATION_PROP, ResourceFactory.createTypedLiteral("POINT(2.0 2.0)", WKTDatatype.INSTANCE));
        MODEL.addLiteral(res, Geo.HAS_SERIALIZATION_PROP, ResourceFactory.createTypedLiteral("POINT(3.0 3.0)", WKTDatatype.INSTANCE));
        MODEL.addLiteral(res, Geo.HAS_SERIALIZATION_PROP, ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(1.0 1.0)", WKTDatatype.INSTANCE));
        MODEL.addLiteral(res, Geo.HAS_SERIALIZATION_PROP, ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(2.0 2.0)", WKTDatatype.INSTANCE));
        MODEL.addLiteral(res, Geo.HAS_SERIALIZATION_PROP, ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> POINT(1.0 1.0)", WKTDatatype.INSTANCE));

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
     * Test of search method, of class ModeSRS.
     */
    @Test
    public void testSearch() {
        System.out.println("search");

        ModeSRS instance = new ModeSRS();
        instance.search(MODEL);

        List<Entry<String, Integer>> expResult = new ArrayList<>();
        expResult.add(new SimpleEntry<>("http://www.opengis.net/def/crs/OGC/1.3/CRS84", 3));
        expResult.add(new SimpleEntry<>("http://www.opengis.net/def/crs/EPSG/0/4326", 2));
        expResult.add(new SimpleEntry<>("http://www.opengis.net/def/crs/EPSG/0/27700", 1));

        List<Entry<String, Integer>> result = instance.getSrsList();

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of getModeURI method, of class ModeSRS.
     */
    @Test
    public void testGetModeURI() {
        System.out.println("getModeURI");
        ModeSRS instance = new ModeSRS();
        instance.search(MODEL);
        String expResult = "http://www.opengis.net/def/crs/OGC/1.3/CRS84";
        String result = instance.getModeURI();

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

}
