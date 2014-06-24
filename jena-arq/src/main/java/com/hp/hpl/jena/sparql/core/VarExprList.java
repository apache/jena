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

package com.hp.hpl.jena.sparql.core;

import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import org.apache.jena.atlas.lib.Lib ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprEvalException ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;

public class VarExprList
{
    private List<Var> vars  ;
    private Map<Var, Expr> exprs  ;
    
    public VarExprList(List<Var> vars)
    {
        this.vars = vars ;
        this.exprs = new HashMap<>() ;
    }
    
    public VarExprList(VarExprList other)
    {
        this.vars = new ArrayList<>(other.vars) ;
        this.exprs = new HashMap<>(other.exprs) ;
    }

    public VarExprList()
    {
        this.vars = new ArrayList<>() ;
        this.exprs = new HashMap<>() ;
    }
    
    public VarExprList(Var var, Expr expr)
    {
        this() ;
        add(var, expr) ;
    }

    public List<Var> getVars() { return vars ; }
    public Map<Var, Expr> getExprs() { return exprs ; }
    
    public boolean contains(Var var) { return vars.contains(var) ; }
    public boolean hasExpr(Var var) { return exprs.containsKey(var) ; }
    
    public Expr getExpr(Var var) { return exprs.get(var) ; }
    
    // Or Binding.get(var, NamedExprList)
    public Node get(Var var, Binding binding, FunctionEnv funcEnv)
    {
        Expr expr = exprs.get(var) ; 
        if ( expr == null )
            return binding.get(var) ; 
        
        try {
            NodeValue nv = expr.eval(binding, funcEnv) ;
            if ( nv == null )
                return null ;
            return nv.asNode() ;
        } catch (ExprEvalException ex)
        //{ Log.warn(this, "Eval failure "+expr+": "+ex.getMessage()) ; }
        { }
        return null ;
    }
    
    public void add(Var var)
    {
        // Checking here controls whether duplicate variables are allowed.
        // Duplicates with expressions are not allowed (add(Var, Expr))
        // See ARQ.allowDuplicateSelectColumns

        // Every should work either way round if this is enabled.
        // Checking is done in Query for adding result vars, and group vars.
        // if ( vars.contains(var) )
            vars.add(var) ;
    }

    public void add(Var var, Expr expr)
    {
        if ( expr == null )
        {
            add(var) ;
            return ;
        }

        if ( var == null )
            throw new ARQInternalErrorException("Attempt to add a named expression with a null variable") ;
        if ( exprs.containsKey(var) )
            throw new ARQInternalErrorException("Attempt to assign an expression again") ;
        add(var) ; 
        exprs.put(var, expr) ;
    }
    
    public void addAll(VarExprList other)
    {
        for ( Var v : other.vars )
        {
            Expr e = other.getExpr( v );
            add( v, e );
        }
    }

    public int size() { return vars.size() ; }
    public boolean isEmpty() { return vars.isEmpty() ; } 
    
    @Override
    public int hashCode()
    { 
        int x = vars.hashCode() ^ exprs.hashCode() ;
        return x ;
    }
    
    @Override
    public boolean equals(Object other)
    {
        if ( this == other) return true ;
        if ( ! ( other instanceof VarExprList ) )
            return false ;
        VarExprList x = (VarExprList)other ;
        return Lib.equal(vars, x.vars) && Lib.equal(exprs, x.exprs) ;
    }
    
    @Override
    public String toString()
    {
        return vars.toString() + " // "+exprs.toString();
    }
}
