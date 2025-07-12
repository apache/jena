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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.apache.jena.sparql.expr.E_Multiply;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;

/**
 * Tests for the {@link UserDefinedFunctionFactory}
 *
 */
public class TestUserDefinedFunctionFactory {

    @BeforeAll
    public static void setup() {
        UserDefinedFunctionFactory.getFactory().clear();
    }

    @AfterAll
    public static void teardown() {
        UserDefinedFunctionFactory.getFactory().clear();
    }

    @Test
    public void test_user_defined_function_factory_instance() {
        UserDefinedFunctionFactory factory = UserDefinedFunctionFactory.getFactory();
        assertNotNull(factory);
    }

    @Test
    public void test_user_defined_function_factory_add_01() {
        Expr e = new E_Multiply(new ExprVar("x"), new ExprVar("x"));
        UserDefinedFunctionFactory.getFactory().add("http://example/square", e, new ArrayList<>(e.getVarsMentioned()));
        assertTrue(UserDefinedFunctionFactory.getFactory().isRegistered("http://example/square"));
        assertEquals(e, UserDefinedFunctionFactory.getFactory().get("http://example/square").getBaseExpr());
    }

    @Test
    public void test_user_defined_function_factory_add_02() {
        Expr e1 = new E_Multiply(new ExprVar("x"), new ExprVar("x"));
        Expr e2 = new E_Multiply(new ExprVar("y"), new ExprVar("y"));

        UserDefinedFunctionFactory.getFactory().add("http://example/square", e1, new ArrayList<>(e1.getVarsMentioned()));
        assertTrue(UserDefinedFunctionFactory.getFactory().isRegistered("http://example/square"));
        assertEquals(e1, UserDefinedFunctionFactory.getFactory().get("http://example/square").getBaseExpr());

        UserDefinedFunctionFactory.getFactory().add("http://example/square", e2, new ArrayList<>(e2.getVarsMentioned()));
        assertTrue(UserDefinedFunctionFactory.getFactory().isRegistered("http://example/square"));
        assertEquals(e2, UserDefinedFunctionFactory.getFactory().get("http://example/square").getBaseExpr());
    }

    @Test
    public void test_user_defined_function_factory_add_03() {
        Expr e = new E_Multiply(new ExprVar("x"), new ExprVar("x"));

        //Instead of registering the pre-built expression register using a string for the expression
        UserDefinedFunctionFactory.getFactory().add("http://example/square", "?x * ?x", new ArrayList<>(e.getVarsMentioned()));

        assertTrue(UserDefinedFunctionFactory.getFactory().isRegistered("http://example/square"));
        assertEquals(e, UserDefinedFunctionFactory.getFactory().get("http://example/square").getBaseExpr());
    }
}
