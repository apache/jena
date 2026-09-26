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
import static org.junit.Assert.assertTrue;

import org.apache.jena.geosparql.implementation.datatype.GMLDatatype;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.geosparql.implementation.jts.CoordinateSequenceDimensions;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

public class GeometryCentroidTest {
    private static final String EMPTY_GML_POLYGON_EPSG_4979 =
            "<gml:Polygon xmlns:gml=\"http://www.opengis.net/gml/3.2\" srsName=\"http://www.opengis.net/def/crs/EPSG/0/4979\"/>";
    private static final String GML_POINT_Z_EPSG_4979 =
            "<gml:Point xmlns:gml=\"http://www.opengis.net/gml/3.2\" srsName=\"http://www.opengis.net/def/crs/EPSG/0/4979\"><gml:pos>10 100 7</gml:pos></gml:Point>";

    @Test
    public void ordinaryJtsGeometryProducesASerializableXyPoint() {
        GeometryFactory factory = new GeometryFactory();
        Geometry line = factory.createLineString(new Coordinate[] { new Coordinate(0, 0, 7), new Coordinate(4, 0, 9) });
        GeometryWrapper result = new GeometryWrapper(line, WKTDatatype.URI).centroid();
        assertEquals(CoordinateSequenceDimensions.XY, result.getCoordinateSequenceDimensions());
        assertEquals(0, result.getTopologicalDimension());
        GeometryWrapper reparsed = GeometryWrapper.extract(result.asNodeValue());
        assertEquals(2, reparsed.getXYGeometry().getCoordinate().getX(), 0);
        assertEquals(0, reparsed.getXYGeometry().getCoordinate().getY(), 0);
    }

    @Test
    public void nestedCollectionUsesNonemptyHighestDimensionalMembers() {
        GeometryFactory factory = new GeometryFactory();
        Geometry line = factory.createLineString(new Coordinate[] { new Coordinate(0, 0), new Coordinate(4, 0) });
        Geometry nested = factory.createGeometryCollection(new Geometry[] { line, factory.createPolygon() });
        Geometry collection = factory.createGeometryCollection(new Geometry[] {
            factory.createPoint(new Coordinate(90, 90)), nested
        });
        GeometryWrapper source = new GeometryWrapper(collection, WKTDatatype.URI);
        GeometryWrapper result = source.centroid();
        assertEquals(2, result.getXYGeometry().getCoordinate().getX(), 0);
        assertEquals(0, result.getXYGeometry().getCoordinate().getY(), 0);
        assertEquals(2, source.getParsingGeometry().getNumGeometries());
    }

    @Test
    public void ordinaryJtsEmptyGeometryProducesASerializableEmptyPoint() {
        GeometryWrapper result = new GeometryWrapper(new GeometryFactory().createPolygon(), WKTDatatype.URI).centroid();
        assertEquals(CoordinateSequenceDimensions.XY, result.getCoordinateSequenceDimensions());
        assertEquals("Point", result.getGeometryType());
        assertTrue(GeometryWrapper.extract(result.asNodeValue()).isEmpty());
    }

    @Test
    public void emptyGmlInAThreeDimensionalCrsProducesAnEmptyPoint() {
        GeometryWrapper result = GeometryWrapper.extract(EMPTY_GML_POLYGON_EPSG_4979, GMLDatatype.URI).centroid();
        assertEquals(CoordinateSequenceDimensions.XY, result.getCoordinateSequenceDimensions());
        assertEquals("Point", result.getGeometryType());
        assertEquals(GMLDatatype.URI, result.getGeometryDatatypeURI());
        assertEquals("http://www.opengis.net/def/crs/EPSG/0/4979", result.getSrsURI());
        assertTrue(result.isEmpty());
    }

    @Test
    public void threeDimensionalGmlCentroidHasZeroHeightAndCanBeReparsed() {
        GeometryWrapper source = GeometryWrapper.extract(GML_POINT_Z_EPSG_4979, GMLDatatype.URI);
        GeometryWrapper result = source.centroid();
        assertEquals(GMLDatatype.URI, result.getGeometryDatatypeURI());
        assertEquals("http://www.opengis.net/def/crs/EPSG/0/4979", result.getSrsURI());
        assertEquals(CoordinateSequenceDimensions.XYZ, result.getCoordinateSequenceDimensions());
        assertEquals(DimensionInfo.XYZ_POINT, result.getDimensionInfo());
        assertEquals(10, result.getParsingGeometry().getCoordinate().getX(), 0);
        assertEquals(100, result.getParsingGeometry().getCoordinate().getY(), 0);
        assertEquals(0, result.getParsingGeometry().getCoordinate().getZ(), 0);

        GeometryWrapper reparsed = GeometryWrapper.extract(result.asNodeValue());
        assertEquals(result.getSrsURI(), reparsed.getSrsURI());
        assertEquals(DimensionInfo.XYZ_POINT, reparsed.getDimensionInfo());
        assertEquals(10, reparsed.getParsingGeometry().getCoordinate().getX(), 0);
        assertEquals(100, reparsed.getParsingGeometry().getCoordinate().getY(), 0);
        assertEquals(0, reparsed.getParsingGeometry().getCoordinate().getZ(), 0);
    }
}
