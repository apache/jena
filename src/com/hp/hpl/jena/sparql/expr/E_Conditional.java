/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Epimorphics Ltd.
 * (c) Copyright 2010 Talis Systems Ltd.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr;

import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;

/** IF(expr, expr, expr) */ 

public class E_Conditional extends ExprFunction3
{
    private static final String printName = "if" ;
    
    private final Expr condition ;
    private final Expr thenExpr ;
    private final Expr elseExpr ;
    
    public E_Conditional(Expr condition, Expr thenExpr, Expr elseExpr)
    {
        // Don't let the parent eval the theEpxr or ifExpr.
        super(condition, thenExpr, elseExpr, printName) ;
        // Better names,
        this.condition = condition ;
        this.thenExpr = thenExpr ;
        this.elseExpr = elseExpr ;
    }

    @Override
    public Expr copy(Expr arg1, Expr arg2, Expr arg3)
    {
        return new E_Conditional(arg1, arg2, arg3) ;
    }

    /** Special form evaluation (example, don't eval the arguments first) */
    @Override
    protected NodeValue evalSpecial(Binding binding, FunctionEnv env)
    {
        NodeValue nv = condition.eval(binding, env) ;
        if ( condition.isSatisfied(binding, env) )
            return thenExpr.eval(binding, env) ;
        else
            return elseExpr.eval(binding, env) ;
    }
    
    @Override
    public NodeValue eval(NodeValue x, NodeValue y, NodeValue z)
    {
        throw new ARQInternalErrorException() ;
    }
}

/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd.
 * (c) Copyright 2010 Epimorphics Ltd.
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
