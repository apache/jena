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

import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.geosparql.implementation.jts.CoordinateSequenceDimensions;
import org.apache.jena.geosparql.implementation.jts.CustomCoordinateSequence;
import org.apache.jena.geosparql.implementation.jts.CustomGeometryFactory;
import org.apache.jena.geosparql.implementation.vocabulary.SRS_URI;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateXYM;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class GeometryCoordinateExtremaTest {
    @Test
    public void measuredMultiPointPreservesMWithoutCreatingZ() {
        GeometryWrapper geometry = GeometryWrapper.extract("MULTIPOINT M ((1 2 10), (3 4 20))", WKTDatatype.URI);
        for (int i = 0; i < 2; i++) {
            Coordinate coordinate = geometry.getParsingGeometry().getGeometryN(i).getCoordinate();
            assertEquals(10 * (i + 1), coordinate.getM(), 0);
            assertEquals(Double.NaN, coordinate.getZ(), 0);
        }
        assertThrows(IllegalStateException.class, geometry::getMinZ);
        assertThrows(IllegalStateException.class, geometry::getMaxZ);
    }

    @Test
    public void zExtremaIgnoreMeasuresInMixedJtsCollections() {
        GeometryFactory factory = new GeometryFactory();
        for (double measure : new double[] { -1000, 1000, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY }) {
            Geometry xyz = factory.createPoint(new Coordinate(1, 2, 99));
            Geometry xym = factory.createPoint(new CoordinateXYM(3, 4, measure));
            for (Geometry[] members : new Geometry[][] { { xyz, xym }, { xym, xyz } }) {
                Geometry mixed = factory.createGeometryCollection(members);
                GeometryWrapper geometry = GeometryWrapperFactory.createGeometry(mixed,
                        SRS_URI.DEFAULT_WKT_CRS84, WKTDatatype.URI);
                assertEquals(99, geometry.getMinZ(), 0);
                assertEquals(99, geometry.getMaxZ(), 0);
            }
        }
    }

    @Test
    public void factoryHandlesEmptyCollectionsWithoutCoordinates() {
        GeometryFactory factory = new GeometryFactory();
        Geometry[] members = {
            factory.createPoint(new CoordinateArraySequence(0, 3, 0)),
            factory.createPoint(new CoordinateArraySequence(0, 3, 1))
        };
        for (Geometry empty : new Geometry[] {
                factory.createGeometryCollection(),
                factory.createGeometryCollection(new Geometry[] { factory.createGeometryCollection() }),
                factory.createGeometryCollection(members) }) {
            GeometryWrapper geometry = GeometryWrapperFactory.createGeometry(empty,
                    SRS_URI.DEFAULT_WKT_CRS84, WKTDatatype.URI);
            assertThrows(IllegalStateException.class, geometry::getMinX);
            assertThrows(IllegalStateException.class, geometry::getMaxX);
            assertThrows(IllegalStateException.class, geometry::getMinY);
            assertThrows(IllegalStateException.class, geometry::getMaxY);
            assertThrows(IllegalStateException.class, geometry::getMinZ);
            assertThrows(IllegalStateException.class, geometry::getMaxZ);
        }
    }

    @Test
    public void factoryZExtremaFindLaterValuesWhenFirstZIsMissing() {
        GeometryFactory factory = new GeometryFactory();
        CoordinateSequence coordinates = new CoordinateArraySequence(2, 3, 0);
        for (int i = 0; i < coordinates.size(); i++) {
            coordinates.setOrdinate(i, 0, i);
            coordinates.setOrdinate(i, 1, i);
            coordinates.setOrdinate(i, 2, i == 0 ? Double.NaN : 9);
        }
        Geometry line = factory.createLineString(coordinates);
        Geometry homogeneousCollection = factory.createGeometryCollection(new Geometry[] {
                factory.createPoint(new Coordinate(0, 0, Double.NaN)), line });
        for (Geometry source : new Geometry[] { line, homogeneousCollection }) {
            for (String srs : new String[] { SRS_URI.DEFAULT_WKT_CRS84,
                    "http://www.opengis.net/def/crs/EPSG/0/4979" }) {
                GeometryWrapper geometry = GeometryWrapperFactory.createGeometry(source, srs, WKTDatatype.URI);
                assertEquals(9, geometry.getMinZ(), 0);
                assertEquals(9, geometry.getMaxZ(), 0);
            }
        }
    }

    @Test
    public void factoryPointPreservesZInAuthorityAxisOrder() {
        GeometryWrapper geometry = GeometryWrapperFactory.createPoint(new Coordinate(100, 10, -4),
            "http://www.opengis.net/def/crs/EPSG/0/4979", WKTDatatype.URI);
        assertEquals(10, geometry.getMinX(), 0);
        assertEquals(10, geometry.getMaxX(), 0);
        assertEquals(100, geometry.getMinY(), 0);
        assertEquals(100, geometry.getMaxY(), 0);
        assertEquals(-4, geometry.getMinZ(), 0);
        assertEquals(-4, geometry.getMaxZ(), 0);
    }

    @Test
    public void factoryGeometryPreservesZAndMeasureInAuthorityAxisOrder() {
        CustomCoordinateSequence coordinates = new CustomCoordinateSequence(
            CoordinateSequenceDimensions.XYZM, "100 10 -4 7");
        Geometry point = CustomGeometryFactory.theInstance().createPoint(coordinates);
        GeometryWrapper geometry = GeometryWrapperFactory.createGeometry(point,
            "http://www.opengis.net/def/crs/EPSG/0/4979", WKTDatatype.URI);
        assertEquals(10, geometry.getMinX(), 0);
        assertEquals(10, geometry.getMaxX(), 0);
        assertEquals(100, geometry.getMinY(), 0);
        assertEquals(100, geometry.getMaxY(), 0);
        assertEquals(-4, geometry.getMinZ(), 0);
        assertEquals(-4, geometry.getMaxZ(), 0);
        assertEquals(7, geometry.getParsingGeometry().getCoordinate().getM(), 0);
    }

    @Test
    public void xyExtremaFollowSrsDimensionOrder() {
        GeometryWrapper crs84Point = GeometryWrapper.extract(
                "<http://www.opengis.net/def/crs/OGC/1.3/CRS84> POINT(10 100)",
                WKTDatatype.URI);
        GeometryWrapper epsg4326Point = GeometryWrapper.extract(
                "<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(10 100)",
                WKTDatatype.URI);

        assertEquals(10.0, crs84Point.getMinX(), 0.0);
        assertEquals(10.0, crs84Point.getMaxX(), 0.0);
        assertEquals(100.0, crs84Point.getMinY(), 0.0);
        assertEquals(100.0, crs84Point.getMaxY(), 0.0);
        assertEquals(10.0, epsg4326Point.getMinX(), 0.0);
        assertEquals(10.0, epsg4326Point.getMaxX(), 0.0);
        assertEquals(100.0, epsg4326Point.getMinY(), 0.0);
        assertEquals(100.0, epsg4326Point.getMaxY(), 0.0);
    }

    @Test
    public void xyExtremaRejectNaNAlongsideFiniteValues() {
        CustomCoordinateSequence coordinatesX = new CustomCoordinateSequence(
                CoordinateSequenceDimensions.XY, "1 1,NaN 2,3 3");
        GeometryWrapper nanX = wrapper(
                CustomGeometryFactory.theInstance().createLineString(coordinatesX),
                CoordinateSequenceDimensions.XY);
        assertThrows(IllegalStateException.class, nanX::getMinX);
        assertThrows(IllegalStateException.class, nanX::getMaxX);
        assertEquals(1, nanX.getMinY(), 0);
        assertEquals(3, nanX.getMaxY(), 0);

        CustomCoordinateSequence coordinatesY = new CustomCoordinateSequence(
                CoordinateSequenceDimensions.XY, "1 1,2 NaN,3 3");
        GeometryWrapper nanY = wrapper(
                CustomGeometryFactory.theInstance().createLineString(coordinatesY),
                CoordinateSequenceDimensions.XY);
        assertThrows(IllegalStateException.class, nanY::getMinY);
        assertThrows(IllegalStateException.class, nanY::getMaxY);
        assertEquals(1, nanY.getMinX(), 0);
        assertEquals(3, nanY.getMaxX(), 0);
    }

    @Test
    public void zExtremaIgnoreMissingZSentinels() {
        CustomCoordinateSequence coordinates = new CustomCoordinateSequence(
                CoordinateSequenceDimensions.XYZ,
                "0 0 NaN,1 1 -4,2 2 NaN,3 3 9");
        GeometryWrapper geometry = wrapper(
                CustomGeometryFactory.theInstance().createLineString(coordinates),
                CoordinateSequenceDimensions.XYZ);

        assertEquals(-4.0, geometry.getMinZ(), 0.0);
        assertEquals(9.0, geometry.getMaxZ(), 0.0);
    }

    @Test
    public void zExtremaFailWhenAllZValuesAreMissing() {
        CustomCoordinateSequence coordinates = new CustomCoordinateSequence(
                CoordinateSequenceDimensions.XYZ,
                "0 0 NaN,1 1 NaN");
        GeometryWrapper geometry = wrapper(
                CustomGeometryFactory.theInstance().createLineString(coordinates),
                CoordinateSequenceDimensions.XYZ);

        assertThrows(IllegalStateException.class,
                () -> geometry.getMinZ());
        assertThrows(IllegalStateException.class,
                () -> geometry.getMaxZ());
    }

    @Test
    public void zExtremaRejectInfinityAlongsideFiniteValues() {
        for (String infinity : new String[] { "Infinity", "-Infinity" }) {
            CustomCoordinateSequence coordinates = new CustomCoordinateSequence(
                CoordinateSequenceDimensions.XYZ, "0 0 -4,1 1 " + infinity + ",2 2 9");
            GeometryWrapper geometry = wrapper(
                CustomGeometryFactory.theInstance().createLineString(coordinates),
                CoordinateSequenceDimensions.XYZ);
            assertThrows(infinity, IllegalStateException.class, geometry::getMinZ);
            assertThrows(infinity, IllegalStateException.class, geometry::getMaxZ);
        }
    }

    @Test
    public void zExtremaRequireZBearingSequences() {
        CoordinateSequence coordinates = new CoordinateArraySequence(
                new Coordinate[]{new Coordinate(1, 2, 99)}, 3, 1);
        GeometryWrapper geometry = wrapper(
                CustomGeometryFactory.theInstance().createPoint(coordinates),
                CoordinateSequenceDimensions.XYM);

        assertThrows(IllegalStateException.class,
                () -> geometry.getMinZ());
        assertThrows(IllegalStateException.class,
                () -> geometry.getMaxZ());
    }

    @Test
    public void allExtremaScanEveryCoordinateIncludingNegativeValues() {
        GeometryWrapper geometry = GeometryWrapper.extract("LINESTRING ZM (-9 8 -7 100, -2 3 -1 -100)", WKTDatatype.URI);
        assertEquals(-9, geometry.getMinX(), 0);
        assertEquals(-2, geometry.getMaxX(), 0);
        assertEquals(3, geometry.getMinY(), 0);
        assertEquals(8, geometry.getMaxY(), 0);
        assertEquals(-7, geometry.getMinZ(), 0);
        assertEquals(-1, geometry.getMaxZ(), 0);
    }

    @Test
    public void threeDimensionalExtremaFollowSrsDimensionOrder() {
        GeometryWrapper geometry = GeometryWrapper.extract(
            "<http://www.opengis.net/def/crs/EPSG/0/4979> LINESTRING Z (10 100 -4, 20 120 9)", WKTDatatype.URI);
        assertEquals(10, geometry.getMinX(), 0);
        assertEquals(20, geometry.getMaxX(), 0);
        assertEquals(100, geometry.getMinY(), 0);
        assertEquals(120, geometry.getMaxY(), 0);
        assertEquals(-4, geometry.getMinZ(), 0);
        assertEquals(9, geometry.getMaxZ(), 0);
    }

    @Test
    public void nestedCollectionsIncludeAllDescendantsAndSkipEmptyMembers() {
        var factory = CustomGeometryFactory.theInstance();
        Geometry first = GeometryWrapper.extract("POINT Z (-9 8 -7)", WKTDatatype.URI).getParsingGeometry();
        Geometry second = GeometryWrapper.extract("POINT Z (-2 3 -1)", WKTDatatype.URI).getParsingGeometry();
        Geometry nested = factory.createGeometryCollection(new Geometry[] { second, factory.createPoint() });
        GeometryWrapper geometry = wrapper(factory.createGeometryCollection(new Geometry[] { first, nested }),
                                           CoordinateSequenceDimensions.XYZ);
        assertEquals(-9, geometry.getMinX(), 0);
        assertEquals(-2, geometry.getMaxX(), 0);
        assertEquals(3, geometry.getMinY(), 0);
        assertEquals(8, geometry.getMaxY(), 0);
        assertEquals(-7, geometry.getMinZ(), 0);
        assertEquals(-1, geometry.getMaxZ(), 0);
    }

    @Test
    public void emptyGeometriesHaveNoExtrema() {
        for (String wkt : new String[] { "POINT Z EMPTY", "LINESTRING Z EMPTY", "POLYGON Z EMPTY",
                                         "MULTIPOINT Z EMPTY", "GEOMETRYCOLLECTION Z EMPTY" }) {
            GeometryWrapper geometry = GeometryWrapper.extract(wkt, WKTDatatype.URI);
            assertThrows(wkt, IllegalStateException.class, geometry::getMinX);
            assertThrows(wkt, IllegalStateException.class, geometry::getMaxX);
            assertThrows(wkt, IllegalStateException.class, geometry::getMinY);
            assertThrows(wkt, IllegalStateException.class, geometry::getMaxY);
            assertThrows(wkt, IllegalStateException.class, geometry::getMinZ);
            assertThrows(wkt, IllegalStateException.class, geometry::getMaxZ);
        }
    }

    @Test
    public void polygonZExtremaIncludeInteriorRings() {
        GeometryWrapper geometry = GeometryWrapper.extract(
            "POLYGON Z ((0 0 1, 10 0 2, 10 10 3, 0 10 4, 0 0 1), "
            + "(2 2 -9, 2 3 20, 3 2 6, 2 2 -9))", WKTDatatype.URI);
        assertEquals(0, geometry.getMinX(), 0);
        assertEquals(10, geometry.getMaxX(), 0);
        assertEquals(0, geometry.getMinY(), 0);
        assertEquals(10, geometry.getMaxY(), 0);
        assertEquals(-9, geometry.getMinZ(), 0);
        assertEquals(20, geometry.getMaxZ(), 0);
    }

    private static GeometryWrapper wrapper(Geometry geometry, CoordinateSequenceDimensions dimensions) {
        return new GeometryWrapper(geometry, SRS_URI.DEFAULT_WKT_CRS84, WKTDatatype.URI,
                new DimensionInfo(dimensions, geometry.getDimension()));
    }
}
