/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVisitorByType;
import com.hp.hpl.jena.sparql.algebra.op.*;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.ExprList;

public class ReOrganise
{
    Map<Op, Set<Var>> scopeMap ;
    
    // Use OpWalker as a before visitor walker?
    // But sometimes, want the walk to control whether to descend or not. 

    // Before visitor with ability to modify the walk.
    private final class WalkerVisitor extends OpVisitorByType
    {

        @Override
        protected void visit0(Op0 op)
        {}

        @Override
        protected void visit1(Op1 op)
        {}

        @Override
        protected void visit2(Op2 op)
        {}

        @Override
        protected void visitExt(OpExt op)
        {}

        @Override
        protected void visitN(OpN op)
        {}
        
        @Override
        public void visit(OpFilter opFilter)
        {
            if ( OpBGP.isBGP(opFilter.getSubOp()) )
            {
                reorganise((OpBGP)opFilter.getSubOp(), opFilter.getExprs(), scopeMap.get(opFilter)) ;
                return ;
            }
            visit1(opFilter) ;
        }

        @Override
        public void visit(OpBGP opBGP)
        {
            reorganise(opBGP, null, scopeMap.get(opBGP)) ;
        }
        
        
        private void reorganise(OpBGP subOp, ExprList exprs, Set<Var> set)
        {}
        
        
    }
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