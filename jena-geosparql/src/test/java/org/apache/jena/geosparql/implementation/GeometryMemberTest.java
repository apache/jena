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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;

import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.geosparql.implementation.jts.CoordinateSequenceDimensions;
import org.apache.jena.geosparql.implementation.jts.CustomGeometryFactory;
import org.apache.jena.geosparql.implementation.parsers.wkt.WKTWriter;
import org.apache.jena.geosparql.implementation.vocabulary.SRS_URI;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.Point;

public class GeometryMemberTest {
    @Test
    public void atomicIndexOnePreservesTheAtomicGeometry() {
        GeometryWrapper point = geometry("POINT Z EMPTY");
        GeometryWrapper selected = point.getGeometryN(1);
        assertEquals(point.getGeometryDatatypeURI(), selected.getGeometryDatatypeURI());
        assertEquals(point.getSrsURI(), selected.getSrsURI());
        assertEquals(point.getCoordinateSequenceDimensions(), selected.getCoordinateSequenceDimensions());
        assertEquals(point.getGeometryType(), selected.getGeometryType());
        assertEquals(WKTWriter.write(point), WKTWriter.write(selected));
        assertThrows(IllegalArgumentException.class, () -> point.getGeometryN(0));
        assertThrows(IllegalArgumentException.class, () -> point.getGeometryN(2));
    }

    @Test
    public void nestedCollectionsAreSelectedWithoutFlattening() {
        GeometryWrapper nested = collection(CoordinateSequenceDimensions.XY,
            geometry("POINT (1 2)").getParsingGeometry(), geometry("POINT (3 4)").getParsingGeometry());
        GeometryWrapper source = collection(CoordinateSequenceDimensions.XY,
            geometry("POINT (9 9)").getParsingGeometry(), nested.getParsingGeometry());
        GeometryWrapper selected = source.getGeometryN(2);
        assertEquals("GeometryCollection", selected.getGeometryType());
        assertEquals(2, selected.getParsingGeometry().getNumGeometries());
        assertEquals(3, selected.getGeometryN(2).getXYGeometry().getCoordinate().getX(), 0);
        GeometryWrapper reparsed = GeometryWrapper.extract(selected.asNodeValue());
        assertEquals(2, reparsed.getParsingGeometry().getNumGeometries());
        assertEquals(3, reparsed.getGeometryN(2).getXYGeometry().getCoordinate().getX(), 0);
    }

    @Test
    public void allEmptyCollectionMembersSurviveSerialization() {
        GeometryWrapper nested = collection(CoordinateSequenceDimensions.XYZ,
            geometry("POINT Z EMPTY").getParsingGeometry());
        GeometryWrapper source = collection(CoordinateSequenceDimensions.XYZ, nested.getParsingGeometry());
        GeometryWrapper selected = GeometryWrapper.extract(source.getGeometryN(1).asNodeValue());
        assertEquals(1, selected.getParsingGeometry().getNumGeometries());
        assertEquals(CoordinateSequenceDimensions.XYZ, selected.getGeometryN(1).getCoordinateSequenceDimensions());
    }

    @Test
    public void selectedMixedCollectionRetainsEachMemberLayout() {
        GeometryWrapper nested = collection(CoordinateSequenceDimensions.XY,
            geometry("POINT Z (1 2 3)").getParsingGeometry(), geometry("POINT M (4 5 6)").getParsingGeometry());
        GeometryWrapper source = collection(CoordinateSequenceDimensions.XY, nested.getParsingGeometry());
        GeometryWrapper selected = GeometryWrapper.extract(source.getGeometryN(1).asNodeValue());
        assertEquals(CoordinateSequenceDimensions.XYZ, selected.getGeometryN(1).getCoordinateSequenceDimensions());
        assertEquals(CoordinateSequenceDimensions.XYM, selected.getGeometryN(2).getCoordinateSequenceDimensions());
    }

    @Test
    public void memberMetadataComesFromTheSelectedCoordinateSequence() {
        GeometryWrapper source = geometry("GEOMETRYCOLLECTION (POINT Z (1 2 3), LINESTRING M (1 2 7, 3 4 9))");
        GeometryWrapper point = source.getGeometryN(1);
        GeometryWrapper line = source.getGeometryN(2);
        assertEquals(CoordinateSequenceDimensions.XYZ, point.getCoordinateSequenceDimensions());
        assertEquals(CoordinateSequenceDimensions.XYM, line.getCoordinateSequenceDimensions());
        assertEquals(0, point.getTopologicalDimension());
        assertEquals(1, line.getTopologicalDimension());
    }

