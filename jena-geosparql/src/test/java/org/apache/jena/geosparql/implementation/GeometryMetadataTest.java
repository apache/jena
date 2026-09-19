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
package org.apache.jena.geosparql.implementation;

import static org.junit.Assert.assertEquals;

import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.geosparql.implementation.vocabulary.GeoSPARQL_URI;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

public class GeometryMetadataTest {

    @Test
    public void geometryTypesIncludeTypedEmpties() {
        String[] types = { "Point", "LineString", "LinearRing", "Polygon", "MultiPoint",
                           "MultiLineString", "MultiPolygon", "GeometryCollection" };
        for (String type : types) {
            GeometryWrapper geometry = geometry(type.toUpperCase(java.util.Locale.ROOT) + " EMPTY");
            assertEquals(type, GeoSPARQL_URI.SF_URI + type, geometry.getGeometryTypeURI());
        }
    }

    @Test
    public void geometryTypesIncludeNonemptyGeometries() {
        String[][] cases = {
            { "POINT (1 2)", "Point" },
            { "LINESTRING (0 0, 1 1)", "LineString" },
            { "LINEARRING (0 0, 1 0, 1 1, 0 0)", "LinearRing" },
            { "POLYGON ((0 0, 1 0, 0 1, 0 0))", "Polygon" },
            { "MULTIPOINT ((1 2))", "MultiPoint" },
            { "MULTILINESTRING ((0 0, 1 1))", "MultiLineString" },
            { "MULTIPOLYGON (((0 0, 1 0, 0 1, 0 0)))", "MultiPolygon" },
            { "GEOMETRYCOLLECTION (POINT (1 2))", "GeometryCollection" }
        };
        for (String[] example : cases) {
            assertEquals(example[0], GeoSPARQL_URI.SF_URI + example[1], geometry(example[0]).getGeometryTypeURI());
        }
    }

    @Test
    public void directlyConstructedLinearRingHasSimpleFeaturesType() {
        GeometryWrapper ring = new GeometryWrapper(new GeometryFactory().createLinearRing(), WKTDatatype.URI);
        assertEquals(GeoSPARQL_URI.SF_URI + "LinearRing", ring.getGeometryTypeURI());
    }

    @Test
    public void coordinateLayoutsDistinguishZAndM() {
        assertLayout("POINT (1 2)", false, false);
        assertLayout("POINT Z (1 2 3)", true, false);
        assertLayout("POINT M (1 2 3)", false, true);
        assertLayout("POINT ZM (1 2 3 4)", true, true);
        assertLayout("<http://www.opengis.net/def/crs/EPSG/0/4979> POINT Z (2 1 3)", true, false);
    }

    @Test
    public void emptyPointsRetainDeclaredCoordinateLayouts() {
        assertLayout("POINT EMPTY", false, false);
        assertLayout("POINT Z EMPTY", true, false);
        assertLayout("POINT M EMPTY", false, true);
        assertLayout("POINT ZM EMPTY", true, true);
    }

    @Test
    public void collectionsUseTheirDeclaredCoordinateLayout() {
        assertLayout("GEOMETRYCOLLECTION Z (POINT Z (1 2 3), LINESTRING Z (1 2 3, 4 5 6))", true, false);
        assertLayout("GEOMETRYCOLLECTION M (POINT M (1 2 3))", false, true);
        assertLayout("GEOMETRYCOLLECTION ZM (POINT ZM (1 2 3 4))", true, true);
    }

    @Test
    public void permissiveWktCollectionLayoutDoesNotAggregateMemberLayouts() {
        // Mixed member layouts are a Jena WKT extension, not an SFA conformance case.
        String wkt = "GEOMETRYCOLLECTION (POINT Z (1 2 3), POINT M (4 5 6))";
        // WKT collection metadata comes from the collection marker, not its members.
        GeometryWrapper geometry = geometry(wkt);
        assertEquals(2, geometry.getCoordinateDimension());
        assertEquals(2, geometry.getSpatialDimension());
        assertLayout(wkt, false, false);
    }

    @Test
    public void atomicGeometriesCountAsOneIncludingEmpties() {
        assertCount("POINT (1 2)", 1);
        assertCount("POINT EMPTY", 1);
        assertCount("LINESTRING EMPTY", 1);
        assertCount("POLYGON EMPTY", 1);
    }

    @Test
    public void collectionsWithoutMembersCountAsZero() {
        assertCount("MULTIPOINT EMPTY", 0);
        assertCount("MULTILINESTRING EMPTY", 0);
        assertCount("MULTIPOLYGON EMPTY", 0);
        assertCount("GEOMETRYCOLLECTION EMPTY", 0);
    }

    @Test
    public void collectionsCountDirectMembersIncludingEmptyMembers() {
        assertCount("MULTIPOINT ((1 2), (3 4))", 2);
        assertCount("MULTILINESTRING ((0 0, 1 1), (2 2, 3 3))", 2);
        assertCount("MULTIPOLYGON (((0 0, 1 0, 0 1, 0 0)))", 1);
        assertCount("GEOMETRYCOLLECTION (POINT EMPTY)", 1);
    }

    @Test
    public void nestedCollectionsAreNotFlattened() {
        GeometryFactory factory = new GeometryFactory();
        Geometry point = geometry("POINT (1 2)").getParsingGeometry();
        Geometry nested = factory.createGeometryCollection(new Geometry[] { point, point });
        Geometry collection = factory.createGeometryCollection(new Geometry[] { point, nested });
        GeometryWrapper wrapper = new GeometryWrapper(collection, WKTDatatype.URI);
        assertEquals(2, wrapper.getNumGeometries());
    }

    private static GeometryWrapper geometry(String wkt) {
        return GeometryWrapper.extract(wkt, WKTDatatype.URI);
    }

    private static void assertLayout(String wkt, boolean hasZ, boolean hasM) {
        GeometryWrapper geometry = geometry(wkt);
        assertEquals(wkt, hasZ, geometry.is3D());
        assertEquals(wkt, hasM, geometry.isMeasured());
    }

    private static void assertCount(String wkt, int expected) {
        assertEquals(wkt, expected, geometry(wkt).getNumGeometries());
    }
}
