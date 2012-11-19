/*
 * Copyright 2012 YarcData LLC All Rights Reserved.
 */ 

package com.hp.hpl.jena.sparql.function.user;

import java.util.ArrayList;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_Multiply;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.lang.sparql_11.ParseException;

/**
 * Tests for the {@link UserDefinedFunctionFactory}
 * @author rvesse
 *
 */
public class TestUserDefinedFunctionFactory {
    
    @BeforeClass
    public static void setup() {
        UserDefinedFunctionFactory.getFactory().clear();
    }
    
    @AfterClass
    public static void teardown() {
        UserDefinedFunctionFactory.getFactory().clear();
    }
    
    @Test
    public void test_user_defined_function_factory_instance() {
        UserDefinedFunctionFactory factory = UserDefinedFunctionFactory.getFactory();
        Assert.assertNotNull(factory);
    }
    
    @Test
    public void test_user_defined_function_factory_add_01() {
        Expr e = new E_Multiply(new ExprVar("x"), new ExprVar("x"));
        UserDefinedFunctionFactory.getFactory().add("http://example/square", e, new ArrayList<Var>(e.getVarsMentioned()));
        Assert.assertTrue(UserDefinedFunctionFactory.getFactory().isRegistered("http://example/square"));
        Assert.assertEquals(e, UserDefinedFunctionFactory.getFactory().get("http://example/square").getBaseExpr());
    }
    
    @Test
    public void test_user_defined_function_factory_add_02() {
        Expr e1 = new E_Multiply(new ExprVar("x"), new ExprVar("x"));
        Expr e2 = new E_Multiply(new ExprVar("y"), new ExprVar("y"));
        
        UserDefinedFunctionFactory.getFactory().add("http://example/square", e1, new ArrayList<Var>(e1.getVarsMentioned()));
        Assert.assertTrue(UserDefinedFunctionFactory.getFactory().isRegistered("http://example/square"));
        Assert.assertEquals(e1, UserDefinedFunctionFactory.getFactory().get("http://example/square").getBaseExpr());
        
        UserDefinedFunctionFactory.getFactory().add("http://example/square", e2, new ArrayList<Var>(e2.getVarsMentioned()));
        Assert.assertTrue(UserDefinedFunctionFactory.getFactory().isRegistered("http://example/square"));
        Assert.assertEquals(e2, UserDefinedFunctionFactory.getFactory().get("http://example/square").getBaseExpr());
    }
    
    @Test
    public void test_user_defined_function_factory_add_03() throws ParseException {
        Expr e = new E_Multiply(new ExprVar("x"), new ExprVar("x"));
        
        //Instead of registering the pre-built expression register using a string for the expression
        UserDefinedFunctionFactory.getFactory().add("http://example/square", "?x * ?x", new ArrayList<Var>(e.getVarsMentioned()));
        
        Assert.assertTrue(UserDefinedFunctionFactory.getFactory().isRegistered("http://example/square"));
        Assert.assertEquals(e, UserDefinedFunctionFactory.getFactory().get("http://example/square").getBaseExpr());
    }
}
