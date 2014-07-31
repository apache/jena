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

package com.hp.hpl.jena.sparql.function.user;

import java.util.ArrayList;

import org.junit.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.E_Multiply;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.util.NodeFactoryExtra;

/**
 * Tests that user functions are properly wired in and usable from SPARQL
 *
 */
public class TestUserFunctionsInSparql {

    @BeforeClass
    public static void setup() {
        UserDefinedFunctionFactory.getFactory().clear();
        
        //Define a square function
        Expr square = new E_Multiply(new ExprVar("x"), new ExprVar("x"));
        UserDefinedFunctionFactory.getFactory().add("http://example/square", square, new ArrayList<>(square.getVarsMentioned()));
    }
    
    @AfterClass
    public static void teardown() {
        UserDefinedFunctionFactory.getFactory().clear();
    }
    
    @Test
    public void test_user_functions_in_sparql() {
        Assert.assertTrue(UserDefinedFunctionFactory.getFactory().isRegistered("http://example/square"));
        
        String query = "SELECT (<http://example/square>(2) AS ?square) { }";
        Query q = QueryFactory.create(query);
        
        try(QueryExecution qe = QueryExecutionFactory.create(q, ModelFactory.createDefaultModel())) {
            ResultSet rset = qe.execSelect();
            Assert.assertTrue(rset.hasNext());
            Binding b = rset.nextBinding();
            Assert.assertFalse(rset.hasNext());
            //Validate returned value
            Node actual = b.get(Var.alloc("square"));
            Assert.assertEquals(NodeFactoryExtra.intToNode(4), actual);
        }
    }
}
