/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra.op;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVisitor;
import com.hp.hpl.jena.sparql.algebra.Transform;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.sse.Tags;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;

public class OpLeftJoin extends Op2
{
    ExprList expressions = null ;
    
    public static Op create(Op left, Op right, ExprList exprs)
    { 
        return new OpLeftJoin(left, right, exprs) ;
    }
    
    public static Op create(Op left, Op right, Expr expr)
    { 
        return new OpLeftJoin(left, right, expr == null ? null : new ExprList(expr)) ;
    }

    protected OpLeftJoin(Op left, Op right, ExprList exprs) 
    { 
        super(left, right) ;
        expressions = exprs ;
    }
    
    public ExprList getExprs()      { return expressions ; } 
    public String getName()         { return Tags.tagLeftJoin ; }
    
    @Override
    public Op apply(Transform transform, Op left, Op right)
    { return transform.transform(this, left, right) ; }
        
    public void visit(OpVisitor opVisitor) { opVisitor.visit(this) ; }
    @Override
    public Op copy(Op newLeft, Op newRight)
    { return new OpLeftJoin(newLeft, newRight, expressions) ; }
    
    @Override
    public boolean equalTo(Op op2, NodeIsomorphismMap labelMap)
    {
        if ( ! ( op2 instanceof OpLeftJoin) ) return false ;
        return super.sameArgumentsAs((Op2)op2, labelMap) ;
    }
}

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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