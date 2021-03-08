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

package org.apache.jena.sparql.expr;

import java.util.Collection ;
import java.util.HashSet ;
import java.util.Set ;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.SortCondition ;
import org.apache.jena.sparql.algebra.OpVars ;
import org.apache.jena.sparql.algebra.walker.Walker ;
import org.apache.jena.sparql.core.Var ;

public class ExprVars
{
    @FunctionalInterface
    interface Action<T> { void var(Collection<T> acc, Var var) ; }

    // Collect variables / ExprList

    public static Set<Var> getVarsMentioned(Expr expr) {
        Set<Var> acc = new HashSet<>();
        varsMentioned(acc, expr);
        return acc;
    }

    public static Set<Var> getNonOpVarsMentioned(Expr expr) {
        Set<Var> acc = new HashSet<>();
        nonOpVarsMentioned(acc, expr);
        return acc;
    }

    private static Action<Var> accVar = (a, var) -> a.add(var) ;

    public static void varsMentioned(Collection<Var> acc, Expr expr) {
        ExprVarsWorker<Var> vv = new ExprVarsWorker<>(acc, accVar) ;
        Walker.walk(expr, vv) ;
    }

    public static void nonOpVarsMentioned(Collection<Var> acc, Expr expr) {
        ExprNoOpVarsWorker<Var> vv = new ExprNoOpVarsWorker<>(acc, accVar) ;
        Walker.walk(expr, vv) ;
    }

    // Collect variables / ExprList

    public static Set<Var> getVarsMentioned(ExprList exprs) {
        Set<Var> acc = new HashSet<>();
        varsMentioned(acc, exprs);
        return acc;
    }

    public static void varsMentioned(Collection<Var> acc, ExprList exprs) {
        exprs.forEach(e->varsMentioned(acc, e));
    }

    public static Set<Var> getNonOpVarsMentioned(ExprList exprs) {
        Set<Var> acc = new HashSet<>();
        nonOpVarsMentioned(acc, exprs);
        return acc;
    }

    public static void nonOpVarsMentioned(Collection<Var> acc, ExprList exprs) {
        exprs.forEach(e->nonOpVarsMentioned(acc, e));
    }

    // Names variants

    public static Set<String> getVarNamesMentioned(Expr expr) {
        Set<String> acc = new HashSet<>() ;
        varNamesMentioned(acc, expr) ;
        return acc ;
    }

    public static Set<String> getNonOpVarNamesMentioned(Expr expr) {
        Set<String> acc = new HashSet<>() ;
        nonOpVarNamesMentioned(acc, expr) ;
        return acc ;
    }

    private static Action<String> accVarName = (a, var) -> a.add(var.getVarName());

    public static void varNamesMentioned(Collection<String> acc, Expr expr) {
        ExprVisitor vv = new ExprVarsWorker<>(acc, accVarName);
        Walker.walk(expr, vv);
    }

    public static void nonOpVarNamesMentioned(Collection<String> acc, Expr expr) {
        ExprVisitor vv = new ExprNoOpVarsWorker<>(acc, accVarName);
        Walker.walk(expr, vv);
    }

    public static Set<Var> getVarsMentioned(SortCondition sortCondition) {
        Set<Var> acc = new HashSet<>();
        varsMentioned(acc, sortCondition);
        return acc;
    }

    public static Set<Var> getVarsMentioned(Collection<SortCondition> sortConditions) {
        Set<Var> acc = new HashSet<>() ;
        varsMentioned(acc, sortConditions) ;
        return acc ;
    }

    public static  void varsMentioned(Collection<Var> acc, SortCondition sortCondition) {
        varsMentioned(acc, sortCondition.getExpression());
    }

    public static void varsMentioned(Collection<Var> acc, Collection<SortCondition> sortConditions) {
        for (SortCondition sc : sortConditions )
            varsMentioned(acc, sc) ;
    }

    static class ExprNoOpVarsWorker<T>  extends ExprVisitorBase
    {
        protected final Collection<T> acc ;
        protected final Action<T> action ;

        public ExprNoOpVarsWorker(Collection<T> acc, Action<T> action)
        { this.acc = acc ; this.action = action ; }

        @Override
        public void visit(ExprVar nv)
        { action.var(acc, nv.asVar()) ; }

        @Override
        public void visit(ExprTripleTerm exTripleTerm)
        {
            Triple t = exTripleTerm.getTriple();
            process(t);
        }

        private void process(Triple t) {
            process(t.getSubject());
            process(t.getPredicate());
            process(t.getObject());
        }

        private void process(Node node) {
            if ( Var.isVar(node) ) {
                action.var(acc, Var.alloc(node));
                return;
            }
            if ( node.isNodeTriple() )
                process(node.getTriple());
        }
    }

    static class ExprVarsWorker<T> extends ExprNoOpVarsWorker<T>
    {
        public ExprVarsWorker(Collection<T> acc, Action<T> action) {
            super(acc, action);
        }

        // Also include variables in ExprFunctionOp : EXISTS and NOT EXISTS
        @Override
        public void visit(ExprFunctionOp funcOp)
        {
            Collection<Var> vars = OpVars.visibleVars(funcOp.getGraphPattern()) ;
            for ( Var v : vars )
                action.var(acc, v) ;
        }

    }


}
