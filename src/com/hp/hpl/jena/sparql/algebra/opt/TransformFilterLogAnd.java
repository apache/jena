/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra.opt;

import java.util.Iterator;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.TransformCopy;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.expr.E_LogicalAnd;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;

/** Redo FILTER (A&&B) as FILTER(A) FILTER(B) via multiple elements of the exprList of the OpFilter */

public class TransformFilterLogAnd extends TransformCopy
{
    public TransformFilterLogAnd() {}
    
    public Op transform(OpFilter opFilter, Op subOp)
    {
        ExprList exprList = opFilter.getExprs() ;
        ExprList exprList2 = new ExprList() ;
        
        for ( Iterator iter = exprList.iterator() ; iter.hasNext() ; )
        {
            Expr expr = (Expr)iter.next() ;
            mergeExprList(exprList2, expr) ;
        }
        
        return OpFilter.filter(exprList2, subOp) ;
    }
    
    private static ExprList asExprList(Expr expr)
    {
        ExprList exprList = new ExprList() ;
        mergeExprList(exprList, expr) ;
        return exprList ;
    }
    
    private static void mergeExprList(ExprList exprList, Expr expr)
    {
        // Explode &&-chain to exprlist.
        while ( expr instanceof E_LogicalAnd )
        {
            E_LogicalAnd x = (E_LogicalAnd)expr ;
            Expr left = x.getArg1() ;
            Expr right = x.getArg2() ;
            mergeExprList(exprList, left) ;
            expr = right ;
        }
        // Drop through and add remaining
        exprList.add(expr) ;
    }
}
    
/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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