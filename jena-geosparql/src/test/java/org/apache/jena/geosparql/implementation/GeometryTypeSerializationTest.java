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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.geosparql.implementation.datatype.GeometryDatatype;
import org.apache.jena.geosparql.implementation.datatype.GMLDatatype;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.junit.Test;

public class GeometryTypeSerializationTest {
    private static final String GML = "http://www.opengis.net/ont/gml#";
    private static final String SF = "http://www.opengis.net/ont/sf#";

    @Test
    public void parsedTypeQueriesDoNotReadGmlAgain() {
        CountingGeometryWrapper geometry = new CountingGeometryWrapper(curve());
        assertEquals(GML + "Curve", type(geometry));
        assertEquals(GML + "Curve", type(geometry));
        assertEquals(0, geometry.lexicalReads);
        assertEquals(0, geometry.sourceReads);
    }

    @Test
    public void derivedGeometryDoesNotInheritTheCachedSourceType() {
        GeometryWrapper source = curve();
        assertEquals(GML + "Curve", type(source));
        assertEquals(GML + "Polygon", type(source.envelope()));
        assertEquals(GML + "Curve", type(source));
    }

    @Test
    public void copyRetainsSourceSubtypeWithDefaultXmlNamespace() {
        GeometryWrapper source = curve();
        assertEquals(GML + "Curve", type(source));
        assertEquals(GML + "Curve", type(new GeometryWrapper(source)));
    }

    @Test
    public void conversionToWktUsesTheConvertedTypeWithoutChangingSource() {
        GeometryWrapper source = curve();
        assertEquals(GML + "Curve", type(source));
        GeometryWrapper converted = GeometryWrapper.extract(source.asLiteral(WKTDatatype.URI));
        assertEquals(SF + "LineString", type(converted));
        assertEquals(GML + "Curve", type(source));
    }

    @Test
    public void constructedGmlUsesWriterElementNames() {
        String[][] cases = {
            { "POINT (1 2)", "Point" },
            { "LINESTRING (0 0, 1 1)", "LineString" },
            { "POLYGON ((0 0, 1 0, 0 1, 0 0))", "Polygon" },
            { "MULTIPOINT ((1 2), (3 4))", "MultiPoint" },
            { "MULTILINESTRING ((0 0, 1 1))", "MultiCurve" },
            { "MULTIPOLYGON (((0 0, 1 0, 0 1, 0 0)))", "MultiSurface" },
            { "GEOMETRYCOLLECTION (POINT (1 2))", "MultiGeometry" },
            { "MULTIPOLYGON EMPTY", "MultiSurface" }
        };
        for (String[] example : cases) {
            GeometryWrapper wkt = GeometryWrapper.extract(example[0], WKTDatatype.URI);
            GeometryWrapper constructed = new GeometryWrapper(wkt.getParsingGeometry(), wkt.getSrsURI(),
                                                               GMLDatatype.URI, wkt.getDimensionInfo());
            assertEquals(example[0], GML + example[1], type(constructed));
            assertEquals(example[0], GML + example[1], type(GeometryWrapper.extract(constructed.asLiteral())));
            GeometryWrapper serializedFirst = new GeometryWrapper(wkt.getParsingGeometry(), wkt.getSrsURI(),
                                                                  GMLDatatype.URI, wkt.getDimensionInfo());
            serializedFirst.asLiteral();
            assertNull(serializedFirst.getSourceLexicalForm());
            assertEquals(example[0], GML + example[1], type(serializedFirst));
        }
    }

    @Test
    public void emptyGmlTextUsesJenasPointInterpretation() {
        assertEquals(GML + "Point", type(GeometryWrapper.extract("", GMLDatatype.URI)));
    }

    @Test
    public void constructedTypeIsMemoizedAndCopied() {
        GeometryWrapper wkt = WKTDatatype.INSTANCE.read("MULTILINESTRING ((0 0, 1 1))");
        CountingGeometryWrapper geometry = new CountingGeometryWrapper(new GeometryWrapper(
            wkt.getParsingGeometry(), wkt.getSrsURI(), GMLDatatype.URI, wkt.getDimensionInfo()));
        assertEquals(GML + "MultiCurve", type(geometry));
        assertEquals(GML + "MultiCurve", type(geometry));
        assertEquals(1, geometry.sourceReads);
        assertEquals(0, geometry.lexicalReads);
        CountingGeometryWrapper copy = new CountingGeometryWrapper(geometry);
        assertEquals(GML + "MultiCurve", type(copy));
        assertEquals(0, copy.sourceReads);
        assertEquals(0, copy.lexicalReads);
    }

