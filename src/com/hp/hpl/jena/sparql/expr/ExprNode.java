/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr;

import java.util.Collection;
import java.util.Set;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.nodevalue.XSDFuncOp;
import com.hp.hpl.jena.sparql.function.FunctionEnv;
import com.hp.hpl.jena.sparql.util.ExprUtils;


/** A node that is a constraint expression that can be evaluated
 * An Expr is already a Constraint - ExprNode is the base implementation
 * of all Expr classes that provides the Constraint machinary.
 * 
 * @author Andy Seaborne
 */
 
public abstract class ExprNode implements Expr
{
    public boolean isSatisfied(Binding binding, FunctionEnv funcEnv)
    {
        try {
            NodeValue v = eval(binding, funcEnv) ;
            boolean b = XSDFuncOp.booleanEffectiveValue(v) ;
            return b ;
        }
        catch (ExprEvalException ex)
        { 
            return false ;
        }
    }

    public boolean isExpr() { return true ; }
    public final Expr getExpr()   { return this ; }
    
    // --- interface Constraint
    
    public abstract NodeValue eval(Binding binding, FunctionEnv env) ; 
    
    public Set<Var> getVarsMentioned() { return ExprVars.getVarsMentioned(this) ; }
    public void varsMentioned(Collection<Var> acc) { ExprVars.varsMentioned(acc, this) ; }

    public Set<String> getVarNamesMentioned() { return ExprVars.getVarNamesMentioned(this) ; }
    public void varNamesMentioned(Collection<String> acc) { ExprVars.varNamesMentioned(acc, this) ; }

    @Override
    public abstract int     hashCode() ;
    @Override
    public abstract boolean equals(Object other) ;
    
    final public Expr copySubstitute(Binding binding)
    { return copySubstitute(binding, false) ; }
    
    final public Expr deepCopy()                     
    { return copySubstitute(null, false) ; }
    
    public abstract Expr copySubstitute(Binding binding, boolean foldConstants) ;
    
    // ---- Default implementations
    public boolean isVariable()        { return false ; }
    public String getVarName()         { return null ; } //throw new ExprException("Expr.getVarName called on non-variable") ; }
    public ExprVar getExprVar()        { return null ; } //throw new ExprException("Expr.getVar called on non-variable") ; }
    public Var asVar()                 { return null ; } //throw new ExprException("Expr.getVar called on non-variable") ; }
    
    public boolean isConstant()        { return false ; }
    public NodeValue getConstant()     { return null ; } // throw new ExprException("Expr.getConstant called on non-constant") ; }
    
    public boolean isFunction()        { return false ; }
    public ExprFunction getFunction()  { return null ; }
    
    public boolean isGraphPattern()    { return false ; }
    public Op getGraphPttern()         { return null ; }
    
    // ---- 
    
    @Override
    public String toString()
    {
        return ExprUtils.fmtSPARQL(this) ; 
    }
}

/*
 *  (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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
