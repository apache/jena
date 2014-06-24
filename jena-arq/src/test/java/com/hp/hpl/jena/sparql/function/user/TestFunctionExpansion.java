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
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory;
import com.hp.hpl.jena.sparql.expr.E_Add;
import com.hp.hpl.jena.sparql.expr.E_Function;
import com.hp.hpl.jena.sparql.expr.E_Multiply;
import com.hp.hpl.jena.sparql.expr.E_Subtract;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueBoolean;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueDouble;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueInteger;
import com.hp.hpl.jena.sparql.function.FunctionEnvBase;
import com.hp.hpl.jena.sparql.sse.builders.ExprBuildException;
import com.hp.hpl.jena.sparql.util.NodeFactoryExtra;

/**
 * Test for checking that functions are appropriately expanded when supplied with actual arguments
 * and the default behavior of not preserving dependencies is enabled
 */
public class TestFunctionExpansion {
    
    @BeforeClass
    public static void setup() {
        UserDefinedFunctionFactory.getFactory().clear();
        UserDefinedFunctionFactory.getFactory().setPreserveDependencies(false);
    }
    
    @AfterClass
    public static void teardown() {
        UserDefinedFunctionFactory.getFactory().clear();
        UserDefinedFunctionFactory.getFactory().setPreserveDependencies(false);
    }

