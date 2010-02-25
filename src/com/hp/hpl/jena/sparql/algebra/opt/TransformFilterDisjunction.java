/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra.opt;

import java.util.ArrayList ;
import java.util.List ;

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.TransformCopy ;
import com.hp.hpl.jena.sparql.algebra.op.OpDisjunction ;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter ;
import com.hp.hpl.jena.sparql.expr.E_LogicalOr ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprList ;

/**Filter disjunction.
 * Merge with TransformFilterImprove
 */

public class TransformFilterDisjunction extends TransformCopy
{
    public TransformFilterDisjunction() {}
    
    @Override
    public Op transform(OpFilter opFilter, final Op subOp)
    {
        ExprList exprList = opFilter.getExprs() ;
        
        // First pass - any disjunctions at all?
        boolean processDisjunction = false ;
        for ( Expr expr : exprList )
        {
            if ( isDisjunction(expr) )
            {
                processDisjunction = true ;
                break ;
            }
        }
        
        // Still may be a disjunction in a form we don't optimize. 
        if ( ! processDisjunction )
            return super.transform(opFilter, subOp) ;
        
        ExprList exprList2 = new ExprList() ;
        Op newOp = subOp ;
        
        for ( Expr expr : exprList )
        {
            if ( ! isDisjunction(expr) )
            {
                // Assignment there?
                exprList2.add(expr) ;
                continue ;
            }
            Op op2 = expandDisjunction(expr, newOp) ;
            
            if ( op2 != null )
                newOp = op2 ;
        }

        if ( exprList2.isEmpty() )
            return newOp ;
        
        // Failed.  These a was one or more expressions we coudln't handle.
        // So the full pattern is going to be executed anyway. 
        return super.transform(opFilter, subOp) ;
        
//        // Put the non-disjunctions outside the disjunction and the pattern rewrite. 
//        Op opNew = OpFilter.filter(exprList2, subOp) ;
//        return opNew ;
        //return super.transform((OpFilter)opNew, subOp) ;
    }
    
    private boolean isDisjunction(Expr expr)
    {
        return ( expr instanceof E_LogicalOr ) ; 
    }

    // Todo:
    // 1 - convert TransformEqualityFilter to use ExprLib for testing.
    // 2 - Scan for safe equality filters in disjunction.
    
    public static Op expandDisjunction(Expr expr, Op subOp)
    {
//        if ( !( expr instanceof E_LogicalOr ) )
//            return null ;

        List<Expr> exprList = explodeDisjunction(new ArrayList<Expr>(), expr) ;
        
        // All disjunctions - some can be done efficiently via assignments, some can not.
        // Really should only do if every disjunction can turned into a assign-grounded pattern
        // otherwise the full is done anyway. 
        
        List<Expr> exprList2 = null ;
        Op op = null ;
        for ( Expr e : exprList )
        {
            Op op2 = TransformFilterEquality.processFilter(e, subOp) ;
            if ( op2 == null )
            {
                // Not done.
                if ( exprList2 == null )
                    exprList2 = new ArrayList<Expr>() ;
                exprList2.add(e) ;
                //continue ;
                // Can't do one so don't do any as the original pattern is still executed. 
                
                
                
                
            }

            op = OpDisjunction.create(op, op2) ;
        }
        
        if ( exprList2 != null && !exprList2.isEmpty() )
        {
            // These are left as disjunctions.
            Expr eOther = null ;
            for ( Expr e : exprList2 )
            {
                if ( eOther == null )
                    eOther = e ;
                else
                    eOther = new E_LogicalOr(eOther, e) ;
            }
            Op opOther = OpFilter.filter(eOther, subOp) ;
            op = OpDisjunction.create(op, opOther) ;
        }
        
        return op ;
    }

    /** Explode a expr into a list of disjunctions */
    private static List<Expr> explodeDisjunction(List<Expr> exprList, Expr expr)
    {
        if ( !( expr instanceof E_LogicalOr ) )
        {
            exprList.add(expr) ;
            return exprList ;
        }
        
        E_LogicalOr exprOr = (E_LogicalOr)expr ;
        Expr e1 =  exprOr.getArg1() ;
        Expr e2 =  exprOr.getArg2() ;
        explodeDisjunction(exprList, e1) ; 
        explodeDisjunction(exprList, e2) ;
        return exprList ;
    }
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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