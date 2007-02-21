/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.algebra;

import com.hp.hpl.jena.query.algebra.op.*;


public class OpWalker
{
    public static void walk(Op op, OpVisitor visitor)
    {
        op.visit(new Walker(visitor)) ;
    }
    
    private static class Walker implements OpVisitor
    {
        private OpVisitor visitor ;

        Walker(OpVisitor visitor) { this.visitor = visitor ; }
        
        private void visitX(Op2 op)
        {
            op.getLeft().visit(this) ;
            op.getRight().visit(this) ;
            op.visit(visitor) ;        
        }
        
        private void visitX(Op1 op)
        {
            op.getSubOp().visit(this) ;
            op.visit(visitor) ;        
        }
        
        private void visitX(Op0 op)         
        {  
            op.visit(visitor) ; 
        }
        
        public void visit(OpBGP opBGP)
        { visitX(opBGP) ; }

        public void visit(OpJoin opJoin)
        { visitX(opJoin) ; }
        

        public void visit(OpLeftJoin opLeftJoin)
        { visitX(opLeftJoin) ; }

        public void visit(OpUnion opUnion)
        { visitX(opUnion) ; }

        public void visit(OpFilter opFilter)
        { visitX(opFilter) ; }

        public void visit(OpGraph opGraph)
        { visitX(opGraph) ; }

        public void visit(OpQuadPattern quadPattern)
        { visitX(quadPattern) ; }

        public void visit(OpDatasetNames dsNames)
        { visitX(dsNames) ; }

        public void visit(OpUnit opUnit)
        { opUnit.visit(visitor) ; }

        public void visit(OpExt opExt)
        { opExt.visit(visitor) ; }

        public void visit(OpOrder opOrder)
        { visitX(opOrder) ; }

        public void visit(OpProject opProject)
        { visitX(opProject) ; }

        public void visit(OpDistinct opDistinct)
        { visitX(opDistinct) ; }

        public void visit(OpSlice opSlice)
        { visitX(opSlice) ; }
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