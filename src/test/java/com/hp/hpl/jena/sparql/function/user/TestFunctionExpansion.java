/*
 * Copyright 2012 YarcData LLC All Rights Reserved.
 */ 

package com.hp.hpl.jena.sparql.function.user;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_Multiply;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueBoolean;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueInteger;

/**
 * Test for checking that functions are appropriately expanded when supplied with actual arguments
 * @author rvesse
 *
 */
public class TestFunctionExpansion {
    
    @BeforeClass
    public static void setup() {
        UserDefinedFunctionFactory.getFactory().clear();
    }
    
    @AfterClass
    public static void teardown() {
        UserDefinedFunctionFactory.getFactory().clear();
    }

    @Test
    public void test_function_expansion_01() {
        Expr e = new ExprVar("x");
        UserDefinedFunctionFactory.getFactory().add("http://example/simple", e, new ArrayList<Var>(e.getVarsMentioned()));
        
        UserDefinedFunction f = (UserDefinedFunction) UserDefinedFunctionFactory.getFactory().create("http://example/simple");
        f.build("http://example/simple", new ExprList(new NodeValueBoolean(true)));
        
        Expr actual = f.getActualExpr();
        Assert.assertFalse(e.equals(actual));
        Assert.assertEquals(0, actual.getVarsMentioned().size());
        Assert.assertEquals(new NodeValueBoolean(true), actual);
    }
    
    @Test
    public void test_function_expansion_02() {
        Expr e = new E_Multiply(new ExprVar("x"), new ExprVar("x"));
        UserDefinedFunctionFactory.getFactory().add("http://example/square", e, new ArrayList<Var>(e.getVarsMentioned()));
        
        UserDefinedFunction f = (UserDefinedFunction) UserDefinedFunctionFactory.getFactory().create("http://example/square");
        f.build("http://example/square", new ExprList(new NodeValueInteger(3)));
        
        Expr actual = f.getActualExpr();
        Assert.assertFalse(e.equals(actual));
        Assert.assertEquals(0, actual.getVarsMentioned().size());
        Assert.assertEquals(new E_Multiply(new NodeValueInteger(3), new NodeValueInteger(3)), actual);
    }
    
    @Test
    public void test_function_expansion_03() {
        Expr e = new E_Multiply(new ExprVar("x"), new ExprVar("y"));
        List<Var> defArgs = new ArrayList<Var>();
        defArgs.add(Var.alloc("x"));
        defArgs.add(Var.alloc("y"));
        UserDefinedFunctionFactory.getFactory().add("http://example/square", e, defArgs);
        
        UserDefinedFunction f = (UserDefinedFunction) UserDefinedFunctionFactory.getFactory().create("http://example/square");
        ExprList args = new ExprList();
        args.add(new NodeValueInteger(3));
        args.add(new NodeValueInteger(4));
        f.build("http://example/square", args);
        
        Expr actual = f.getActualExpr();
        Assert.assertFalse(e.equals(actual));
        Assert.assertEquals(0, actual.getVarsMentioned().size());
        Assert.assertEquals(new E_Multiply(new NodeValueInteger(3), new NodeValueInteger(4)), actual);
    }
}
