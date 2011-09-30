/**
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

import java.util.ArrayList ;
import java.util.Collection ;
import java.util.HashSet ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Set ;

import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.util.Context ;

public class ExprList implements Iterable<Expr>
{
    private final List<Expr> expressions ;
    
    public ExprList() { expressions = new ArrayList<Expr>() ; }
    public ExprList(ExprList other) { this() ; expressions.addAll(other.expressions) ; }
    public ExprList(Expr expr)
    {
        this() ;
        expressions.add(expr) ;
    }
    
    public ExprList(List<Expr> x)   { expressions = x ; }

    public boolean isSatisfied(Binding binding, ExecutionContext execCxt)
    {
        for (Expr expr : expressions)
        {
            if ( ! expr.isSatisfied(binding, execCxt) )
                return false ;
        }
        return true ;
    }
    
    public Expr get(int idx)    { return expressions.get(idx) ; }
    public int size()           { return expressions.size() ; }
    public boolean isEmpty()    { return expressions.isEmpty() ; }
    public ExprList subList(int fromIdx, int toIdx)     { return new ExprList(expressions.subList(fromIdx, toIdx)) ; }
    public ExprList tail(int fromIdx)                   { return subList(fromIdx, expressions.size()) ; }
    
    public Set<Var> getVarsMentioned()
    {
        Set<Var> x = new HashSet<Var>() ;
        varsMentioned(x) ;
        return x ;
    }
    
    public void varsMentioned(Collection<Var> acc)
    {
        for (Expr expr : expressions)
            expr.varsMentioned(acc) ;
    }
    
    public ExprList copySubstitute(Binding binding) { return copySubstitute(binding, false) ; }
    public ExprList copySubstitute(Binding binding, boolean foldConstants)
    {
        ExprList x = new ExprList() ;
        for ( Iterator<Expr> iter = expressions.iterator() ; iter.hasNext() ; )
        {
            Expr expr = iter.next();
            expr = expr.copySubstitute(binding, foldConstants) ;
            x.add(expr) ;
        }
        return x ;
    }
    public void addAll(ExprList exprs) { expressions.addAll(exprs.getList()) ; }
    public void add(Expr expr) { expressions.add(expr) ; }
    public List<Expr> getList() { return expressions ; }
    public Iterator<Expr> iterator() { return expressions.iterator() ; }
    
    public void prepareExprs(Context context)
    {
        ExprBuild build = new ExprBuild(context) ;
        // Give each expression the chance to set up (bind functions)
        for (Expr expr : expressions)
            ExprWalker.walk(build, expr) ;
    }
    
    @Override
    public String toString()
    { return expressions.toString() ; }
    
    @Override
    public int hashCode() { return expressions.hashCode() ; }

    @Override
    public boolean equals(Object other)
    {
        if ( this == other ) return true ;
        if ( ! ( other instanceof ExprList ) ) return false ;
        ExprList exprs = (ExprList)other ;
        return expressions.equals(exprs.expressions) ;
    }
    public static ExprList splitConjunction(ExprList exprList1)
    {
        ExprList exprList2 = new ExprList() ;
        for (Expr expr : exprList1)
            split(exprList2, expr) ;
        return exprList2 ;
    }
    
    private static ExprList splitConjunction(Expr expr)
    {
        ExprList exprList = new ExprList() ;
        split(exprList, expr) ;
        return exprList ;
    }
    
    private static void split(ExprList exprList, Expr expr)
    {
        // Explode &&-chain to exprlist.
        while ( expr instanceof E_LogicalAnd )
        {
            E_LogicalAnd x = (E_LogicalAnd)expr ;
            Expr left = x.getArg1() ;
            Expr right = x.getArg2() ;
            split(exprList, left) ;
            expr = right ;
        }
        // Drop through and add remaining
        exprList.add(expr) ;
    }
}
