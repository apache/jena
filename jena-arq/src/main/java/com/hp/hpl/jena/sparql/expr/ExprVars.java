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

package com.hp.hpl.jena.sparql.expr;

import java.util.Collection ;
import java.util.HashSet ;
import java.util.Set ;

import com.hp.hpl.jena.query.SortCondition ;
import com.hp.hpl.jena.sparql.algebra.OpVars ;
import com.hp.hpl.jena.sparql.core.Var ;

public class ExprVars
{
    interface Action<T> { void var(Collection<T> acc, Var var) ; }

    public static Set<Var> getVarsMentioned(Expr expr)
    {
        Set<Var> acc = new HashSet<>() ;
        varsMentioned(acc, expr) ;
        return acc ;
    }

    public static Set<Var> getVarsMentioned(ExprList exprs)
    {
        Set<Var> acc = new HashSet<>() ;
        for ( Expr expr : exprs )
            varsMentioned(acc, expr) ;    
        return acc ;
    }

    public static void varsMentioned(Collection<Var> acc, Expr expr)
    {
        ExprVars.Action<Var> action =
            new ExprVars.Action<Var>(){
                @Override
                public void var(Collection<Var> acc, Var var)
                {
                    acc.add(var) ;
                }
            } ;
        ExprVarsWorker<Var> vv = new ExprVarsWorker<>(acc, action) ;
        ExprWalker.walk(vv, expr) ;
    }
    
    public static Set<String> getVarNamesMentioned(Expr expr)
    {
        Set<String> acc = new HashSet<>() ;
        varNamesMentioned(acc, expr) ;
        return acc ;
    }
    
    public static void varNamesMentioned(Collection<String> acc, Expr expr)
    {
        ExprVars.Action<String> action =
            new ExprVars.Action<String>(){
                @Override
                public void var(Collection<String> acc, Var var)
                {
                    acc.add(var.getVarName()) ;
                }
            } ;
        ExprVarsWorker<String> vv = new ExprVarsWorker<>(acc, action) ;
        ExprWalker.walk(vv, expr) ;
    }
    
    
    public static Set<Var> getVarsMentioned(SortCondition sortCondition) {
        Set<Var> acc = new HashSet<>() ;
        varsMentioned(acc, sortCondition) ;
        return acc ;
    }
    
    public static Set<Var> getVarsMentioned(Collection<SortCondition> sortConditions) {
        Set<Var> acc = new HashSet<>() ;
        varsMentioned(acc, sortConditions) ;
        return acc ;
    }

    public static  void varsMentioned(Collection<Var> acc, SortCondition sortCondition) {
        sortCondition.getExpression().varsMentioned(acc) ;
    }

    public static void varsMentioned(Collection<Var> acc, Collection<SortCondition> sortConditions) {
        for (SortCondition sc : sortConditions )
            varsMentioned(acc, sc) ;
    }

    static class ExprVarsWorker<T> extends ExprVisitorBase
    {
        final Collection<T> acc ;
        final Action<T> action ;
        
        public ExprVarsWorker(Collection<T> acc, Action<T> action)
        { this.acc = acc ; this.action = action ; }
        
        @Override
        public void visit(ExprVar nv)
        { action.var(acc, nv.asVar()) ; }
        
        @Override
        public void visit(ExprFunctionOp funcOp)
        { 
            Collection<Var> vars = OpVars.visibleVars(funcOp.getGraphPattern()) ;
            
            for ( Var v : vars )
                action.var(acc, v) ;
        }
        
    }
}
