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

import org.apache.jena.geosparql.configuration.GeoSPARQLConfig;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.geosparql.implementation.registry.UnitsURIException;
import org.apache.jena.geosparql.implementation.vocabulary.Unit_URI;
import org.apache.sis.referencing.CRS;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class GeometryAreaTest {
    private static final String EPSG_32634 = "http://www.opengis.net/def/crs/EPSG/0/32634";
    private static final String EPSG_2227 = "http://www.opengis.net/def/crs/EPSG/0/2227";
    private static final String UNKNOWN_UNIT = "http://example.com/unit/unknown";

    @BeforeClass
    public static void initializeJena() {
        GeoSPARQLConfig.setupNoIndex();
    }

    @Test
    public void polygonAreaUsesTheExplicitTargetAreaUnit() {
        GeometryWrapper polygon = geometry("<" + EPSG_32634 + "> POLYGON(("
                + "500000 4600000,501000 4600000,501000 4601000,"
                + "500000 4601000,500000 4600000))");

        assertEquals(1.0, polygon.area(Unit_URI.SQUARE_KILOMETRE_QUDT), 0.0);
    }

    @Test
    public void areaAccumulatesMultiPolygonShellsAndHoles() {
        GeometryWrapper multiPolygon = geometry("<" + EPSG_32634 + "> MULTIPOLYGON("
                + "((500000 4600000,500010 4600000,500010 4600010,"
                + "500000 4600010,500000 4600000),"
                + "(500004 4600004,500006 4600004,500006 4600006,"
                + "500004 4600006,500004 4600004)),"
                + "((500020 4600000,500023 4600000,500023 4600004,"
                + "500020 4600004,500020 4600000)))");

        assertEquals(108.0, multiPolygon.area(), 0.0);
    }

    @Test
    public void emptyGeometriesReturnZeroRegardlessOfCrs() {
        for (String type : List.of("POINT", "LINESTRING", "POLYGON", "MULTIPOINT",
                "MULTILINESTRING", "MULTIPOLYGON", "GEOMETRYCOLLECTION")) {
            assertEquals(0.0, geometry(type + " EMPTY").area(), 0.0);
            assertEquals(0.0, geometry("<" + EPSG_32634 + "> " + type + " EMPTY").area(), 0.0);
        }
    }

    @Test
    public void zAndMDoNotContributeToArea() {
        for (String wkt : List.of(
                "POLYGON Z ((0 0 1, 3 0 2, 3 4 3, 0 4 4, 0 0 1))",
                "POLYGON M ((0 0 1, 3 0 2, 3 4 3, 0 4 4, 0 0 1))",
                "POLYGON ZM ((0 0 1 9, 3 0 2 8, 3 4 3 7, 0 4 4 6, 0 0 1 9))")) {
            assertEquals(12.0, geometry("<" + EPSG_32634 + "> " + wkt).area(), 0.0);
        }
    }

    @Test
    public void areaIneligibleGeometryTypesReturnZeroWithoutSourceCrsCalculation() {
        for (String wkt : List.of(
                "POINT(1 1)",
                "LINESTRING(0 0,1 1)",
                "MULTIPOINT((0 0),(1 1))",
                "MULTILINESTRING((0 0,1 1),(2 2,3 3))",
                "GEOMETRYCOLLECTION(POLYGON((0 0,0 1,1 1,1 0,0 0)))")) {
            assertEquals(0.0, geometry(wkt).area(), 0.0);
        }
    }

    @Test
    public void eligibleAreaRejectsDifferentHorizontalAxisScales() throws Exception {
        GeometryWrapper polygon = geometryWithCrs(
                "<" + EPSG_32634 + "> POLYGON((0 0,0 10,10 10,10 0,0 0))",
                CRS.fromWKT("ENGCRS[\"Mixed axis scale\","
                        + "EDATUM[\"Engineering datum\"],CS[Cartesian,2],"
                        + "AXIS[\"x\",east,ORDER[1],LENGTHUNIT[\"metre\",1]],"
                        + "AXIS[\"y\",north,ORDER[2],LENGTHUNIT[\"foot\",0.3048]]]"));

        assertThrows(UnitsConversionException.class,
                polygon::area);
    }

    @Test
    public void projectedNonMetreAreaConvertsTheSourceAreaQuantity() {
        GeometryWrapper polygon = geometry("<" + EPSG_2227 + "> POLYGON(("
                + "6300000 2000000,6300003 2000000,6300003 2000004,"
                + "6300000 2000004,6300000 2000000))");

        assertEquals(1.114840939359298,
                polygon.area(), 1e-12);
    }

    @Test
    public void multiPolygonAreaIsAccumulatedBeforeUnitConversion() {
        GeometryWrapper multiPolygon = geometry("<" + EPSG_32634 + "> MULTIPOLYGON("
                + "((0 0,0.02 0,0.02 0.02,0 0.02,0 0)),"
                + "((1 0,1.02 0,1.02 0.02,1 0.02,1 0)))");

        assertEquals(0.0000000008,
                multiPolygon.area(Unit_URI.SQUARE_KILOMETRE_QUDT), 1e-20);
    }

    @Test
    public void compoundCrsUsesItsTwoDimensionalHorizontalAxisUnits() throws Exception {
        CoordinateReferenceSystem compoundCrs = CRS.compound(
                CRS.forCode(EPSG_32634), CRS.forCode("EPSG:5703"));
        GeometryWrapper polygon = geometryWithCrs(
                "<" + EPSG_32634 + "> POLYGON Z ((0 0 5,0 10 5,10 10 5,10 0 5,0 0 5))",
                compoundCrs);

        assertEquals(100.0, polygon.area(), 0.0);
    }

    @Test
    public void eligibleGeographicAreaIsRejected() {
        for (String wkt : List.of(
                "POLYGON((0 0,0 1,1 1,1 0,0 0))",
                "MULTIPOLYGON(((0 0,0 1,1 1,1 0,0 0)))")) {
            assertThrows(UnitsConversionException.class, geometry(wkt)::area);
        }
    }

    @Test
    public void emptyEligibleAreaSkipsSourceAxisValidation() throws Exception {
        CoordinateReferenceSystem mixedScaleCrs = CRS.fromWKT("ENGCRS[\"Mixed axis scale\","
                + "EDATUM[\"Engineering datum\"],CS[Cartesian,2],"
                + "AXIS[\"x\",east,ORDER[1],LENGTHUNIT[\"metre\",1]],"
                + "AXIS[\"y\",north,ORDER[2],LENGTHUNIT[\"foot\",0.3048]]]");

        assertEquals(0.0, geometryWithCrs(
                "<" + EPSG_32634 + "> POLYGON EMPTY", mixedScaleCrs).area(), 0.0);
        assertEquals(0.0, geometryWithCrs(
                "<" + EPSG_32634 + "> MULTIPOLYGON EMPTY", mixedScaleCrs).area(), 0.0);
    }

    @Test
    public void zeroAreaStillValidatesTargetUnits() {
        for (GeometryWrapper geometry : List.of(
                geometry("POINT(1 1)"),
                geometry("<" + EPSG_32634 + "> POLYGON EMPTY"))) {
            assertThrows(UnitsConversionException.class,
                    () -> geometry.area(Unit_URI.DEGREE_URL));
            assertThrows(UnitsURIException.class,
                    () -> geometry.area(UNKNOWN_UNIT));
        }
    }

    private GeometryWrapper geometry(String wkt) {
        return GeometryWrapper.extract(wkt, WKTDatatype.URI);
    }

    private GeometryWrapper geometryWithCrs(String wkt, CoordinateReferenceSystem crs) {
        return new GeometryWrapperWithSrsInfo(geometry(wkt), new TestSrsInfo(crs));
    }

    private static final class GeometryWrapperWithSrsInfo extends GeometryWrapper {
        private final SRSInfo srsInfo;

        private GeometryWrapperWithSrsInfo(GeometryWrapper geometry, SRSInfo srsInfo) {
            super(geometry);
            this.srsInfo = srsInfo;
        }

        @Override
        public SRSInfo getSrsInfo() {
            return srsInfo;
        }
    }

    private static final class TestSrsInfo extends SRSInfo {
        private final CoordinateReferenceSystem crs;

        private TestSrsInfo(CoordinateReferenceSystem crs) {
            super(EPSG_32634);
            this.crs = crs;
        }

        @Override
        public CoordinateReferenceSystem getCrs() {
            return crs;
        }
    }
}
