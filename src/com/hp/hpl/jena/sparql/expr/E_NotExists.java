/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr;

import com.hp.hpl.jena.sparql.algebra.Algebra ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.core.Substitute ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;
import com.hp.hpl.jena.sparql.graph.NodeTransform ;
import com.hp.hpl.jena.sparql.graph.NodeTransformLib ;
import com.hp.hpl.jena.sparql.syntax.Element ;

public class E_NotExists extends ExprFunctionOp
{
    // Translated to "(not (exists (...)))" 
    private static final String symbol = "notexists" ;

    public E_NotExists(Op op)
    {
        this(null, op) ;
    }
    
    public E_NotExists(Element elt)
    {
        this(elt, Algebra.compile(elt)) ;
    }
    
    public E_NotExists(Element el, Op op)
    {
        super(symbol, el, op) ;
    }

    @Override
    public Expr copySubstitute(Binding binding, boolean foldConstants)
    {
        // Does not pass down fold constants.  Oh well.
        Op op2 = Substitute.substitute(getGraphPattern(), binding) ;
        return new E_NotExists(getElement(), op2) ;
    }

    @Override
    public Expr applyNodeTransform(NodeTransform nodeTransform)
    {
        Op op2 = NodeTransformLib.transform(nodeTransform, getGraphPattern()) ;
        return new E_NotExists(getElement(), op2) ;
    }
    
    @Override
    protected NodeValue eval(Binding binding, QueryIterator qIter, FunctionEnv env)
    {
        boolean b = qIter.hasNext() ;
        return NodeValue.booleanReturn(!b) ;
    }

    @Override
    public int hashCode()
    {
        return symbol.hashCode() ^ getGraphPattern().hashCode() ;
    }
    
    @Override
    public boolean equals(Object other)
    {
        if ( this == other ) return true ;

        if ( ! ( other instanceof E_NotExists ) )
            return false ;
        
        E_NotExists ex = (E_NotExists)other ;
        return this.getGraphPattern().equals(ex.getGraphPattern()) ;
    }
    
    @Override
    public ExprFunctionOp copy(ExprList args, Op x) { return new E_NotExists(x) ; }

}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd
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