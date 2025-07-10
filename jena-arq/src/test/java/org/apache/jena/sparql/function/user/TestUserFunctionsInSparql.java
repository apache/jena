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

package org.apache.jena.sparql.function.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.apache.jena.graph.Node;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.E_Multiply;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.util.NodeFactoryExtra;

/**
 * Tests that user functions are properly wired in and usable from SPARQL
 *
 */
public class TestUserFunctionsInSparql {

    @BeforeAll
    public static void setup() {
        UserDefinedFunctionFactory.getFactory().clear();

        //Define a square function
        Expr square = new E_Multiply(new ExprVar("x"), new ExprVar("x"));
        UserDefinedFunctionFactory.getFactory().add("http://example/square", square, new ArrayList<>(square.getVarsMentioned()));
    }

    @AfterAll
    public static void teardown() {
        UserDefinedFunctionFactory.getFactory().clear();
    }

    @Test
    public void test_user_functions_in_sparql() {
        assertTrue(UserDefinedFunctionFactory.getFactory().isRegistered("http://example/square"));

        String query = "SELECT (<http://example/square>(2) AS ?square) { }";
        Query q = QueryFactory.create(query);

        try(QueryExecution qe = QueryExecutionFactory.create(q, ModelFactory.createDefaultModel())) {
            ResultSet rset = qe.execSelect();
            assertTrue(rset.hasNext());
            Binding b = rset.nextBinding();
            assertFalse(rset.hasNext());
            //Validate returned value
            Node actual = b.get(Var.alloc("square"));
            assertEquals(NodeFactoryExtra.intToNode(4), actual);
        }
    }
}