    @Test
    public void emptyAggregateMembersRetainExplicitLayoutWhenParentIsXY() {
        String[] members = {
            "MULTIPOINT Z EMPTY",
            "MULTILINESTRING M EMPTY",
            "MULTIPOLYGON ZM EMPTY",
            "GEOMETRYCOLLECTION Z EMPTY"
        };
        for (String member : members) {
            GeometryWrapper selected = geometry("GEOMETRYCOLLECTION (" + member + ", POINT (1 2))").getGeometryN(1);
            GeometryWrapper expected = geometry(member);
            assertEquals(member, expected.getCoordinateSequenceDimensions(), selected.getCoordinateSequenceDimensions());
            assertEquals(member, expected.getGeometryType(), selected.getGeometryType());
            assertEquals(member, WKTWriter.write(expected), WKTWriter.write(selected));
        }
    }

    @Test
    public void selectedAuthorityAxisPointRetainsZInBothCoordinateOrders() {
        GeometryWrapper selected = geometry(
            "<http://www.opengis.net/def/crs/EPSG/0/4979> MULTIPOINT Z ((10 100 7), (20 120 9))")
            .getGeometryN(2);
        Point parsing = (Point)selected.getParsingGeometry();
        Point normalized = (Point)selected.getXYGeometry();
        assertEquals(CoordinateSequenceDimensions.XYZ, selected.getCoordinateSequenceDimensions());
        assertEquals(20, parsing.getX(), 0);
        assertEquals(120, parsing.getY(), 0);
        assertEquals(9, parsing.getCoordinateSequence().getZ(0), 0);
        assertEquals(120, normalized.getX(), 0);
        assertEquals(20, normalized.getY(), 0);
        assertEquals(9, normalized.getCoordinateSequence().getZ(0), 0);
    }

    @Test
    public void emptyCollectionsRejectEveryIndex() {
        GeometryWrapper empty = geometry("GEOMETRYCOLLECTION EMPTY");
        assertThrows(IllegalArgumentException.class, () -> empty.getGeometryN(1));
        assertThrows(IllegalArgumentException.class, () -> empty.getGeometryN(-1));
        assertThrows(IllegalArgumentException.class, () -> empty.getGeometryN(Integer.MAX_VALUE));
    }

    @Test
    public void emptyMultiMembersSurviveSerialization() {
        var factory = CustomGeometryFactory.theInstance();
        for (String marker : new String[] { "", "Z", "M", "ZM" }) {
            CoordinateSequenceDimensions layout = layout(marker);
            Point point = (Point)geometry("POINT " + marker + " EMPTY").getParsingGeometry();
            LineString line = (LineString)geometry("LINESTRING " + marker + " EMPTY").getParsingGeometry();
            Polygon polygon = (Polygon)geometry("POLYGON " + marker + " EMPTY").getParsingGeometry();
            Geometry[] multis = {
                factory.createMultiPoint(new Point[] { point, point }),
                factory.createMultiLineString(new LineString[] { line, line }),
                factory.createMultiPolygon(new Polygon[] { polygon, polygon })
            };
            for (Geometry multi : multis) {
                GeometryWrapper selected = collection(layout, multi).getGeometryN(1);
                assertEmptyMultiRoundTrip(multi.getGeometryType() + " " + marker, selected, layout, true, true);
            }
        }
    }

    @Test
    public void mixedEmptyMultiMembersSurviveSerialization() {
        var factory = CustomGeometryFactory.theInstance();
        Geometry[] multis = {
            factory.createMultiPoint(new Point[] {
                (Point)geometry("POINT (1 2)").getParsingGeometry(),
                (Point)geometry("POINT EMPTY").getParsingGeometry() }),
            factory.createMultiLineString(new LineString[] {
                (LineString)geometry("LINESTRING (0 0, 1 1)").getParsingGeometry(),
                (LineString)geometry("LINESTRING EMPTY").getParsingGeometry() }),
            factory.createMultiPolygon(new Polygon[] {
                (Polygon)geometry("POLYGON ((0 0, 1 0, 0 1, 0 0))").getParsingGeometry(),
                (Polygon)geometry("POLYGON EMPTY").getParsingGeometry() })
        };
        for (Geometry multi : multis) {
            GeometryWrapper selected = collection(CoordinateSequenceDimensions.XY, multi).getGeometryN(1);
            assertFalse(multi.getGeometryType(), selected.isEmpty());
            assertEmptyMultiRoundTrip(multi.getGeometryType(), selected, CoordinateSequenceDimensions.XY, false, true);
        }
    }

