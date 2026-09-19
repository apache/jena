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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThrows;

import java.util.List;

import org.apache.jena.geosparql.configuration.GeoSPARQLConfig;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QueryBuildException;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class GeometryMetadataErrorsTest {
    @Parameterized.Parameters(name = "function: {0}")
    public static List<Object[]> functions() {
        return List.of(
            new Object[] { "geometryType", new GeometryTypeFF() },
            new Object[] { "is3D", new Is3DFF() },
            new Object[] { "isMeasured", new IsMeasuredFF() },
            new Object[] { "numGeometries", new NumGeometriesFF() }
        );
    }

    private final String name;
    private final FunctionBase1 function;

    public GeometryMetadataErrorsTest(String name, FunctionBase1 function) {
        this.name = name;
        this.function = function;
    }

    @BeforeClass
    public static void setup() {
        GeoSPARQLConfig.setupNoIndex();
    }

    @Test
    public void iriArgumentRaisesExpressionError() {
        assertThrows(ExprEvalException.class, () -> function.exec(NodeValue.makeNode(NodeFactory.createURI("urn:not-a-literal"))));
    }

    @Test
    public void numericArgumentRaisesExpressionError() {
        assertThrows(ExprEvalException.class, () -> function.exec(NodeValue.makeInteger(42)));
    }

    @Test
    public void stringWithoutGeometryDatatypeRaisesExpressionError() {
        assertThrows(ExprEvalException.class, () -> function.exec(NodeValue.makeString("POINT (1 2)")));
    }

    @Test
    public void malformedWktRaisesExpressionError() {
        NodeValue malformed = NodeValue.makeNode("invalid", WKTDatatype.INSTANCE);
        assertThrows(ExprEvalException.class, () -> function.exec(malformed));
    }

    @Test
    public void iriArgumentLeavesBindUnbound() {
        assertUnbound("<urn:not-a-literal>");
    }

    @Test
    public void numericArgumentLeavesBindUnbound() {
        assertUnbound("42");
    }

    @Test
    public void stringWithoutGeometryDatatypeLeavesBindUnbound() {
        assertUnbound("'POINT (1 2)'");
    }

    @Test
    public void malformedWktLeavesBindUnbound() {
        assertUnbound("'invalid'^^geo:wktLiteral");
    }

    @Test
    public void unboundArgumentLeavesBindUnbound() {
        assertUnbound("?missing");
    }

    @Test
    public void missingArgumentIsRejectedAtQueryBuild() {
        assertThrows(QueryBuildException.class, () -> evaluate("geof:" + name + "()"));
    }

    @Test
    public void extraArgumentIsRejectedAtQueryBuild() {
        assertThrows(QueryBuildException.class,
                     () -> evaluate("geof:" + name + "('POINT EMPTY'^^geo:wktLiteral, 1)"));
    }

    private static Node evaluate(String expression) {
        String query = """
            PREFIX geof: <http://www.opengis.net/def/function/geosparql/>
            PREFIX geo: <http://www.opengis.net/ont/geosparql#>
            SELECT ?result WHERE { BIND(%s AS ?result) }
            """.formatted(expression);
        try (QueryExecution execution = QueryExecution.create(query, ModelFactory.createDefaultModel())) {
            ResultSet results = execution.execSelect();
            assertTrue("Expected one solution for " + expression, results.hasNext());
            QuerySolution solution = results.next();
            assertFalse("Expected only one solution for " + expression, results.hasNext());
            return solution.contains("result") ? solution.get("result").asNode() : null;
        }
    }

    private void assertUnbound(String argument) {
        String expression = "geof:" + name + "(" + argument + ")";
        assertNull(expression, evaluate(expression));
    }
}
