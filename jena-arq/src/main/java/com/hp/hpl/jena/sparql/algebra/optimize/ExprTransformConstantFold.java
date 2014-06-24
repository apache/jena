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

package com.hp.hpl.jena.sparql.algebra.optimize;

import java.util.ArrayList ;
import java.util.List ;

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.TransformCopy ;
import com.hp.hpl.jena.sparql.algebra.Transformer ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.expr.* ;

/**
 * An expression transform that simplifies expressions by constant folding
 * wherever possible
 */
public class ExprTransformConstantFold extends ExprTransformCopy {

    private Binding b = null;
    
    @Override
    public Expr transform(ExprFunction1 func, Expr expr1) {
        if (isFoldable(expr1)) {
            try {
                return func.eval(expr1.getConstant()) ;
            } catch (Exception ex) { /* Drop through */ }
        }
        return super.transform(func, expr1);
    }

    @Override
    public Expr transform(ExprFunction2 func, Expr expr1, Expr expr2) {
        if (isFoldable(expr1, expr2)) {
            try {
                return func.eval(expr1.getConstant(), expr2.getConstant()) ;
            } catch (Exception ex) { /* Drop through */ }
        }
        return super.transform(func, expr1, expr2);
    }

    @Override
    public Expr transform(ExprFunction3 func, Expr expr1, Expr expr2, Expr expr3) {
        if (isFoldable(expr1, expr2, expr3)) {
            try { 
                return func.eval(expr1.getConstant(), expr2.getConstant(), expr3.getConstant()) ;
            } catch (Exception ex) { /* Drop through */ }
        }
        return super.transform(func, expr1, expr2, expr3);
    }

    @Override
    public Expr transform(ExprFunctionN func, ExprList args) {
        if (isFoldable(args)) {
            List<NodeValue> args2 = new ArrayList<>() ;
            // Need to "cast" to constants
            for ( Expr e : args )
                args2.add(e.getConstant()) ;
            try {
                return func.eval(args2) ;
            } catch (Exception ex) { /* Drop through */ }
        }
        return super.transform(func, args);
    }

    @Override
    public Expr transform(ExprFunctionOp funcOp, ExprList args, Op opArg) {
        // Manually transform each argument
        Op op = Transformer.transform(new TransformCopy(), this, funcOp.getGraphPattern());
        ExprList newArgs = new ExprList();
        for (int i = 0; i < args.size(); i++) {
            Expr curr = args.get(i);
            Expr newArg = ExprTransformer.transform(this, curr) ;
            newArgs.add(newArg);
        }
        return funcOp.copy(newArgs, op);
    }

    private boolean isFoldable(ExprList exprs) {
        for (Expr e : exprs) {
            if (e == null || !e.isConstant())
                return false;
        }
        return true;
    }

    private boolean isFoldable(Expr... exprs) {
        for (Expr e : exprs) {
            if (e == null || !e.isConstant())
                return false;
        }
        return true;
    }
}