    @Test
    public void test_function_expansion_01() {
        Expr e = new ExprVar("x");
        UserDefinedFunctionFactory.getFactory().add("http://example/simple", e, new ArrayList<>(e.getVarsMentioned()));
        
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
        UserDefinedFunctionFactory.getFactory().add("http://example/square", e, new ArrayList<>(e.getVarsMentioned()));
        
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
        List<Var> defArgs = new ArrayList<>();
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
    
    @Test
    public void test_function_expansion_04() {
        Expr square = new E_Multiply(new ExprVar("x"), new ExprVar("x"));
        UserDefinedFunctionFactory.getFactory().add("http://example/square", square, new ArrayList<>(square.getVarsMentioned()));
        
        //Test that with preserveDependencies set to false (the default) that the definition of cube is actually
        //expanded to include the definition of square
        Expr cube = new E_Multiply(new E_Function("http://example/square", new ExprList(new ExprVar("x"))), new ExprVar("x"));
        UserDefinedFunctionFactory.getFactory().add("http://example/cube", cube, new ArrayList<>(cube.getVarsMentioned()));
        
        UserDefinedFunctionDefinition def = UserDefinedFunctionFactory.getFactory().get("http://example/cube");
        Expr base = def.getBaseExpr();
        Assert.assertTrue(base instanceof E_Multiply);
        E_Multiply m = (E_Multiply)base;
        Assert.assertTrue(m.getArg1() instanceof E_Multiply);
        Assert.assertTrue(m.getArg2() instanceof ExprVar);
        Assert.assertEquals(1, base.getVarsMentioned().size());
    }
    
    @Test
    public void test_function_expansion_05() {
        Expr square = new E_Multiply(new ExprVar("x"), new ExprVar("x"));
        UserDefinedFunctionFactory.getFactory().add("http://example/square", square, new ArrayList<>(square.getVarsMentioned()));
        
        //Test that with preserveDependencies set to false (the default) that the definition of cube is actually
        //expanded to include the definition of square
        Expr cube = new E_Multiply(new E_Function("http://example/square", new ExprList(new ExprVar("y"))), new ExprVar("y"));
        UserDefinedFunctionFactory.getFactory().add("http://example/cube", cube, new ArrayList<>(cube.getVarsMentioned()));
        
        UserDefinedFunctionDefinition def = UserDefinedFunctionFactory.getFactory().get("http://example/cube");
        Expr base = def.getBaseExpr();
        Assert.assertTrue(base instanceof E_Multiply);
        E_Multiply m = (E_Multiply)base;
        Assert.assertTrue(m.getArg1() instanceof E_Multiply);
        Assert.assertTrue(m.getArg2() instanceof ExprVar);
        Assert.assertEquals(1, base.getVarsMentioned().size());
    }
    
    @Test
    public void test_function_expansion_06() {
        Expr takeaway = new E_Subtract(new ExprVar("x"), new ExprVar("y"));
        List<Var> args = new ArrayList<>();
        args.add(Var.alloc("x"));
        args.add(Var.alloc("y"));
        UserDefinedFunctionFactory.getFactory().add("http://example/takeaway", takeaway, args);
        
        //Test that with preserveDependencies set to false (the default) that the definition is expanded appropriately
        ExprList numArgs = new ExprList();
        numArgs.add(new NodeValueInteger(1));
        numArgs.add(new NodeValueDouble(2.3));
        Expr test = new E_Function("http://example/takeaway", numArgs);
        UserDefinedFunctionFactory.getFactory().add("http://example/test", test, new ArrayList<Var>());
        
        UserDefinedFunctionDefinition def = UserDefinedFunctionFactory.getFactory().get("http://example/test");
        Expr base = def.getBaseExpr();
        Assert.assertTrue(base instanceof E_Subtract);
        E_Subtract subtract = (E_Subtract)base;
        Assert.assertTrue(subtract.getArg1() instanceof NodeValueInteger);
        Assert.assertTrue(subtract.getArg2() instanceof NodeValueDouble);
    }
    
    @Test
    public void test_function_expansion_07() {
        Expr takeaway = new E_Subtract(new ExprVar("x"), new ExprVar("y"));
        List<Var> args = new ArrayList<>();
        args.add(Var.alloc("x"));
        args.add(Var.alloc("y"));
        UserDefinedFunctionFactory.getFactory().add("http://example/takeaway", takeaway, args);
        
        //Test that with preserveDependencies set to false (the default) that the definition is expanded appropriately
        ExprList numArgs = new ExprList();
        numArgs.add(new NodeValueDouble(2.3));
        numArgs.add(new NodeValueInteger(1));
        Expr test = new E_Function("http://example/takeaway", numArgs);
        UserDefinedFunctionFactory.getFactory().add("http://example/test", test, new ArrayList<Var>());
        
        UserDefinedFunctionDefinition def = UserDefinedFunctionFactory.getFactory().get("http://example/test");
        Expr base = def.getBaseExpr();
        Assert.assertTrue(base instanceof E_Subtract);
        E_Subtract subtract = (E_Subtract)base;
        Assert.assertTrue(subtract.getArg1() instanceof NodeValueDouble);
        Assert.assertTrue(subtract.getArg2() instanceof NodeValueInteger);
    }
    
    @Test
    public void test_function_expansion_08() {
        Expr takeaway = new E_Subtract(new ExprVar("x"), new ExprVar("y"));
        List<Var> args = new ArrayList<>();
        args.add(Var.alloc("x"));
        args.add(Var.alloc("y"));
        UserDefinedFunctionFactory.getFactory().add("http://example/takeaway", takeaway, args);
        
        //Test that with preserveDependencies set to false (the default) that the definition is expanded appropriately
        ExprList altArgs = new ExprList();
        altArgs.add(new ExprVar("a"));
        altArgs.add(new ExprVar("b"));
        ArrayList<Var> defArgs = new ArrayList<>();
        defArgs.add(Var.alloc("a"));
        defArgs.add(Var.alloc("b"));
        Expr test = new E_Function("http://example/takeaway", altArgs);
        UserDefinedFunctionFactory.getFactory().add("http://example/test", test, defArgs);
        
        UserDefinedFunctionDefinition def = UserDefinedFunctionFactory.getFactory().get("http://example/test");
        Expr base = def.getBaseExpr();
        Assert.assertTrue(base instanceof E_Subtract);
        E_Subtract subtract = (E_Subtract)base;
        Assert.assertTrue(subtract.getArg1() instanceof ExprVar);
        Assert.assertTrue(subtract.getArg2() instanceof ExprVar);
        Assert.assertEquals(subtract.getArg1().getVarName(), "a");
        Assert.assertEquals(subtract.getArg2().getVarName(), "b");
    }
    
    @Test
    public void test_function_expansion_09() {
        Expr takeaway = new E_Subtract(new ExprVar("x"), new ExprVar("y"));
        List<Var> args = new ArrayList<>();
        args.add(Var.alloc("x"));
        args.add(Var.alloc("y"));
        UserDefinedFunctionFactory.getFactory().add("http://example/takeaway", takeaway, args);
        
        //Test that with preserveDependencies set to false (the default) that the definition is expanded appropriately
        ExprList altArgs = new ExprList();
        altArgs.add(new ExprVar("b"));
        altArgs.add(new ExprVar("a"));
        ArrayList<Var> defArgs = new ArrayList<>();
        defArgs.add(Var.alloc("a"));
        defArgs.add(Var.alloc("b"));
        Expr test = new E_Function("http://example/takeaway", altArgs);
        UserDefinedFunctionFactory.getFactory().add("http://example/test", test, defArgs);
        
        UserDefinedFunctionDefinition def = UserDefinedFunctionFactory.getFactory().get("http://example/test");
        Expr base = def.getBaseExpr();
        Assert.assertTrue(base instanceof E_Subtract);
        E_Subtract subtract = (E_Subtract)base;
        Assert.assertTrue(subtract.getArg1() instanceof ExprVar);
        Assert.assertTrue(subtract.getArg2() instanceof ExprVar);
        Assert.assertEquals(subtract.getArg1().getVarName(), "b");
        Assert.assertEquals(subtract.getArg2().getVarName(), "a");
    }
    
    @Test
    public void test_function_expansion_10() {
        Expr single = new ExprVar("x");
        UserDefinedFunctionFactory.getFactory().add("http://example/single", single, new ArrayList<>(single.getVarsMentioned()));
        
        //Test that with preserveDependencies set to false (the default) that the definition is expanded appropriately
        //when the outer function has differing numbers of arguments
        List<Var> args = new ArrayList<>();
        args.add(Var.alloc("x"));
        args.add(Var.alloc("y"));
        Expr add = new E_Add(new E_Function("http://example/single", new ExprList(new ExprVar("x"))), new ExprVar("y"));
        UserDefinedFunctionFactory.getFactory().add("http://example/add", add, args);
        
        UserDefinedFunctionDefinition def = UserDefinedFunctionFactory.getFactory().get("http://example/add");
        Expr base = def.getBaseExpr();
        Assert.assertTrue(base instanceof E_Add);
        E_Add actual = (E_Add)base;
        Assert.assertTrue(actual.getArg1() instanceof ExprVar);
        Assert.assertTrue(actual.getArg2() instanceof ExprVar);
        Assert.assertEquals("x", actual.getArg1().getVarName());
        Assert.assertEquals("y", actual.getArg2().getVarName());
    }
    
    @Test
    public void test_function_expansion_11() {
        Expr single = new ExprVar("x");
        UserDefinedFunctionFactory.getFactory().add("http://example/single", single, new ArrayList<>(single.getVarsMentioned()));
        
        //Test that with preserveDependencies set to false (the default) that the definition is expanded appropriately
        //when the outer function has differing numbers of arguments
        List<Var> args = new ArrayList<>();
        args.add(Var.alloc("x"));
        args.add(Var.alloc("y"));
        Expr add = new E_Add(new E_Function("http://example/single", new ExprList(new ExprVar("y"))), new ExprVar("y"));
        UserDefinedFunctionFactory.getFactory().add("http://example/add", add, args);
        
        UserDefinedFunctionDefinition def = UserDefinedFunctionFactory.getFactory().get("http://example/add");
        Expr base = def.getBaseExpr();
        Assert.assertTrue(base instanceof E_Add);
        E_Add actual = (E_Add)base;
        Assert.assertTrue(actual.getArg1() instanceof ExprVar);
        Assert.assertTrue(actual.getArg2() instanceof ExprVar);
        Assert.assertEquals("y", actual.getArg1().getVarName());
        Assert.assertEquals("y", actual.getArg2().getVarName());
    }
    
    @Test
    public void test_function_expansion_12() {
        Expr takeaway = new E_Subtract(new ExprVar("x"), new ExprVar("y"));
        List<Var> args = new ArrayList<>();
        args.add(Var.alloc("x"));
        args.add(Var.alloc("y"));
        UserDefinedFunctionFactory.getFactory().add("http://example/takeaway", takeaway, args);
        
        //Test that with preserveDependencies set to false (the default) that the definition is expanded appropriately
        ExprList altArgs = new ExprList();
        altArgs.add(new ExprVar("a"));
        altArgs.add(new ExprVar("a"));
        ArrayList<Var> defArgs = new ArrayList<>();
        defArgs.add(Var.alloc("a"));
        defArgs.add(Var.alloc("b"));
        Expr test = new E_Function("http://example/takeaway", altArgs);
        UserDefinedFunctionFactory.getFactory().add("http://example/test", test, defArgs);
        
        UserDefinedFunctionDefinition def = UserDefinedFunctionFactory.getFactory().get("http://example/test");
        Expr base = def.getBaseExpr();
        Assert.assertTrue(base instanceof E_Subtract);
        E_Subtract subtract = (E_Subtract)base;
        Assert.assertTrue(subtract.getArg1() instanceof ExprVar);
        Assert.assertTrue(subtract.getArg2() instanceof ExprVar);
        Assert.assertEquals(subtract.getArg1().getVarName(), "a");
        Assert.assertEquals(subtract.getArg2().getVarName(), "a");
    }
    
    @Test
    public void test_function_expansion_13() {
        Expr square = new E_Multiply(new ExprVar("x"), new ExprVar("x"));
        UserDefinedFunctionFactory.getFactory().add("http://example/square", square, new ArrayList<>(square.getVarsMentioned()));
        
        //This test illustrates that if we change the definition of square and call our function again we always
        //get the same result with dependencies not preserved because even though the definition of the dependent function 
        //can change the definition of our function is fully expanded when first defined
        Expr cube = new E_Multiply(new E_Function("http://example/square", new ExprList(new ExprVar("x"))), new ExprVar("x"));
        UserDefinedFunctionFactory.getFactory().add("http://example/cube", cube, new ArrayList<>(cube.getVarsMentioned()));
        
        UserDefinedFunction f = (UserDefinedFunction) UserDefinedFunctionFactory.getFactory().create("http://example/cube");
        f.build("http://example/cube", new ExprList(new NodeValueInteger(2)));
        
        Expr actual = f.getActualExpr();
        NodeValue result = actual.eval(BindingFactory.create(), FunctionEnvBase.createTest());
        Assert.assertEquals(8, NodeFactoryExtra.nodeToInt(result.asNode()));
        
        //Change the definition of the function we depend on
        //This has no effect with preserveDependencies set to false (the default) since we fully expanded the call to the dependent
        //function when our outer function was defined
        square = new ExprVar("x");
        UserDefinedFunctionFactory.getFactory().add("http://example/square", square, new ArrayList<>(square.getVarsMentioned()));
        f.build("http://example/cube", new ExprList(new NodeValueInteger(2)));
        
        actual = f.getActualExpr();
        result = actual.eval(BindingFactory.create(), FunctionEnvBase.createTest());
        Assert.assertEquals(8, NodeFactoryExtra.nodeToInt(result.asNode()));
    }
    
    @Test(expected=ExprBuildException.class)
    public void test_function_expansion_bad_01() {
        List<Var> args = new ArrayList<>();
        args.add(Var.alloc("x"));
        args.add(Var.alloc("y"));
        Expr add = new E_Add(new ExprVar("x"), new ExprVar("y"));
        
        //It's an error to use a variable which is not mentioned in the argument list
        UserDefinedFunctionFactory.getFactory().add("http://example/add", add, new ArrayList<Var>());
    }
    
    @Test(expected=ExprBuildException.class)
    public void test_function_expansion_bad_02() {
        Expr single = new ExprVar("x");
        UserDefinedFunctionFactory.getFactory().add("http://example/single", single, new ArrayList<>(single.getVarsMentioned()));
        
        //It's an error to use a variable which is not mentioned in the argument list, even in a call to a dependent function
        Expr test = new E_Function("http://example/single", new ExprList(new ExprVar("x")));
        UserDefinedFunctionFactory.getFactory().add("http://example/test", test, new ArrayList<Var>());
    }
}
