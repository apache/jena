/*
 * Copyright 2012 YarcData LLC All Rights Reserved.
 */ 

package com.hp.hpl.jena.sparql.function.user;

import junit.framework.JUnit4TestAdapter;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.hp.hpl.jena.sparql.expr.TS_Expr;

@RunWith(Suite.class)
@SuiteClasses( {
    TestUserDefinedFunctionFactory.class,
    TestFunctionExpansion.class,
    TestUserFunctionsInSparql.class
})
public class TS_UserFunctions {
    
    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(TS_Expr.class);
      }
}
