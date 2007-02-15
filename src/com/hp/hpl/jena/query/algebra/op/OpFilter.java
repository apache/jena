/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.algebra.op;

import com.hp.hpl.jena.query.algebra.Op;
import com.hp.hpl.jena.query.engine.ref.Evaluator;
import com.hp.hpl.jena.query.engine.ref.Table;
import com.hp.hpl.jena.query.expr.Expr;
import com.hp.hpl.jena.query.expr.ExprList;

public class OpFilter extends Op1
{
    ExprList expressions ;
    
    public static OpFilter filter(Expr expr, Op op)
    {
        if ( op instanceof OpFilter )
        {
            OpFilter f = (OpFilter)op ;
            f.getExprs().add(expr) ;
            return f ;
        }
        ExprList x = new ExprList(expr) ;
        return new OpFilter(x, op) ;
    }
    
    public static OpFilter filter(ExprList exprs, Op op)
    {
        if ( op instanceof OpFilter )
        {
            OpFilter f = (OpFilter)op ;
            f.getExprs().addAll(exprs) ;
            return f ;
        }
        
        return new OpFilter(exprs, op) ;
    }

    private OpFilter(ExprList exprs , Op sub)
    { 
        super(sub) ;
        expressions = exprs ;
    }
    
    public Table eval_1(Table table, Evaluator evaluator)
    {
        return evaluator.filter(expressions, table) ;
    }

    // Compress a filter(filter(filter(op)))) into one filter(op) 
    public static OpFilter tidy(OpFilter base)
    {
        ExprList exprs = new ExprList() ;
        
        Op op = base ; 
        while ( op instanceof OpFilter )
        {
            OpFilter f = (OpFilter)op ;
            exprs.addAll(f.getExprs()) ;
            //expr = new E_LogicalAnd(expr, f.getExpr()) ;
            op = f.getSubOp() ;
        }
        return OpFilter.filter(exprs, op) ;
    }
    
//    // Drill down a chain of OpFilters.
//    public Op underlyingOp()
//    {
//        Op sub = this ;
//        while ( sub instanceof OpFilter )
//        {
//            OpFilter f = (OpFilter)sub ;
//            sub = f.getSubOp() ;
//        }
//        return sub ;
//    }
    
    public ExprList getExprs() { return expressions ; }
    
    public String getName() { return "Filter" ; }
    
    public Op apply(Transform transform, Op subOp)
    { return transform.transform(this, subOp) ; }

    public void visit(OpVisitor opVisitor) { opVisitor.visit(this) ; }
    
    public Op copy(Op subOp)                { return new OpFilter(expressions, subOp) ; }
}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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