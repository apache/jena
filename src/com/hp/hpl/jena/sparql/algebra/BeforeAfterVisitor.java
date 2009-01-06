/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra;

import com.hp.hpl.jena.sparql.algebra.op.*;

public class BeforeAfterVisitor extends OpVisitorByType//implements OpVisitor
{
    OpVisitor beforeVisitor = null ;
    OpVisitor afterVisitor = null ;
    OpVisitor mainVisitor = null ;
    
    public BeforeAfterVisitor(OpVisitor mainVisitor ,
                              OpVisitor beforeVisitor, 
                              OpVisitor afterVisitor) 
    {
        this.mainVisitor = mainVisitor ;
        this.beforeVisitor = beforeVisitor ;
        this.afterVisitor = afterVisitor ;
    }
    
    private void before(Op op)
    { 
        if ( beforeVisitor != null )
            op.visit(beforeVisitor) ;
    }

    private void after(Op op)
    {
        if ( afterVisitor != null )
            op.visit(afterVisitor) ;
    }

    @Override
    protected void visit0(Op0 op)
    { 
        before(op) ; op.visit(mainVisitor) ; after(op) ;
    }

    @Override
    protected void visit1(Op1 op)
    { 
        before(op) ; op.visit(mainVisitor) ; after(op) ;
    }

    @Override
    protected void visit2(Op2 op)
    { 
        before(op) ; op.visit(mainVisitor) ; after(op) ;
    }

    @Override
    protected void visitExt(OpExt op)
    { 
        before(op) ; op.visit(mainVisitor) ; after(op) ;
    }

    @Override
    protected void visitN(OpN op)
    { 
        before(op) ; op.visit(mainVisitor) ; after(op) ;
    }

//    public void visit(OpBGP opBGP)
//    { 
//        before(opBGP) ; mainVisitor.visit(opBGP) ; after(opBGP) ;
//    }
//    
//    public void visit(OpQuadPattern quadPattern)
//    {
//		before(quadPattern) ; mainVisitor.visit(quadPattern) ; after(quadPattern) ;
//	}
//    
//    public void visit(OpTriple opTriple)
//    {
//		before(opTriple) ; mainVisitor.visit(opTriple) ; after(opTriple) ;
//	}
//    
//    public void visit(OpPath opPath)
//    {
//		before(opPath) ; mainVisitor.visit(opPath) ; after(opPath) ;
//	}
//    
//    public void visit(OpTable opTable)
//    {
//		before(opTable) ; mainVisitor.visit(opTable) ; after(opTable) ;
//	}
//    public void visit(OpNull opNull)
//    {
//		before(opNull) ; mainVisitor.visit(opNull) ; after(opNull) ;
//	}
//    
//    public void visit(OpProcedure opProc)
//    {
//		before(opProc) ; mainVisitor.visit(opProc) ; after(opProc) ;
//	}
//    public void visit(OpPropFunc opPropFunc)
//    {
//		before(opPropFunc) ; mainVisitor.visit(opPropFunc) ; after(opPropFunc) ;
//	}
//    
//    public void visit(OpFilter opFilter)
//    {
//		before(opFilter) ; mainVisitor.visit(opFilter) ; after(opFilter) ;
//	}
//    public void visit(OpGraph opGraph)
//    {
//		before(opGraph) ; mainVisitor.visit(opGraph) ; after(opGraph) ;
//	}
//    
//    public void visit(OpService opService)
//    {
//		before(opService) ; mainVisitor.visit(opService) ; after(opService) ;
//	}
//    public void visit(OpDatasetNames dsNames)
//    {
//		before(dsNames) ; mainVisitor.visit(dsNames) ; after(dsNames) ;
//	}
//    
//    public void visit(OpLabel opLabel)
//    {
//		before(opLabel) ; mainVisitor.visit(opLabel) ; after(opLabel) ;
//	}
//    public void visit(OpJoin opJoin)
//    {
//		before(opJoin) ; mainVisitor.visit(opJoin) ; after(opJoin) ;
//	}
//    
//    public void visit(OpSequence opSequence)
//    {
//		before(opSequence) ; mainVisitor.visit(opSequence) ; after(opSequence) ;
//	}
//    
//    public void visit(OpLeftJoin opLeftJoin)
//    {
//		before(opLeftJoin) ; mainVisitor.visit(opLeftJoin) ; after(opLeftJoin) ;
//	}
//    public void visit(OpDiff opDiff)
//    {
//		before(opDiff) ; mainVisitor.visit(opDiff) ; after(opDiff) ;
//	}
//    
//    public void visit(OpUnion opUnion)
//    {
//		before(opUnion) ; mainVisitor.visit(opUnion) ; after(opUnion) ;
//	}
//    
//    public void visit(OpConditional opCondition)
//    {
//		before(opCondition) ; mainVisitor.visit(opCondition) ; after(opCondition) ;
//	}
//    public void visit(OpExt opExt)
//    {
//		before(opExt) ; mainVisitor.visit(opExt) ; after(opExt) ;
//	}
//    
//    public void visit(OpList opList)
//    {
//		before(opList) ; mainVisitor.visit(opList) ; after(opList) ;
//	}
//    public void visit(OpOrder opOrder)
//    {
//		before(opOrder) ; mainVisitor.visit(opOrder) ; after(opOrder) ;
//	}
//    
//    public void visit(OpProject opProject)
//    {
//		before(opProject) ; mainVisitor.visit(opProject) ; after(opProject) ;
//	}
//    
//    public void visit(OpReduced opReduced)
//    {
//		before(opReduced) ; mainVisitor.visit(opReduced) ; after(opReduced) ;
//	}
//    
//    public void visit(OpDistinct opDistinct)
//    {
//		before(opDistinct) ; mainVisitor.visit(opDistinct) ; after(opDistinct) ;
//	}
//    public void visit(OpSlice opSlice)
//    {
//		before(opSlice) ; mainVisitor.visit(opSlice) ; after(opSlice) ;
//	}
//    
//    public void visit(OpAssign opAssign)
//    {
//		before(opAssign) ; mainVisitor.visit(opAssign) ; after(opAssign) ;
//	}
//    
//    public void visit(OpGroupAgg opGroupAgg)
//    {
//		before(opGroupAgg) ; mainVisitor.visit(opGroupAgg) ; after(opGroupAgg) ;
//	}
}
/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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