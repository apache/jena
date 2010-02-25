/*
 * (c) 2010 Talis Information Ltd
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr;

import com.hp.hpl.jena.sparql.ARQNotImplemented ;


public class E_Cast extends ExprFunction2
{
    // See E_StrDatatype
    private static final String symbol = "cast" ;

    private E_Cast(Expr expr1, Expr expr2)
    {
        super(expr1, expr2, symbol) ;
    }

    @Override
    public NodeValue eval(NodeValue x, NodeValue y)
    {
        if ( ! x.isString() ) throw new ExprEvalException("cast: arg 2 is not a string: "+x) ;
        if ( ! y.isIRI() ) throw new ExprEvalException("cast: arg 2 is not a URI: "+y) ;
        
        String lex = x.getString() ;
        y.asNode().getURI() ;
        
        throw new ARQNotImplemented() ;
    }

    @Override
    public Expr copy(Expr arg1, Expr arg2)
    { return new E_Cast(arg1, arg2) ; }
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