    @Test
    public void emptyMultiMemberWktRoundTripsMemberCountOrderAndLayout() {
        String[] wkts = {
            "MULTIPOINT (EMPTY, EMPTY)",
            "MULTIPOINT ((1 2), EMPTY)",
            "MULTILINESTRING (EMPTY, EMPTY)",
            "MULTILINESTRING ((0 0, 1 1), EMPTY)",
            "MULTIPOLYGON (EMPTY, EMPTY)",
            "MULTIPOLYGON (((0 0, 1 0, 0 1, 0 0)), EMPTY)"
        };
        boolean[][] emptiness = {
            { true, true },
            { false, true },
            { true, true },
            { false, true },
            { true, true },
            { false, true }
        };
        for (int i = 0; i < wkts.length; i++) {
            GeometryWrapper original = geometry(wkts[i]);
            assertEmptyMultiRoundTrip(wkts[i], original, CoordinateSequenceDimensions.XY,
                    emptiness[i][0], emptiness[i][1]);
        }
    }

    @Test
    public void blankMultiMembersAreRejected() {
        for (String wkt : new String[] {
                "MULTIPOINT ((1 2),, (3 4))",
                "MULTIPOINT (, (1 2))",
                "MULTIPOINT ((1 2),)",
                "MULTILINESTRING ((0 0, 1 1),, (2 2, 3 3))",
                "MULTILINESTRING (, (0 0, 1 1))",
                "MULTILINESTRING ((0 0, 1 1),)",
                "MULTIPOLYGON (((0 0, 1 0, 0 1, 0 0)),, ((2 2, 3 2, 2 3, 2 2)))",
                "MULTIPOLYGON (, ((0 0, 1 0, 0 1, 0 0)))",
                "MULTIPOLYGON (((0 0, 1 0, 0 1, 0 0)),)" }) {
            assertThrows(wkt, DatatypeFormatException.class, () -> geometry(wkt));
        }
    }

    @Test
    public void unbalancedMultiParenthesesAreRejected() {
        for (String wkt : new String[] {
                "MULTIPOINT ((1 2)",
                "MULTIPOINT ((1 2)))",
                "MULTILINESTRING ((0 0, 1 1)",
                "MULTILINESTRING ((0 0, 1 1)))",
                "MULTIPOLYGON (((0 0, 1 0, 0 1, 0 0))",
                "MULTIPOLYGON (((0 0, 1 0, 0 1, 0 0))))" }) {
            assertThrows(wkt, DatatypeFormatException.class, () -> geometry(wkt));
        }
    }

