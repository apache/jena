/*
 * (c) 2010 Talis Information Ltd
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr;


public class E_Cast extends E_Call
{
    private static final String symbol = "CAST" ;

    public E_Cast(Expr expr1, Expr expr2)
    {
        this(makeArgList(expr1, expr2)) ;
    }
    
    protected E_Cast(ExprList args)
    {
        super(symbol, args) ;
    }

    private static ExprList makeArgList(Expr expr1, Expr expr2)
    {
        ExprList a = new ExprList() ;
        if ( expr1 != null ) a.add(expr1) ; 
        if ( expr2 != null ) a.add(expr2) ; 
        return a ;
    }
    
    @Override
    protected Expr copy(ExprList newArgs)       { return new E_Cast(newArgs) ; }
}

/*
 * (c) 2010 Talis Information Ltd
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
