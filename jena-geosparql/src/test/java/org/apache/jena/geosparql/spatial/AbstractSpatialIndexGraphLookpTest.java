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
package org.apache.jena.geosparql.spatial;

import org.apache.jena.geosparql.implementation.vocabulary.SRS_URI;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParserBuilder;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.exec.QueryExec;
import org.junit.Assert;
import org.junit.Test;

public abstract class AbstractSpatialIndexGraphLookpTest {
    protected abstract SpatialIndex buildSpatialIndex(DatasetGraph dsg, String srsUri) throws SpatialIndexException;

    private static boolean enableDebugPrint = false;

    private static void debugPrint(Table table) {
        if (enableDebugPrint) {
            System.err.println(ResultSetFormatter.asText(ResultSet.adapt(table.toRowSet())));
        }
    }

    // SpatialIndexUtils.buildSpatialIndex(dsg, SRS_URI.DEFAULT_WKT_CRS84);
    @Test
    public void mustNotMatchDefaultGraph1() throws SpatialIndexException {
        DatasetGraph dsg = RDFParserBuilder.create().fromString( """
                PREFIX eg: <http://www.example.org/>
                PREFIX geo: <http://www.opengis.net/ont/geosparql#>

                eg:graph1 {
                  eg:feature1 geo:hasGeometry eg:geometry1 .
                  eg:geometry1 geo:asWKT "POINT (0.3 0.3)"^^geo:wktLiteral .
                }

                eg:graph2 {
                  eg:feature1 geo:hasGeometry eg:geometry1 .
                  eg:geometry1 geo:asWKT "POINT (0.7 0.7)"^^geo:wktLiteral .
                }
            """).lang(Lang.TRIG).toDatasetGraph();

        String queryStr = """
                PREFIX eg: <http://www.example.org/>
                PREFIX spatial: <http://jena.apache.org/spatial#>
                PREFIX geo: <http://www.opengis.net/ont/geosparql#>

                SELECT * {
                  VALUES ?search {
                    "POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0))"^^geo:wktLiteral
                  }
                  LATERAL {
                    # GRAPH eg:graph1 { ?feature spatial:intersectBoxGeom(?search) . }
                    ?feature spatial:intersectBoxGeom(?search) .
                  }
                }
            """;

        buildSpatialIndex(dsg, SRS_URI.DEFAULT_WKT_CRS84);
        Table table = QueryExec.dataset(dsg).query(queryStr).table();
        debugPrint(table);
        Assert.assertTrue(table.isEmpty());
    }

    @Test
    public void mustNotMatchDefaultGraph2() throws SpatialIndexException {
        DatasetGraph dsg = RDFParserBuilder.create().fromString( """
                PREFIX eg: <http://www.example.org/>
                PREFIX geo: <http://www.opengis.net/ont/geosparql#>

                # Feature in default graph is outside of query polygon
                eg:feature1 geo:hasGeometry eg:geometry1 .
                eg:geometry1 geo:asWKT "POINT (-10 -10)"^^geo:wktLiteral .

                eg:graph1 {
                  eg:feature1 geo:hasGeometry eg:geometry1 .
                  eg:geometry1 geo:asWKT "POINT (0.3 0.3)"^^geo:wktLiteral .
                }

                eg:graph2 {
                  eg:feature1 geo:hasGeometry eg:geometry1 .
                  eg:geometry1 geo:asWKT "POINT (0.7 0.7)"^^geo:wktLiteral .
                }
            """).lang(Lang.TRIG).toDatasetGraph();

        String queryStr = """
                PREFIX eg: <http://www.example.org/>
                PREFIX spatial: <http://jena.apache.org/spatial#>
                PREFIX geo: <http://www.opengis.net/ont/geosparql#>

                SELECT * {
                  VALUES ?search {
                    "POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0))"^^geo:wktLiteral
                  }
                  LATERAL {
                    ?feature spatial:intersectBoxGeom(?search) .
                  }
                }
            """;

        buildSpatialIndex(dsg, SRS_URI.DEFAULT_WKT_CRS84);
        Table table = QueryExec.dataset(dsg).query(queryStr).table();
        debugPrint(table);
        Assert.assertTrue(table.isEmpty());
    }

    @Test
    public void mustNotMatchNamedGraph() throws SpatialIndexException {
        DatasetGraph dsg = RDFParserBuilder.create().fromString( """
                PREFIX eg: <http://www.example.org/>
                PREFIX geo: <http://www.opengis.net/ont/geosparql#>

                eg:graph1 {
                  eg:feature1 geo:hasGeometry eg:geometry1 .
                  eg:geometry1 geo:asWKT "POINT (-10 -10)"^^geo:wktLiteral .
                }

                eg:graph2 {
                  eg:feature1 geo:hasGeometry eg:geometry1 .
                  eg:geometry1 geo:asWKT "POINT (0.7 0.7)"^^geo:wktLiteral .
                }
            """).lang(Lang.TRIG).toDatasetGraph();

        String queryStr = """
                PREFIX eg: <http://www.example.org/>
                PREFIX spatial: <http://jena.apache.org/spatial#>
                PREFIX geo: <http://www.opengis.net/ont/geosparql#>

                SELECT * {
                  VALUES ?search {
                    "POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0))"^^geo:wktLiteral
                  }
                  LATERAL {
                    GRAPH eg:graph1 { ?feature spatial:intersectBoxGeom(?search) . }
                  }
                }
            """;

        buildSpatialIndex(dsg, SRS_URI.DEFAULT_WKT_CRS84);
        Table table = QueryExec.dataset(dsg).query(queryStr).table();
        debugPrint(table);
        Assert.assertTrue(table.isEmpty());
    }

    @Test
    public void mustMatchNamedGraph() throws SpatialIndexException {
        DatasetGraph dsg = RDFParserBuilder.create().fromString( """
                PREFIX eg: <http://www.example.org/>
                PREFIX geo: <http://www.opengis.net/ont/geosparql#>

                eg:graph1 {
                  eg:feature1 geo:hasGeometry eg:geometry1 .
                  eg:geometry1 geo:asWKT "POINT (-10 -10)"^^geo:wktLiteral .
                }

                eg:graph2 {
                  eg:feature1 geo:hasGeometry eg:geometry1 .
                  eg:geometry1 geo:asWKT "POINT (0.7 0.7)"^^geo:wktLiteral .
                }
            """).lang(Lang.TRIG).toDatasetGraph();

        String queryStr = """
                PREFIX eg: <http://www.example.org/>
                PREFIX spatial: <http://jena.apache.org/spatial#>
                PREFIX geo: <http://www.opengis.net/ont/geosparql#>

                SELECT * {
                  VALUES ?search {
                    "POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0))"^^geo:wktLiteral
                  }
                  LATERAL {
                    GRAPH eg:graph2 { ?feature spatial:intersectBoxGeom(?search) . }
                  }
                }
            """;

        buildSpatialIndex(dsg, SRS_URI.DEFAULT_WKT_CRS84);
        Table table = QueryExec.dataset(dsg).query(queryStr).table();
        debugPrint(table);
        Assert.assertFalse(table.isEmpty());
    }
}
