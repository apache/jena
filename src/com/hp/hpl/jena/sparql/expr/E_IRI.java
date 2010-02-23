/*
 * (c) 2010 Talis Information Ltd
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.iri.IRI ;
import com.hp.hpl.jena.iri.IRIFactory ;

public class E_IRI extends ExprFunction1
{
    private static final String symbol = "IRI" ;
    private static final IRIFactory iriFactory = IRIFactory.iriImplementation() ;

    public E_IRI(Expr expr)
    {
        super(expr, symbol) ;
    }

    public E_IRI(Expr expr, String altSymbol)
    {
        super(expr, altSymbol) ;
    }
    
    @Override
    public NodeValue eval(NodeValue v)
    { 
        if ( v.isIRI() )
            return v ;
        if ( v.isString() )
        {
            // Level of checking?
            IRI iri = iriFactory.create(v.getString()) ;
            if ( ! iri.isAbsolute() )
                throw new ExprEvalException("Relative IRI string: "+v.getString()) ;
            return NodeValue.makeNode(Node.createURI(iri.toString())) ;
        }
        throw new ExprEvalException("Can't make an IRI from "+v) ;
    }
    
    @Override
    public Expr copy(Expr expr) { return new E_IRI(expr) ; } 
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
