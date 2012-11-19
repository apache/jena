/*
 * Copyright 2012 YarcData LLC All Rights Reserved.
 */ 

package com.hp.hpl.jena.sparql.function.user;

import java.util.Map;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;
import com.hp.hpl.jena.sparql.expr.ExprFunction0;
import com.hp.hpl.jena.sparql.expr.ExprFunction1;
import com.hp.hpl.jena.sparql.expr.ExprFunction2;
import com.hp.hpl.jena.sparql.expr.ExprFunction3;
import com.hp.hpl.jena.sparql.expr.ExprFunctionN;
import com.hp.hpl.jena.sparql.expr.ExprFunctionOp;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.ExprTransformCopy;

/**
 * An expression transformer that will expand user defined expressions so they do not explicitly rely on other user defined functions
 * @author rvesse
 *
 */
public class ExprTransformExpand extends ExprTransformCopy {
    
    private Map<String, UserDefinedFunctionDefinition> definitions;
    
    /**
     * Creates a new transformer
     * @param defs User defined function definitions
     */
    public ExprTransformExpand(Map<String, UserDefinedFunctionDefinition> defs) {
        if (defs == null) throw new IllegalArgumentException("defs cannot be null");
        this.definitions = defs;
    }

    @Override
    public Expr transform(ExprFunction0 func) {
        ExprFunction f = func.getFunction();
        if (this.shouldExpand(f)) {
            //TODO Need to expand the function
            return super.transform(func);
        } else {
            return super.transform(func);
        }
    }

    @Override
    public Expr transform(ExprFunction1 func, Expr expr1) {
        // TODO Auto-generated method stub
        return super.transform(func, expr1);
    }

    @Override
    public Expr transform(ExprFunction2 func, Expr expr1, Expr expr2) {
        // TODO Auto-generated method stub
        return super.transform(func, expr1, expr2);
    }

    @Override
    public Expr transform(ExprFunction3 func, Expr expr1, Expr expr2, Expr expr3) {
        // TODO Auto-generated method stub
        return super.transform(func, expr1, expr2, expr3);
    }

    @Override
    public Expr transform(ExprFunctionN func, ExprList args) {
        // TODO Auto-generated method stub
        return super.transform(func, args);
    }

    @Override
    public Expr transform(ExprFunctionOp funcOp, ExprList args, Op opArg) {
        // TODO Auto-generated method stub
        return super.transform(funcOp, args, opArg);
    }
    
    private boolean shouldExpand(ExprFunction func) {
        return this.definitions.containsKey(func.getFunctionIRI());
    }

}