    @Test
    public void publicLexicalConstructorPreservesSourceSubtype() {
        GeometryWrapper source = curve();
        GeometryWrapper geometry = new GeometryWrapper(source.getParsingGeometry(), source.getSrsURI(),
            GMLDatatype.URI, source.getDimensionInfo(), source.getLexicalForm());
        geometry.asLiteral();
        assertEquals(GML + "Curve", type(geometry));
        assertEquals(GML + "Curve", type(new GeometryWrapper(geometry)));
    }

    @Test
    public void protectedLexicalConstructorPreservesSourceSubtype() {
        GeometryWrapper source = curve();
        GeometryWrapper geometry = new GeometryWrapper(source.getParsingGeometry(), source.getXYGeometry(),
            source.getSrsURI(), GMLDatatype.URI, source.getDimensionInfo(), source.getLexicalForm());
        assertEquals(GML + "Curve", type(new GeometryWrapper(geometry)));
    }

    @Test
    public void constructedGmlRejectsTypesTheWriterCannotRepresent() {
        GeometryWrapper ring = WKTDatatype.INSTANCE.read("LINEARRING (0 0, 1 0, 1 1, 0 0)");
        GeometryWrapper gml = new GeometryWrapper(ring.getParsingGeometry(), ring.getSrsURI(),
                                                  GMLDatatype.URI, ring.getDimensionInfo());
        assertThrows(DatatypeFormatException.class, gml::getGeometryTypeURI);
        assertThrows(DatatypeFormatException.class, gml::asLiteral);
    }

    @Test
    public void unsupportedSimpleFeaturesTypeRaisesDatatypeError() {
        GeometryWrapper geometry = new GeometryWrapper(WKTDatatype.INSTANCE.read("POINT (1 2)")) {
            @Override
            public String getGeometryType() {
                return "UnsupportedGeometry";
            }
        };
        assertThrows(DatatypeFormatException.class, geometry::getGeometryTypeURI);
    }

    @Test
    public void datatypeExtensionsWithoutTypeResolutionRaiseDatatypeError() {
        GeometryDatatype datatype = new GeometryDatatype("urn:test:geometry-type:default") {
            @Override
            public GeometryWrapper read(String text) {
                GeometryWrapper wkt = WKTDatatype.INSTANCE.read(text);
                return new GeometryWrapper(wkt.getParsingGeometry(), wkt.getSrsURI(), getURI(), wkt.getDimensionInfo());
            }
        };
        TypeMapper.getInstance().registerDatatype(datatype);
        try {
            GeometryWrapper geometry = datatype.read("POINT (1 2)");
            assertThrows(DatatypeFormatException.class, geometry::getGeometryTypeURI);
        } finally {
            TypeMapper.getInstance().unregisterDatatype(datatype);
        }
    }

    @Test
    public void datatypeExtensionsCanResolveTheirOwnTypes() {
        GeometryDatatype datatype = new GeometryDatatype("urn:test:geometry-type:custom") {
            @Override
            public GeometryWrapper read(String text) {
                GeometryWrapper wkt = WKTDatatype.INSTANCE.read(text);
                return new GeometryWrapper(wkt.getParsingGeometry(), wkt.getSrsURI(), getURI(), wkt.getDimensionInfo());
            }

            @Override
            public String getGeometryTypeURI(GeometryWrapper geometry) {
                return "urn:test:Point";
            }
        };
        TypeMapper.getInstance().registerDatatype(datatype);
        try {
            assertEquals("urn:test:Point", type(datatype.read("POINT (1 2)")));
        } finally {
            TypeMapper.getInstance().unregisterDatatype(datatype);
        }
    }

    private static GeometryWrapper curve() {
        return GMLDatatype.INSTANCE.read("""
            <Curve xmlns="http://www.opengis.net/gml/3.2"
                   srsName="http://www.opengis.net/def/crs/EPSG/0/27700">
              <segments><LineStringSegment><posList>0 0 10 10 20 0</posList></LineStringSegment></segments>
            </Curve>
            """);
    }

    private static String type(GeometryWrapper geometry) {
        return geometry.getGeometryTypeURI();
    }

    private static class CountingGeometryWrapper extends GeometryWrapper {
        private int lexicalReads;
        private int sourceReads;

        CountingGeometryWrapper(GeometryWrapper geometry) {
            super(geometry);
        }

        @Override
        public String getSourceLexicalForm() {
            sourceReads++;
            return super.getSourceLexicalForm();
        }

        @Override
        public String getLexicalForm() {
            lexicalReads++;
            return super.getLexicalForm();
        }
    }
}
