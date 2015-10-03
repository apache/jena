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

package org.apache.jena.sparql.algebra.optimize;

import static org.apache.jena.sparql.expr.NodeValue.FALSE ;
import static org.apache.jena.sparql.expr.NodeValue.TRUE ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.algebra.TransformCopy ;
import org.apache.jena.sparql.algebra.op.OpFilter ;
import org.apache.jena.sparql.algebra.op.OpLeftJoin ;
import org.apache.jena.sparql.expr.* ;

public class TransformExpandOneOf extends TransformCopy
{
    public TransformExpandOneOf() {}
    
    // Expressions to expand can be in two places: OpFilter and the expr of OpLeftJoin. 
    
    @Override
    public Op transform(OpFilter opFilter, Op subOp)
    {
        ExprList exprList = opFilter.getExprs() ;
        ExprList exprList2 = process(exprList) ;
        if ( exprList2 == null )
            return super.transform(opFilter, subOp) ;
        Op opFilter2 = OpFilter.filter(exprList2, subOp) ;
        return opFilter2 ;
    }
    
    @Override
    public Op transform(OpLeftJoin opLeftJoin, Op opLeft, Op opRight)
    {
        ExprList exprList = opLeftJoin.getExprs() ;
        if ( exprList == null )
            return opLeftJoin ;
        ExprList exprList2 = process(exprList) ;
        if ( exprList2 == null )
            return opLeftJoin ;
        return OpLeftJoin.create(opLeft, opRight, exprList2) ;
    }

    private static ExprList process(ExprList exprList)
    {
        if ( !interesting(exprList) )
            return null ;
        return expand(exprList) ; 
    }
    
    private static boolean interesting(ExprList exprList)
    {
        for ( Expr e : exprList )
        {
            if ( e instanceof E_OneOf ) return true ;
            if ( e instanceof E_NotOneOf ) return true ;
        }
        return false ;
    }

    private static ExprList expand(ExprList exprList)
    {
        ExprList exprList2 = new ExprList() ;
        
        for (  Expr e : exprList)
        {
            if ( e instanceof E_OneOf )
            {
                // ?x IN (a,b) ===> (?x == a) || (?x == b)
                // ?x IN ()    ===> false
                
                E_OneOf exprOneOf = (E_OneOf)e ;
                Expr x = exprOneOf.getLHS() ;
                Expr disjunction = null ;
                // if ?x IN () then it's false regardless.  
                for ( Expr sub : exprOneOf.getRHS() )
                {
                    Expr e2 = new E_Equals(x, sub) ;
                    if ( disjunction == null )
                        disjunction = e2 ;
                    else
                        disjunction = new E_LogicalOr(disjunction, e2) ;
                }
                
                if ( disjunction == null )
                    exprList2.add(FALSE) ;
                else
                    exprList2.add(disjunction) ;
                continue ;
            }
            if ( e instanceof E_NotOneOf )
            {
                // ?x NOT IN (a,b) ===> (?x != a) && (?x != b)
                // ?x NOT IN () ===> TRUE (or nothing)
                E_NotOneOf exprNotOneOf = (E_NotOneOf)e ;
                Expr x = exprNotOneOf.getLHS() ;
                if ( exprNotOneOf.getRHS().size() == 0 )
                    exprList2.add(TRUE) ;
                else
                {
                    for ( Expr sub : exprNotOneOf.getRHS() )
                        exprList2.add(new E_NotEquals(x, sub)) ;
                }
                continue ;
            }
            
            exprList2.add(e) ;
        }
        
        return exprList2 ;
    }
}
