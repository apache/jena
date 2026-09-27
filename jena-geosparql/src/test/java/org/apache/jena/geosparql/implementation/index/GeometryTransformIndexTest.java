/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */
package org.apache.jena.geosparql.implementation.index;

import static org.junit.Assert.assertEquals;

import org.apache.jena.geosparql.configuration.GeoSPARQLConfig;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.datatype.GMLDatatype;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.junit.Test;

public class GeometryTransformIndexTest {
    private static final String TARGET_SRS = "http://www.opengis.net/def/crs/EPSG/0/27700";
    private static final String WKT_POINT = "http://www.opengis.net/ont/sf#Point";
    private static final String GML_POINT = "http://www.opengis.net/ont/gml#Point";

    @Test
    public void sameLexicalFormKeepsItsDatatypeInBothCacheOrders() throws Exception {
        GeoSPARQLConfig.setupMemoryIndex();
        try {
            GeometryWrapper wkt = GeometryWrapper.extract("", WKTDatatype.URI);
            GeometryWrapper gml = GeometryWrapper.extract("", GMLDatatype.URI);

            assertTransformedType(wkt, WKTDatatype.URI, WKT_POINT);
            assertTransformedType(gml, GMLDatatype.URI, GML_POINT);

            GeometryTransformIndex.clear();
            assertTransformedType(gml, GMLDatatype.URI, GML_POINT);
            assertTransformedType(wkt, WKTDatatype.URI, WKT_POINT);
        } finally {
            GeoSPARQLConfig.setupNoIndex();
        }
    }

    private static void assertTransformedType(GeometryWrapper source, String datatypeURI,
            String geometryTypeURI) throws Exception {
        GeometryWrapper transformed = source.transform(TARGET_SRS);
        assertEquals(datatypeURI, transformed.getGeometryDatatypeURI());
        assertEquals(datatypeURI, transformed.asNodeValue().asNode().getLiteralDatatypeURI());
        assertEquals(geometryTypeURI, transformed.getGeometryTypeURI());
    }
}
