/*
 * (c) Copyright 2010 Talis Information Ltd.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr;

import com.hp.hpl.jena.graph.Node ;

/** Create a literal from lexical form and datatype URI */ 
public class E_StrDatatype extends ExprFunction2
{
    private static final String symbol = "strdt" ;

    public E_StrDatatype(Expr expr1, Expr expr2)
    {
        super(expr1, expr2, symbol) ;
    }
    
    @Override
    public NodeValue eval(NodeValue v1, NodeValue v2)
    { 
        if ( ! v1.isString() ) throw new ExprEvalException("Not a string (arg 1): "+v1) ;
        if ( ! v2.isIRI() ) throw new ExprEvalException("Not an IRI (arg 2): "+v2) ;
        
        String lex = v1.asString() ;
        Node dt = v2.asNode() ;
        // Check?
        
        Node n = Node.createLiteral(lex, null, Node.getType(dt.getURI())) ;
        return NodeValue.makeNode(n) ; 
    }
    
    @Override
    public Expr copy(Expr expr1, Expr expr2) { return new E_StrDatatype(expr1, expr2) ; } 
}

/*
 *  (c) Copyright 2010 Talis Information Ltd.
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
