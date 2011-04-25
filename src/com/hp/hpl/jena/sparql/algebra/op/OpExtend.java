/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
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
    
    public String getName() { return Tags.tagExtend ; }
    
    // Need to protect this with checking for var already used.
    // See the factories in statics above. 
    private void add(Var var, Expr expr)
    { assignments.add(var, expr) ; }

    public VarExprList getVarExprList() { return assignments ; }

    @Override
    public int hashCode()
    { return assignments.hashCode() ^ getSubOp().hashCode() ; }

    public void visit(OpVisitor opVisitor)
    { opVisitor.visit(this) ; }

    @Override
    public Op copy(Op subOp)
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

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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