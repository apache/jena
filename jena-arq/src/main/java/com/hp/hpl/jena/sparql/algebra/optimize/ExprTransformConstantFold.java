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

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.TransformCopy;
import com.hp.hpl.jena.sparql.algebra.Transformer;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.*;

/**
 * An expression transform that simplifies expressions by constant folding
 * wherever possible
 */
public class ExprTransformConstantFold extends ExprTransformCopy {

    private Binding b = null;

    @Override
    public Expr transform(ExprFunction1 func, Expr expr1) {
        Expr transformed = func.copySubstitute(this.b, true);
        if (transformed.equals(func))
            return super.transform(func, expr1);
        return transformed;
    }

    @Override
    public Expr transform(ExprFunction2 func, Expr expr1, Expr expr2) {
        Expr transformed = func.copySubstitute(this.b, true);
        if (transformed.equals(func))
            return super.transform(func, expr1, expr2);
        return transformed;
    }

    @Override
    public Expr transform(ExprFunction3 func, Expr expr1, Expr expr2, Expr expr3) {
        Expr transformed = func.copySubstitute(this.b, true);
        if (transformed.equals(func))
            return super.transform(func, expr1, expr2, expr3);
        return transformed;
    }

    @Override
    public Expr transform(ExprFunctionN func, ExprList args) {
        Expr transformed = func.copySubstitute(this.b, true);
        if (transformed.equals(func))
            return super.transform(func, args);
        return transformed;
    }

    @Override
    public Expr transform(ExprFunctionOp funcOp, ExprList args, Op opArg) {
        // Calling copySubstitute() likely won't work for these cases

        // Manually transform each argument
        Op op = Transformer.transform(new TransformCopy(), this, funcOp.getGraphPattern());
        ExprList newArgs = new ExprList();
        for (int i = 0; i < funcOp.getArgs().size(); i++) {
            Expr curr = funcOp.getArg(i);
            Expr newArg = curr.copySubstitute(this.b, true);
            newArgs.add(newArg);
        }
        return funcOp.copy(newArgs, op);
    }

}
