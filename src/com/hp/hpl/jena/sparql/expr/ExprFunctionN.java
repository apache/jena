/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr;

import com.hp.hpl.jena.sparql.engine.binding.Binding;

/** A function in the expression hierarchy.
 *  Everything that is evaluable (i.e. not NodeValue, NodeVar) is a function.
 *  It is useful to distinguish between values, vars and functions.
 */
 
public abstract class ExprFunctionN extends ExprFunction
{
    protected ExprList args = null ;
    
    protected ExprFunctionN(String fName, ExprList args)
    {
        super(fName) ;
        this.args = args ;
    }

    @Override
    public Expr getArg(int i)
    {
        i = i-1 ;
        if ( args.size() <= i )
            return null ;
        return args.get(i) ;
    }

    @Override
    public int numArgs() { return args.size() ; }
    
    //public List getArgs() { return args.getList() ; }

    @Override
    public Expr copySubstitute(Binding binding, boolean foldConstants)
    {
        ExprList newArgs = new ExprList() ;
        for ( int i = 1 ; i <= numArgs() ; i++ )
        {
            Expr e = getArg(i) ;
            e = e.copySubstitute(binding, foldConstants) ;
            newArgs.add(e) ;
        }
        return copy(newArgs) ;
    }

    protected abstract Expr copy(ExprList newArgs) ;
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
