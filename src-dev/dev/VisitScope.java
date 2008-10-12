/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

//import com.hp.hpl.jena.sparql.algebra.OpVisitor;
import java.util.*;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVisitorByType;
import com.hp.hpl.jena.sparql.algebra.op.*;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.Var;

import com.hp.hpl.jena.tdb.lib.NodeLib;

public class VisitScope extends OpVisitorByType
{
    Map<Op, Collection<Var>> defined = new HashMap<Op, Collection<Var>>() ;

    // ---- Default actions
    @Override
    protected void visit0(Op0 op)
    {
        Collection<Var> x =  Collections.emptySet() ;
        defined.put(op,x) ;
    }

    @Override
    protected void visit1(Op1 op)
    {
        defined.put(op, defined.get(op.getSubOp())) ;
    }

    @Override
    protected void visit2(Op2 op)
    {}

    @Override
    protected void visitExt(OpExt op)
    {}

    @Override
    protected void visitN(OpN op)
    {}

    // ---- Compositions
    
    @Override
    public void visit(OpJoin opJoin)
    {
        // IF Walker: will have visited below already.
        opJoin.getLeft().visit(this) ;
        opJoin.getRight().visit(this) ;
        
        Collection<Var> vars = new HashSet<Var>() ;
        vars.addAll(defined.get(opJoin.getLeft())) ;
        vars.addAll(defined.get(opJoin.getRight())) ;
        defined.put(opJoin, vars) ;
    }
    
    @Override
    public void visit(OpLeftJoin opLeftJoin) {}

    @Override
    public void visit(OpUnion opUnion) {}

    @Override
    public void visit(OpGraph opGraph) {}
    
    // ---- Other
    
    @Override
    public void visit(OpFilter opFilter) {}
    
    @Override
    public void visit(OpAssign opAssign) {}

    @Override
    public void visit(OpProject opProject) {}
    
    // ---- Base elements
    
    @Override
    public void visit(OpBGP opBGP)
    {
        Set<Var> acc = new HashSet<Var>() ;
        for ( Triple t : NodeLib.tripleList(opBGP) )
            addVarsFromTriple(acc, t) ;
        defined.put(opBGP, acc) ;
    }
    
    @Override
    public void visit(OpQuadPattern opQuad)
    {
        Set<Var> acc = new HashSet<Var>() ;
        for ( Quad q : NodeLib.quadList(opQuad) )
            addVarsFromQuad(acc, q) ;
        defined.put(opQuad, acc) ;
    }
    
    // ---- Workers
    
    private static void addVarsFromTriple(Set<Var> acc, Triple t)
    {
        addVar(acc, t.getSubject()) ;
        addVar(acc, t.getPredicate()) ;
        addVar(acc, t.getObject()) ;
    }
    
    private static void addVarsFromQuad(Set<Var> acc, Quad q)
    {
        addVar(acc, q.getSubject()) ;
        addVar(acc, q.getPredicate()) ;
        addVar(acc, q.getObject()) ;
        addVar(acc, q.getGraph()) ;
    }
    
    private static void addVar(Set<Var> acc, Node n)
    {
        if ( n == null )
            return ;
        
        if ( Var.isVar(n) )
            acc.add(Var.alloc(n)) ;
    }
    
//
//    @Override
//    public void visit(OpQuadPattern quadPattern)
//    {}
//
//    @Override
//    public void visit(OpTriple opTriple)
//    {}
//
//    @Override
//    public void visit(OpPath opPath)
//    {}
//
//    @Override
//    public void visit(OpTable opTable)
//    {}
//
//    @Override
//    public void visit(OpNull opNull)
//    {}
//
//    @Override
//    public void visit(OpProcedure opProc)
//    {}
//
//    @Override
//    public void visit(OpPropFunc opPropFunc)
//    {}
//
//    @Override
//    public void visit(OpFilter opFilter)
//    {}
//
//    @Override
//    public void visit(OpGraph opGraph)
//    {}
//
//    @Override
//    public void visit(OpService opService)
//    {}
//
//    @Override
//    public void visit(OpDatasetNames dsNames)
//    {}
//
//    @Override
//    public void visit(OpLabel opLabel)
//    {}
//
//    @Override
//    public void visit(OpJoin opJoin)
//    {}
//
//    @Override
//    public void visit(OpSequence opSequence)
//    {}
//
//    @Override
//    public void visit(OpLeftJoin opLeftJoin)
//    {}
//
//    @Override
//    public void visit(OpDiff opDiff)
//    {}
//
//    @Override
//    public void visit(OpUnion opUnion)
//    {}
//
//    @Override
//    public void visit(OpExt opExt)
//    {}
//
//    @Override
//    public void visit(OpList opList)
//    {}
//
//    @Override
//    public void visit(OpOrder opOrder)
//    {}
//
//    @Override
//    public void visit(OpProject opProject)
//    {}
//
//    @Override
//    public void visit(OpReduced opReduced)
//    {}
//
//    @Override
//    public void visit(OpDistinct opDistinct)
//    {}
//
//    @Override
//    public void visit(OpSlice opSlice)
//    {}
//
//    @Override
//    public void visit(OpAssign opAssign)
//    {}
//
//    @Override
//    public void visit(OpGroupAgg opGroupAgg)
//    {}

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