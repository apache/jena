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
package org.apache.jena.geosparql.geo.topological.property_functions.simple_features;

import static org.junit.Assert.assertEquals;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.sse.SSE;
import org.junit.Test;

/** Miscellaneous SPARQL-based tests across the simple feature family of property functions. */
public class SfPFMiscSparqlTest {

    @Test
    public void test01() {
        String query = """
            PREFIX geo: <http://www.opengis.net/ont/geosparql#>
            PREFIX ogcsf: <http://www.opengis.net/ont/sf#>
            SELECT * {
              ?s a ogcsf:Point .
              ?s geo:sfWithin <urn:test:geosparql#geoFrance> .
            } ORDER BY ?s
        """;

        DatasetGraph dsg = createTestDataFrance();
        Table actual = QueryExec.dataset(dsg).query(query).table();
        Table expected = SSE.parseTable("(table (row (?s <urn:test:geosparql#geoStrasbourg>) ))");
        assertEquals(expected, actual);
    }

    @Test
    public void test02() {
        String query = """
            PREFIX geo: <http://www.opengis.net/ont/geosparql#>
            SELECT * {
              ?s geo:sfWithin <urn:test:geosparql#geoFrance> .
            } ORDER BY ?s
        """;

        // Note: sfWithin is reflexive so 'geoFrance' is really expected as a result.
        DatasetGraph dsg = createTestDataFrance();
        Table actual = QueryExec.dataset(dsg).query(query).table();
        Table expected = SSE.parseTable("(table (row (?s <urn:test:geosparql#geoFrance>) ) (row (?s <urn:test:geosparql#geoStrasbourg>) ))");
        assertEquals(expected, actual);
    }

    // Test data derived from GH-3473.
    private static DatasetGraph createTestDataFrance() {
        String data = """
            PREFIX : <urn:test:geosparql#>
            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
            PREFIX geo: <http://www.opengis.net/ont/geosparql#>
            PREFIX ogcsf: <http://www.opengis.net/ont/sf#>

            :France
                rdfs:label "France";
                geo:hasGeometry :geoFrance.

            :geoFrance a ogcsf:Polygon ;
                # This is a bounding box of France.
                geo:asWKT "POLYGON((-4.9423 41.3247, -4.9423 51.1496, 10.02105 51.1496, 10.0210 41.3247, -4.9423 41.3247))"^^geo:wktLiteral .

            :Strasbourg
                rdfs:label "Strasbourg";
                geo:hasGeometry :geoStrasbourg.

            :geoStrasbourg a ogcsf:Point ;
                geo:asWKT "POINT(7.7510 48.5819)"^^geo:wktLiteral .

            # This point is outside of France's BBOX.
            :Berlin
                rdfs:label "Berlin";
                geo:hasGeometry :geoBerlin.

            :geoBerlin a ogcsf:Point ;
                geo:asWKT "POINT (13.4050 52.5200)"^^geo:wktLiteral .
        """;
        return RDFParser.create().fromString(data).lang(Lang.TRIG).toDatasetGraph();
    }
}
