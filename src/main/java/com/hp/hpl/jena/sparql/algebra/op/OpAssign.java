/**
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

public class OpAssign extends Op1
{
    private VarExprList assignments ;
    
    // There factory operations compress nested assignments if possible.
    // Not possible if it's the reassignment of something already assigned.
    // Or we could implement something like (let*).
    
    static public Op assign(Op op, Var var, Expr expr)
    {
        if ( ! ( op instanceof OpAssign ) )
            return createAssign(op, var, expr) ;
        
        OpAssign opAssign = (OpAssign)op ;
        if ( opAssign.assignments.contains(var) )
            return createAssign(op, var, expr) ;

        opAssign.assignments.add(var, expr) ;
        return opAssign ;
    }
    
    static public Op assign(Op op, VarExprList exprs)
    {
        if ( ! ( op instanceof OpAssign ) )
            return createAssign(op, exprs) ;
            
        OpAssign opAssign = (OpAssign)op ;
        for ( Var var : exprs.getVars() )
        {
            if ( opAssign.assignments.contains(var) )
                return createAssign(op, exprs) ;
        }
            
        opAssign.assignments.addAll(exprs) ;
        return opAssign ;
    }
    
    /** Make a OpAssign - guaranteed to return an OpFilter */
    public static OpAssign assignDirect(Op op, VarExprList exprs)
    {
        return new OpAssign(op, exprs) ;
    }

    static private Op createAssign(Op op, Var var, Expr expr)
    {
        VarExprList x = new VarExprList() ;
        x.add(var, expr) ;
        return new OpAssign(op, x) ;
    }   
    
    static private Op createAssign(Op op, VarExprList exprs)
    {
        // Create, copying the var-expr list
        VarExprList x = new VarExprList() ;
        x.addAll(exprs) ;
        return new OpAssign(op, x) ;
    }   
    
    private OpAssign(Op subOp)
    {
        super(subOp) ;
        assignments = new VarExprList() ;
    }
    
    private OpAssign(Op subOp, VarExprList exprs)
    {
        super(subOp) ;
        assignments = exprs ;
    }
    
    @Override
    public String getName() { return Tags.tagAssign ; }
    
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
    public Op copy(Op subOp)
    {
        OpAssign op = new OpAssign(subOp, new VarExprList(getVarExprList())) ;
        return op ;
    }

    @Override
    public boolean equalTo(Op other, NodeIsomorphismMap labelMap)
    {
        if ( ! ( other instanceof OpAssign) )
            return false ;
        OpAssign assign = (OpAssign)other ;
        
        if ( ! Lib.equal(assignments, assign.assignments) )
            return false ;
        return getSubOp().equalTo(assign.getSubOp(), labelMap) ;
    }

    @Override
    public Op apply(Transform transform, Op subOp)
    { return transform.transform(this, subOp) ; }
}
