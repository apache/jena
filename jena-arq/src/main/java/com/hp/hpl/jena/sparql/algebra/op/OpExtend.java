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

import org.openjena.atlas.lib.Lib ;

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.OpVisitor ;
import com.hp.hpl.jena.sparql.algebra.Transform ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.core.VarExprList ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.sse.Tags ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;

/** This is the operation in stadard SPARQL 1.1
 *  OpAssign is specifically in support of LET.
 */
public class OpExtend extends Op1
{
    private VarExprList assignments ;
    
    // There factory operations compress nested assignments if possible.
    // Not possible if it's the reassignment of something already assigned.
    // Or we could implement something like (let*).
    
    static public Op extend(Op op, Var var, Expr expr)
    {
        if ( ! ( op instanceof OpExtend ) )
            return createExtend(op, var, expr) ;
        
        OpExtend opAssign = (OpExtend)op ;
        if ( opAssign.assignments.contains(var) )
            return createExtend(op, var, expr) ;

        opAssign.assignments.add(var, expr) ;
        return opAssign ;
    }
    
    static public Op extend(Op op, VarExprList exprs)
    {
        if ( ! ( op instanceof OpExtend ) )
            return createExtend(op, exprs) ;
            
        OpExtend opAssign = (OpExtend)op ;
        for ( Var var : exprs.getVars() )
        {
            if ( opAssign.assignments.contains(var) )
                return createExtend(op, exprs) ;
        }
            
        opAssign.assignments.addAll(exprs) ;
        return opAssign ;
    }
    
    /** Make a OpExtend - guaranteed to return an OpExtend */
    public static OpExtend extendDirect(Op op, VarExprList exprs)
    {
        return new OpExtend(op, exprs) ;
    }

    static private Op createExtend(Op op, Var var, Expr expr)
    {
        VarExprList x = new VarExprList() ;
        x.add(var, expr) ;
        return new OpExtend(op, x) ;
    }   
    
    static private Op createExtend(Op op, VarExprList exprs)
    {
        // Create, copying the var-expr list
        VarExprList x = new VarExprList() ;
        x.addAll(exprs) ;
        return new OpExtend(op, x) ;
    }   
    
    private OpExtend(Op subOp)
    {
        super(subOp) ;
        assignments = new VarExprList() ;
    }
    
    private OpExtend(Op subOp, VarExprList exprs)
    {
        super(subOp) ;
        assignments = exprs ;
    }
    
    @Override
    public String getName() { return Tags.tagExtend ; }
    
    // Need to protect this with checking for var already used.
    // See the factories in statics above. 
    private void add(Var var, Expr expr)
    { assignments.add(var, expr) ; }

    public VarExprList getVarExprList() { return assignments ; }

    @Override
    public int hashCode()
    { return assignments.hashCode() ^ getSubOp().hashCode() ; }

    @Override
    public void visit(OpVisitor opVisitor)
    { opVisitor.visit(this) ; }

    @Override
    public Op1 copy(Op subOp)
    {
        OpExtend op = new OpExtend(subOp, new VarExprList(getVarExprList())) ;
        return op ;
    }

    @Override
    public boolean equalTo(Op other, NodeIsomorphismMap labelMap)
    {
        if ( ! ( other instanceof OpExtend) )
            return false ;
        OpExtend assign = (OpExtend)other ;
        
        if ( ! Lib.equal(assignments, assign.assignments) )
            return false ;
        return getSubOp().equalTo(assign.getSubOp(), labelMap) ;
    }

    @Override
    public Op apply(Transform transform, Op subOp)
    { return transform.transform(this, subOp) ; }
}
