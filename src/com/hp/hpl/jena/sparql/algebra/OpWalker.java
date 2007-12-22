/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra;

import com.hp.hpl.jena.sparql.algebra.op.*;


public class OpWalker
{
    public static void walk(Op op, OpVisitor visitor)
    {
        op.visit(new Walker(visitor)) ;
    }
    
    private static class Walker implements OpVisitor
    {
        private OpVisitor visitor ;

        public Walker(OpVisitor visitor) { this.visitor = visitor ; }
        
        protected void visit2(Op2 op)
        {
            op.getLeft().visit(this) ;
            op.getRight().visit(this) ;
            op.visit(visitor) ;        
        }
        
        protected void visit1(Op1 op)
        {
            op.getSubOp().visit(this) ;
            op.visit(visitor) ;        
        }
        
        protected void visit0(Op0 op)         
        {  
            op.visit(visitor) ; 
        }
        
        public void visit(OpBGP opBGP)
        { visit0(opBGP) ; }
        
        public void visit(OpQuadPattern quadPattern)
        { visit0(quadPattern) ; }

        public void visit(OpProcedure opProcedure)
        { visit1(opProcedure) ; }

        public void visit(OpJoin opJoin)
        { visit2(opJoin) ; }

        public void visit(OpStage opStage)
        { visit2(opStage) ; }
        
        public void visit(OpLeftJoin opLeftJoin)
        { visit2(opLeftJoin) ; }

        public void visit(OpDiff opDiff)
        { visit2(opDiff) ; }

        public void visit(OpUnion opUnion)
        { visit2(opUnion) ; }

        public void visit(OpFilter opFilter)
        { visit1(opFilter) ; }

        public void visit(OpGraph opGraph)
        { visit1(opGraph) ; }

        public void visit(OpService opService)
        { visit1(opService) ; }

        public void visit(OpDatasetNames dsNames)
        { visit0(dsNames) ; }

        public void visit(OpTable opUnit)
        { visit0(opUnit) ; }

        public void visit(OpExt opExt)
        { opExt.visit(visitor) ; }

        public void visit(OpNull opNull)
        { visit0(opNull) ; }

        public void visit(OpAssign opAssign)
        { visit1(opAssign) ; }

        public void visit(OpList opList)
        { visit1(opList) ; }

        public void visit(OpOrder opOrder)
        { visit1(opOrder) ; }

        public void visit(OpProject opProject)
        { visit1(opProject) ; }

        public void visit(OpReduced opReduced)
        { visit1(opReduced) ; }

        public void visit(OpDistinct opDistinct)
        { visit1(opDistinct) ; }

        public void visit(OpSlice opSlice)
        { visit1(opSlice) ; }

        public void visit(OpGroupAgg opGroupAgg)
        { visit1(opGroupAgg) ; }
    }
}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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