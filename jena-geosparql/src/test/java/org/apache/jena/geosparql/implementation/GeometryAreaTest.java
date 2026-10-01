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
import org.apache.jena.geosparql.implementation.datatype.GMLDatatype;
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
import static org.junit.Assert.assertTrue;

public class GeometryAreaTest {
    private static final String EPSG_32634 = "http://www.opengis.net/def/crs/EPSG/0/32634";
    private static final String EPSG_2227 = "http://www.opengis.net/def/crs/EPSG/0/2227";
    private static final String EPSG_4326 = "http://www.opengis.net/def/crs/EPSG/0/4326";
    private static final String EPSG_4267 = "http://www.opengis.net/def/crs/EPSG/0/4267";
    private static final String EPSG_4275 = "http://www.opengis.net/def/crs/EPSG/0/4275";
    private static final String EPSG_4807 = "http://www.opengis.net/def/crs/EPSG/0/4807";
    private static final String EPSG_4979 = "http://www.opengis.net/def/crs/EPSG/0/4979";
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
    public void polygonHolesAreExcluded() {
        GeometryWrapper polygon = geometry("<" + EPSG_32634 + "> POLYGON("
                + "(0 0,10 0,10 10,0 10,0 0),"
                + "(4 4,6 4,6 6,4 6,4 4))");

        assertEquals(96.0, polygon.area(), 0.0);
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
    public void nonPolygonTypesReturnZeroWithoutSourceCrsCalculation() {
        for (String wkt : List.of(
                "POINT(1 1)",
                "LINESTRING(0 0,1 1)",
                "MULTIPOINT((0 0),(1 1))",
                "MULTILINESTRING((0 0,1 1),(2 2,3 3))",
                "MULTIPOLYGON(((0 0,2 0,2 2,0 2,0 0)))",
                "GEOMETRYCOLLECTION(POINT(1 1),LINESTRING(0 0,1 1))",
                "GEOMETRYCOLLECTION(POLYGON EMPTY,POINT(1 1))",
                "GEOMETRYCOLLECTION(POLYGON((0 0,2 0,2 2,0 2,0 0)))")) {
            assertEquals(0.0, geometry(wkt).area(), 0.0);
            assertEquals(0.0, geometry("<" + EPSG_32634 + "> " + wkt).area(), 0.0);
        }
    }

    @Test
    public void nonFiniteXYCoordinatesAreRejected() {
        for (String wkt : List.of(
                "LINESTRING(0 0,1e309 1)",
                "<" + EPSG_32634 + "> POLYGON((0 0,1 0,1e309 1,0 1,0 0))")) {
            assertThrows(UnitsConversionException.class, () -> geometry(wkt).area());
        }

        String gml = """
            <gml:Polygon xmlns:gml="http://www.opengis.net/gml/3.2"
                srsName="http://www.opengis.net/def/crs/EPSG/0/32634">
              <gml:exterior><gml:LinearRing><gml:posList>0 0 1 0 NaN 1 0 1 0 0</gml:posList></gml:LinearRing></gml:exterior>
            </gml:Polygon>
            """;
        GeometryWrapper polygon = GeometryWrapper.extract(gml, GMLDatatype.URI);
        assertThrows(UnitsConversionException.class, polygon::area);
    }

    @Test
    public void nonFinitePlanarAreaResultIsRejected() {
        GeometryWrapper polygon = geometry("<" + EPSG_32634 + "> POLYGON(("
                + "0 0,1e200 0,1e200 1e200,0 1e200,0 0))");

        assertThrows(UnitsConversionException.class, polygon::area);
    }

    @Test
    public void areaOverflowDuringUnitConversionIsRejected() {
        GeometryWrapper polygon = geometry("<" + EPSG_32634 + "> POLYGON(("
                + "0 0,2e151 0,2e151 2e151,0 2e151,0 0))");

        assertTrue(Double.isFinite(polygon.area()));
        assertThrows(UnitsConversionException.class,
                () -> polygon.area(Unit_URI.SQUARE_MILLIMETRE_QUDT));
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
    public void eligibleAreaRejectsNonCartesianHorizontalAxes() throws Exception {
        GeometryWrapper polygon = geometryWithCrs(
                "<" + EPSG_32634 + "> POLYGON((0 0,1 0,1 1,0 1,0 0))",
                CRS.fromWKT("ENGCRS[\"Skewed axes\","
                        + "EDATUM[\"Engineering datum\"],CS[affine,2],"
                        + "AXIS[\"x\",east,ORDER[1],LENGTHUNIT[\"metre\",1]],"
                        + "AXIS[\"y\",northEast,ORDER[2],LENGTHUNIT[\"metre\",1]]]"));

        UnitsConversionException error = assertThrows(UnitsConversionException.class, polygon::area);
        assertTrue(error.getMessage().contains("Cartesian"));
    }

    @Test
    public void cartesianEngineeringCrsStillSupportsArea() throws Exception {
        GeometryWrapper polygon = geometryWithCrs(
                "<" + EPSG_32634 + "> POLYGON((0 0,1 0,1 1,0 1,0 0))",
                CRS.fromWKT("ENGCRS[\"Cartesian engineering grid\","
                        + "EDATUM[\"Engineering datum\"],CS[Cartesian,2],"
                        + "AXIS[\"x\",east,ORDER[1],LENGTHUNIT[\"metre\",1]],"
                        + "AXIS[\"y\",north,ORDER[2],LENGTHUNIT[\"metre\",1]]]"));

        assertEquals(1.0, polygon.area(), 0.0);
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
    public void compoundCrsUsesItsTwoDimensionalHorizontalAxisUnits() throws Exception {
        CoordinateReferenceSystem compoundCrs = CRS.compound(
                CRS.forCode(EPSG_32634), CRS.forCode("EPSG:5703"));
        GeometryWrapper polygon = geometryWithCrs(
                "<" + EPSG_32634 + "> POLYGON Z ((0 0 5,0 10 5,10 10 5,10 0 5,0 0 5))",
                compoundCrs);

        assertEquals(100.0, polygon.area(), 0.0);
    }

    @Test
    public void geographicAreaUsesTheDeclaredAxisOrderAndConvertsUnits() {
        GeometryWrapper crs84 = geometry("POLYGON((0 0,2 0,2 1,0 1,0 0))");
        GeometryWrapper epsg4326 = geometry("<" + EPSG_4326 + "> POLYGON(("
                + "0 0,0 2,1 2,1 0,0 0))");

        double squareMetres = crs84.area();
        assertTrue(squareMetres > 24_000_000_000.0 && squareMetres < 25_000_000_000.0);
        assertEquals(squareMetres, epsg4326.area(), squareMetres * 1e-8);
        assertEquals(squareMetres / 1_000_000, crs84.area(Unit_URI.SQUARE_KILOMETRE_QUDT), 1e-6);
    }

    @Test
    public void geographicPolygonExcludesHoles() {
        String square = "POLYGON((0 0,1 0,1 1,0 1,0 0))";
        GeometryWrapper polygon = geometry(square);
        GeometryWrapper withHole = geometry("POLYGON((0 0,1 0,1 1,0 1,0 0),"
                + "(0.25 0.25,0.25 0.75,0.75 0.75,0.75 0.25,0.25 0.25))");

        // WGS84 geodesic reference areas from pyproj.Geod.polygon_area_perimeter.
        assertEquals(12_308_778_361.469, polygon.area(), 100_000.0);
        assertEquals(9_231_614_224.815, withHole.area(), 100_000.0);
    }

    @Test
    public void geographicAreaSupportsDatelineAndPolarPolygons() {
        double dateline = geometry("POLYGON((179 10,-179 10,-179 11,179 11,179 10))").area();
        double polar = geometry("POLYGON((-90 80,0 80,90 80,180 80,-90 80))").area();

        // WGS84 geodesic references from pyproj.Geod.polygon_area_perimeter;
        // tolerances allow for projected-edge approximation.
        assertEquals(24_218_606_783.655, dateline, 250_000.0);
        assertEquals(2_507_270_031_169.875, polar, 2_500_000.0);
    }

    @Test
    public void geographicAreaUsesTheSourceDatum() {
        String square = "POLYGON((0 0,1 0,1 1,0 1,0 0))";
        double wgs84 = geometry(square).area();
        double nad27 = geometry("<" + EPSG_4267 + "> " + square).area();

        assertTrue(nad27 > 0 && Double.isFinite(nad27));
        assertTrue(Math.abs(nad27 - wgs84) > 10_000.0);
    }

    @Test
    public void parisPrimeMeridianAndGradUnitsMatchGreenwichDegrees() {
        // NTF (Paris) uses grads and the Paris meridian; NTF uses degrees and Greenwich.
        GeometryWrapper paris = geometry("<" + EPSG_4807 + "> POLYGON(("
                + "48 0,48 1,49 1,49 0,48 0))");
        GeometryWrapper greenwich = geometry("<" + EPSG_4275 + "> POLYGON(("
                + "43.2 2.33722917,43.2 3.23722917,44.1 3.23722917,"
                + "44.1 2.33722917,43.2 2.33722917))");

        double area = greenwich.area();
        assertTrue(area > 0);
        assertEquals(area, paris.area(), area * 1e-5);
    }

    @Test
    public void geographicThreeDimensionalCrsIgnoresHeight() {
        GeometryWrapper twoDimensional = geometry("<" + EPSG_4326 + "> POLYGON(("
                + "0 0,0 2,1 2,1 0,0 0))");
        GeometryWrapper threeDimensional = geometry("<" + EPSG_4979 + "> POLYGON Z (("
                + "0 0 10,0 2 20,1 2 30,1 0 40,0 0 10))");

        assertEquals(twoDimensional.area(), threeDimensional.area(), twoDimensional.area() * 1e-8);
    }

    @Test
    public void compoundGeographicCrsUsesItsHorizontalComponent() throws Exception {
        CoordinateReferenceSystem compoundCrs = CRS.compound(
                CRS.forCode(EPSG_4326), CRS.forCode("EPSG:5703"));
        GeometryWrapper polygon = geometryWithCrs("<" + EPSG_32634 + "> POLYGON Z (("
                + "0 0 5,0 1 5,1 1 5,1 0 5,0 0 5))", compoundCrs);

        assertEquals(geometry("POLYGON((0 0,1 0,1 1,0 1,0 0))").area(),
                polygon.area(), 100_000.0);
    }

    @Test
    public void geographicAreaRejectsPolygonBeyondOneLocalHemisphere() {
        GeometryWrapper wide = geometry("POLYGON((0 0,179.9 0,179.9 1,0 1,0 0))");

        assertThrows(UnitsConversionException.class, wide::area);
    }

    @Test
    public void geographicAreaRejectsUnknownCrsAndDisabledTransformation() {
        GeometryWrapper unknown = geometry("<http://example.com/crs/unknown> "
                + "POLYGON((0 0,1 0,1 1,0 1,0 0))");
        assertThrows(UnitsConversionException.class, unknown::area);

        GeoSPARQLConfig.allowGeometrySRSTransformation(false);
        try {
            assertThrows(UnitsConversionException.class,
                    () -> geometry("POLYGON((0 0,1 0,1 1,0 1,0 0))").area());
        } finally {
            GeoSPARQLConfig.allowGeometrySRSTransformation(true);
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
