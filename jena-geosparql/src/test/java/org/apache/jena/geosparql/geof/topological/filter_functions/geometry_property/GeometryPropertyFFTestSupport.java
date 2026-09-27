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
package org.apache.jena.geosparql.geof.topological.filter_functions.geometry_property;

import org.apache.jena.graph.Node;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.ModelFactory;

final class GeometryPropertyFFTestSupport {
    static final String GML_2D_POINT = """
        '<gml:Point xmlns:gml="http://www.opengis.net/gml/3.2"
                    srsName="http://www.opengis.net/def/crs/OGC/1.3/CRS84">
           <gml:pos>1 2</gml:pos>
         </gml:Point>'^^geo:gmlLiteral
        """.replace("\n", " ");

    static final String GML_3D_POINT = """
        '<gml:Point xmlns:gml="http://www.opengis.net/gml/3.2"
                    srsName="http://www.opengis.net/def/crs/EPSG/0/4979">
           <gml:pos srsDimension="3">2 1 3</gml:pos>
         </gml:Point>'^^geo:gmlLiteral
        """.replace("\n", " ");

    private GeometryPropertyFFTestSupport() {}

    static String wktArgument(String wkt) {
        return "'" + wkt + "'^^geo:wktLiteral";
    }

    static Node evaluate(String expression) {
        String query = """
            PREFIX geof: <http://www.opengis.net/def/function/geosparql/>
            PREFIX geo: <http://www.opengis.net/ont/geosparql#>
            SELECT ?result WHERE { BIND(%s AS ?result) }
            """.formatted(expression);
        try (QueryExecution execution = QueryExecution.create(query, ModelFactory.createDefaultModel())) {
            ResultSet results = execution.execSelect();
            QuerySolution solution = results.next();
            if (results.hasNext())
                throw new IllegalStateException("Expected one solution for " + expression);
            return solution.contains("result") ? solution.get("result").asNode() : null;
        }
    }
}