    @Test
    public void nestedMultiGeometriesInCollectionsRoundTrip() {
        String[][] cases = {
            { "GEOMETRYCOLLECTION (MULTIPOINT ((1 2), (3 4)), POINT (5 6))", "MultiPoint", "false", "false" },
            { "GEOMETRYCOLLECTION (MULTIPOINT (EMPTY, EMPTY), POINT (5 6))", "MultiPoint", "true", "true" },
            { "GEOMETRYCOLLECTION (MULTIPOINT ((1 2), EMPTY), POINT (5 6))", "MultiPoint", "false", "true" },
            { "GEOMETRYCOLLECTION (MULTILINESTRING ((0 0, 1 1), (2 2, 3 3)), POINT (5 6))", "MultiLineString", "false", "false" },
            { "GEOMETRYCOLLECTION (MULTILINESTRING (EMPTY, EMPTY), POINT (5 6))", "MultiLineString", "true", "true" },
            { "GEOMETRYCOLLECTION (MULTILINESTRING ((0 0, 1 1), EMPTY), POINT (5 6))", "MultiLineString", "false", "true" },
            { "GEOMETRYCOLLECTION (MULTIPOLYGON (((0 0, 1 0, 0 1, 0 0)), ((2 2, 3 2, 2 3, 2 2))), POINT (5 6))", "MultiPolygon", "false", "false" },
            { "GEOMETRYCOLLECTION (MULTIPOLYGON (EMPTY, EMPTY), POINT (5 6))", "MultiPolygon", "true", "true" },
            { "GEOMETRYCOLLECTION (MULTIPOLYGON (((0 0, 1 0, 0 1, 0 0)), EMPTY), POINT (5 6))", "MultiPolygon", "false", "true" }
        };
        for (String[] test : cases) {
            GeometryWrapper original = geometry(test[0]);
            assertEquals(test[0], 2, original.getParsingGeometry().getNumGeometries());
            assertEquals(test[0], test[1], original.getGeometryN(1).getGeometryType());
            assertEquals(test[0], "Point", original.getGeometryN(2).getGeometryType());
            assertEmptyMultiRoundTrip(test[0], original.getGeometryN(1), CoordinateSequenceDimensions.XY,
                    Boolean.parseBoolean(test[2]), Boolean.parseBoolean(test[3]));
            GeometryWrapper reparsed = reparse(original);
            assertEquals(test[0], 2, reparsed.getParsingGeometry().getNumGeometries());
            assertEquals(test[0], test[1], reparsed.getGeometryN(1).getGeometryType());
            assertEquals(test[0], 5, reparsed.getGeometryN(2).getXYGeometry().getCoordinate().getX(), 0);
            assertEquals(test[0], 6, reparsed.getGeometryN(2).getXYGeometry().getCoordinate().getY(), 0);
            assertEmptyMultiRoundTrip(test[0] + " reparsed", reparsed.getGeometryN(1),
                    CoordinateSequenceDimensions.XY, Boolean.parseBoolean(test[2]), Boolean.parseBoolean(test[3]));
        }
    }

    private static void assertEmptyMultiRoundTrip(String label, GeometryWrapper original,
            CoordinateSequenceDimensions layout, boolean firstEmpty, boolean secondEmpty) {
        assertEquals(label, 2, original.getParsingGeometry().getNumGeometries());
        GeometryWrapper reparsed = reparse(original);
        assertEquals(label, 2, reparsed.getParsingGeometry().getNumGeometries());
        assertEquals(label, layout, reparsed.getCoordinateSequenceDimensions());
        for (int index = 1; index <= 2; index++) {
            GeometryWrapper expectedMember = original.getGeometryN(index);
            GeometryWrapper actualMember = reparsed.getGeometryN(index);
            boolean expectEmpty = index == 1 ? firstEmpty : secondEmpty;
            assertEquals(label + " type " + index, expectedMember.getGeometryType(), actualMember.getGeometryType());
            assertEquals(label + " empty " + index, expectEmpty, actualMember.isEmpty());
            assertEquals(label + " layout " + index, layout, actualMember.getCoordinateSequenceDimensions());
            if (!expectEmpty) {
                assertEquals(label + " member " + index, expectedMember.getParsingGeometry().toText(),
                             actualMember.getParsingGeometry().toText());
            }
        }
    }

    private static CoordinateSequenceDimensions layout(String marker) {
        switch (marker) {
            case "Z":
                return CoordinateSequenceDimensions.XYZ;
            case "M":
                return CoordinateSequenceDimensions.XYM;
            case "ZM":
                return CoordinateSequenceDimensions.XYZM;
            default:
                return CoordinateSequenceDimensions.XY;
        }
    }

    private static GeometryWrapper reparse(GeometryWrapper original) {
        return GeometryWrapper.extract(WKTWriter.write(original), WKTDatatype.URI);
    }

    private static GeometryWrapper geometry(String wkt) {
        return GeometryWrapper.extract(wkt, WKTDatatype.URI);
    }

    private static GeometryWrapper collection(CoordinateSequenceDimensions dimensions, Geometry... members) {
        Geometry collection = CustomGeometryFactory.theInstance().createGeometryCollection(members);
        return new GeometryWrapper(collection, SRS_URI.DEFAULT_WKT_CRS84, WKTDatatype.URI,
                                   new DimensionInfo(dimensions, collection.getDimension()));
    }
}
