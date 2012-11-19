/*
 * Copyright 2012 YarcData LLC All Rights Reserved.
 */ 

package com.hp.hpl.jena.sparql.function.user;

import java.util.ArrayList;

import junit.framework.Assert;

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
import com.hp.hpl.jena.sparql.util.NodeFactory;

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
        UserDefinedFunctionFactory.getFactory().add("http://example/square", square, new ArrayList<Var>(square.getVarsMentioned()));
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
        
        QueryExecution qe = QueryExecutionFactory.create(q, ModelFactory.createDefaultModel());
        ResultSet rset = qe.execSelect();
        Assert.assertTrue(rset.hasNext());
        Binding b = rset.nextBinding();
        Assert.assertFalse(rset.hasNext());
        qe.close();
        
        //Validate returned value
        Node actual = b.get(Var.alloc("square"));
        Assert.assertEquals(NodeFactory.intToNode(4), actual);
    }
}
