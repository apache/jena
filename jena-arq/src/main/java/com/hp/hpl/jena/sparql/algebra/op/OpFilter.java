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

package com.hp.hpl.jena.sparql.algebra.op;

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.OpVisitor ;
import com.hp.hpl.jena.sparql.algebra.Transform ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprList ;
import com.hp.hpl.jena.sparql.sse.Tags ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;

public class OpFilter extends Op1
{
    protected ExprList expressions ;
    
    /** Add expression - mutates an existing filter */  
    public static Op filter(Expr expr, Op op)
    {
        OpFilter f = filter(op) ;
        f.getExprs().add(expr) ;
        return f ;
    }
    
    public static OpFilter filter(Op op)
    {
        if ( op instanceof OpFilter )
           return (OpFilter)op ;
        else
           return new OpFilter(op) ;  
    }
    
    /** Add expressions - mutates an existing filter */  
    public static Op filter(ExprList exprs, Op op)
    {
        if ( exprs.isEmpty() )
            return op ;
        OpFilter f = filter(op) ;
        f.getExprs().addAll(exprs) ;
        return f ;
    }
    
    /** Make a OpFilter - guaranteed to return an OpFilter */
    public static OpFilter filterDirect(ExprList exprs, Op op)
    {
        return new OpFilter(exprs, op) ;
    }

    
    private OpFilter(Op sub)
    { 
        super(sub) ;
        expressions = new ExprList() ;
    }
    
    private OpFilter(ExprList exprs , Op sub)
    { 
        super(sub) ;
        expressions = exprs ;
    }
    
    /** Compress multiple filters:  (filter (filter (filter op)))) into one (filter op) */ 
    public static OpFilter tidy(OpFilter base)
    {
        ExprList exprs = new ExprList() ;
        
        Op op = base ; 
        while ( op instanceof OpFilter )
        {
            OpFilter f = (OpFilter)op ;
            exprs.addAll(f.getExprs()) ;
            op = f.getSubOp() ;
        }
        return new OpFilter(exprs, op) ;
    }
    
    public ExprList getExprs() { return expressions ; }
    
    @Override
    public String getName() { return Tags.tagFilter ; }
    
    @Override
    public Op apply(Transform transform, Op subOp)
    { return transform.transform(this, subOp) ; }

    @Override
    public void visit(OpVisitor opVisitor) { opVisitor.visit(this) ; }
    
    @Override
    public Op1 copy(Op subOp)                { return new OpFilter(expressions, subOp) ; }
    
    @Override
    public int hashCode()
    {
        return expressions.hashCode() ;
    }
    
    @Override
    public boolean equalTo(Op other, NodeIsomorphismMap labelMap)
    {
        if ( ! (other instanceof OpFilter) ) return false ;
        OpFilter opFilter = (OpFilter)other ;
        if ( ! expressions.equals(opFilter.expressions) )
            return false ;
        
        return getSubOp().equalTo(opFilter.getSubOp(), labelMap) ;
    }
}
