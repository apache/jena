/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.core;

import java.util.*;

import com.hp.hpl.jena.graph.Node;

import com.hp.hpl.jena.sparql.ARQInternalErrorException;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprEvalException;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionEnv;
import com.hp.hpl.jena.sparql.util.Utils;

public class VarExprList
{
    private List<Var> vars  ;
    private Map<Var, Expr> exprs  ;
    
    public VarExprList(List<Var> vars)
    {
        this.vars = vars ;
        this.exprs = new HashMap<Var, Expr>() ;
    }
    
    public VarExprList(VarExprList other)
    {
        this.vars = new ArrayList<Var>(other.vars) ; 
        this.exprs = new HashMap<Var, Expr>(other.exprs) ;
    }

    public VarExprList()
    {
        this.vars = new ArrayList<Var>() ;
        this.exprs = new HashMap<Var, Expr>() ;
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
        //{ ALog.warn(this, "Eval failure "+expr+": "+ex.getMessage()) ; }
        { }
        return null ;
    }
    
    public void add(Var var)
    {
        if ( ! vars.contains(var) )
            vars.add(var) ;
    }

    public void add(Var var, Expr expr)
    {
        if ( var == null )
            throw new ARQInternalErrorException("Attempt to add a named expression with a null variable") ;
        if ( exprs.containsKey(var) )
            throw new ARQInternalErrorException("Attempt to assign an expression again") ;
        add(var) ; 
        exprs.put(var, expr) ;
    }
    
    public void addAll(VarExprList other)
    {
        for ( Iterator<Var> iter = other.vars.iterator() ; iter.hasNext() ; )
        {
            Var v = iter.next () ;
            Expr e = other.getExpr(v) ;
            add(v, e) ;
        }
    }

    public int size() { return vars.size() ; }
    public boolean isEmpty() { return vars.isEmpty() ; } 
    
    @Override
    public int hashCode()
    { 
        int x = vars.hashCode() ^ exprs.hashCode() ;
        
        return vars.hashCode() ^ exprs.hashCode() ; }
    
    @Override
    public boolean equals(Object other)
    {
        if ( this == other) return true ;
        if ( ! ( other instanceof VarExprList ) )
            return false ;
        VarExprList x = (VarExprList)other ;
        return Utils.equal(vars, x.vars) &&  Utils.equal(exprs, x.exprs) ;
    }
    
    @Override
    public String toString()
    {
        return vars.toString() + " // "+exprs.toString();
    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */