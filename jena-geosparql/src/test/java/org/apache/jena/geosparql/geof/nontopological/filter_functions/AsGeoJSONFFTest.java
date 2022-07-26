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
package org.apache.jena.geosparql.geof.nontopological.filter_functions;

import org.apache.jena.geosparql.implementation.datatype.GMLDatatype;
import org.apache.jena.geosparql.implementation.datatype.GeoJSONDatatype;
import org.apache.jena.sparql.expr.NodeValue;
import org.junit.*;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;

import static org.junit.Assert.assertTrue;

public class AsGeoJSONFFTest {
    private static final double TOLERANCE = 0.0001;

    /**
     * Test of AsGeoJSON
     */
    @Test
    public void testExec() throws ParseException {
        NodeValue v = NodeValue.makeNode("<gml:LineString xmlns:gml=\"http://www.opengis.net/gml/3.2\" srsName=\"EPSG:25832\">" +
                        "<gml:posList>" +
                        "663957.75944074022118 5103981.64908889029175 663955.915655555087142 5103991.151674075052142" +
                        "</gml:posList>" +
                        "</gml:LineString>", GMLDatatype.INSTANCE);
        AsGeoJSONFF instance = new AsGeoJSONFF();
        NodeValue result = instance.exec(v);
        // "{\"type\":\"LineString\",\"coordinates\":[[11.120116,46.069743],[11.120095,46.069829]]}"^^<http://www.opengis.net/ont/geosparql#geoJSONLiteral>
        NodeValue expected = NodeValue.makeNode("{\"type\":\"LineString\",\"coordinates\":[[11.12011,46.06974],[11.12009,46.06982]]}",
                GeoJSONDatatype.INSTANCE);

        GeometryFactory geometryFactory = new GeometryFactory();
        GeoJsonReader reader = new GeoJsonReader(geometryFactory);
        LineString expectedGeo = (LineString) reader.read(expected.asString());
        LineString actualGeo = (LineString) reader.read(result.asString());
        assertTrue("\n" +
                "Expected :" + String.valueOf(expectedGeo) + "\n" +
                "Actual   :" + String.valueOf(actualGeo) + "\n" +
                "Tolerance:" + String.valueOf(TOLERANCE), expectedGeo.equalsExact(actualGeo, TOLERANCE));
    }
}
