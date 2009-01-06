/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr;

import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.function.FunctionEnv;


/** A function of two arguments */
 
public abstract class ExprFunction2 extends ExprFunction
{
    Expr expr1 = null ;
    Expr expr2 = null ;

    protected ExprFunction2(Expr expr1, Expr expr2, String fName) { this(expr1, expr2, fName, null) ; }
    
    protected ExprFunction2(Expr expr1, Expr expr2, String fName, String opSign)
    {
        super(fName, opSign) ;
        this.expr1 = expr1 ;
        this.expr2 = expr2 ;
    }
    
    public Expr getArg1() { return getArg(1) ; }
    public Expr getArg2() { return getArg(2) ; }
    
    @Override
    public Expr getArg(int i)
    {
        if ( i == 1 )
            return expr1 ; 
        if ( i == 2 )
            return expr2 ; 
        return null ;
    }
    
    @Override
    public int numArgs() { return 2 ; }
    
    // ---- Evaluation
    
    @Override
    public int hashCode()
    {
        return getFunctionSymbol().hashCode() ^
               getArg1().hashCode() ^
               getArg2().hashCode() ;
    }

    @Override
    final public NodeValue eval(Binding binding, FunctionEnv env)
    {
        NodeValue s = evalSpecial(binding, env) ;
        if ( s != null )
            return s ;
        
        NodeValue x = expr1.eval(binding, env) ;
        NodeValue y = expr2.eval(binding, env) ;
        return eval(x, y) ;
    }
    
    /** Special form evaluation (example, don't eval the arguments first) */
    protected NodeValue evalSpecial(Binding binding, FunctionEnv env) { return null ; } 
    
    public abstract NodeValue eval(NodeValue x, NodeValue y) ; 

    // ---- Duplication
    
    @Override
    final public Expr copySubstitute(Binding binding, boolean foldConstants)
    {
        Expr e1 = expr1.copySubstitute(binding, foldConstants) ;
        Expr e2 = expr2.copySubstitute(binding, foldConstants) ;
        
        if ( foldConstants)
        {
            try {
                if ( e1.isConstant() && e2.isConstant() )
                    return eval(e1.getConstant(), e2.getConstant()) ;
            } catch (ExprEvalException ex) { /* Drop through */ }
        }
        return copy(e1, e2) ;
    }

    public abstract Expr copy(Expr arg1, Expr arg2) ;
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
