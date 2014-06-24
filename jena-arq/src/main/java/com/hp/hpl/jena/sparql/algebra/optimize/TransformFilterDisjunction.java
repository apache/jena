/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.sparql.algebra.optimize;

import java.util.ArrayList ;
import java.util.HashSet ;
import java.util.List ;
import java.util.Set ;

import org.apache.jena.atlas.logging.Log ;

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
        // remember what's been seen so that FILTER(?x = <x> || ?x = <x> ) does not result in two transforms. 
        Set<Expr> doneSoFar = new HashSet<>() ;
        
        for ( Expr expr : exprList )
        {
            if ( ! isDisjunction(expr) )
            {
                // Assignment there?
                exprList2.add(expr) ;
                continue ;
            }
            
//            // Relies on expression equality.
//            if ( doneSoFar.contains(expr) )
//                continue ;
//            // Must be canonical: ?x = <x> is the same as <x> = ?x
//            doneSoFar.add(expr) ;
            
            Op op2 = expandDisjunction(expr, newOp) ;
            
            if ( op2 != null )
                newOp = op2 ;
        }

        if ( exprList2.isEmpty() )
            return newOp ;

        // There should have been at least on disjunction.
        if ( newOp == subOp ) {
            Log.warn(this, "FilterDisjunction assumption failure: didn't find a disjunction after all") ;
            return super.transform(opFilter, subOp) ;
        }
            

        // Failed.  These a was one or more expressions we couldn't handle.
        // So the full pattern is going to be executed anyway. 
        //return super.transform(super.transform(opFilter, subOp)) ;
        
        
        // Put the non-disjunctions outside the disjunction and the pattern rewrite. 
        Op opOther = OpFilter.filter(exprList2, newOp) ;
        if ( opOther instanceof OpFilter) {
            return opOther ;
        }
            
        // opOther is not a filter any more - should not happen but to isolate from future changes ...
        Log.warn(this, "FilterDisjunction assumption failure: not a filter after processing disjunction/other mix") ;
        return super.transform(opFilter, subOp) ;
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
                    exprList2 = new ArrayList<>() ;
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
