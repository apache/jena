/*
 * (c) Copyright 2010 Talis Information Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr;

import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;

public abstract class E_OneOfBase extends ExprFunctionN
{
    protected final Expr expr ;
    protected final ExprList possibleValues ;
    
    protected E_OneOfBase(String name, Expr expr, ExprList args)
    {
        super(name, fixup(expr, args)) ;
        this.expr = expr ;
        this.possibleValues = args ;
    }
    
    // All ArgList, first arg is the expression.
    protected E_OneOfBase(String name, ExprList args)
    {
        super(name, args) ;
        ExprList x = new ExprList(args) ;
        this.expr = x.get(0) ;
        x.getList().remove(0) ;
        this.possibleValues = x ;
    }
    
    private static ExprList fixup(Expr expr2, ExprList args)
    {
        ExprList allArgs = new ExprList(expr2) ;
        allArgs.addAll(args) ;
        return allArgs ;
    }

    public Expr getLHS() { return expr ; }
    public ExprList getRHS() { return possibleValues ; }

    protected boolean evalOneOf(Binding binding, FunctionEnv env)
    {
        // Special form.
        // Like ( expr = expr1 ) || ( expr = expr2 ) || ...

        NodeValue nv = expr.eval(binding, env) ;
        ExprEvalException error = null ;
        for ( Expr inExpr : possibleValues )
        {
            try {
                NodeValue maybe = inExpr.eval(binding, env) ;
                if ( NodeValue.sameAs(nv, maybe) )
                    return true ;
            } catch (ExprEvalException ex)
            {
                error = ex ;
            }
        }
        if ( error != null )
            throw error ;
        return false ;
    }
    
    protected boolean evalNotOneOf(Binding binding, FunctionEnv env)
    {
        return ! evalOneOf(binding, env) ;
    }
}

/*
 * (c) Copyright 2010 Talis Information Ltd.
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