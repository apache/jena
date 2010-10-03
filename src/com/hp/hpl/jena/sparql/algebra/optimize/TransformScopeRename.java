/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra.optimize;

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.OpVisitorByTypeBase ;
import com.hp.hpl.jena.sparql.algebra.TransformCopy ;
import com.hp.hpl.jena.sparql.algebra.Transformer ;
import com.hp.hpl.jena.sparql.algebra.op.OpModifier ;
import com.hp.hpl.jena.sparql.algebra.op.OpProject ;
import com.hp.hpl.jena.sparql.engine.VarRename ;

/** Rename variables so that names can be treated globally.
 *  (project) and (group) can hide variables, but we only
 *  need to account for OpProject because group is never
 *  executed linearly. 
 */

public class TransformScopeRename
{
    // Is there an OpProject before any pattern algebra operators?
    // We don't need to rename through this one.

    // Track OpProject

    public static Op transform(Op op)
    {
        return new TransformScopeRename$(op).work() ;
    }

    private static class TransformScopeRename$
    {
        private boolean outerMostOpProject = false ;
        private int projectRenameDepth = 0 ;
        private int projectCount = 0 ;
        
        private Op op ;

        public TransformScopeRename$(Op op)
        {
            this.op = op ;
            {
                Op op2 = op ;
                while( op2 instanceof OpModifier )
                {
                    // If already true ...
                    if ( op2 instanceof OpProject )
                    {
                        outerMostOpProject = true ;
                        break ;
                    }
                    op2 = ((OpModifier)op2).getSubOp() ;
                }
            }
            // Set the project counter: renaming begins when this hits one.
            // Set 2 to there is a project in the top-most OpModifers.
            // This does not cause a rename so start renaming at depth .
            // otherwise rename from depth 1.
            if ( outerMostOpProject )
                projectRenameDepth = 2 ;
            else
                projectRenameDepth = 1;
        }
        
        public Op work()
        {
            return Transformer.transform(new RenameByScope(), op, new BeforeWalk(), new AfterWalk()) ;   
        }


        private class BeforeWalk extends OpVisitorByTypeBase
        {
            @Override
            public void visit(OpProject opProject)
            {
                projectCount++ ;
            }
        }

        private class AfterWalk extends OpVisitorByTypeBase
        {
            @Override
            public void visit(OpProject opProject)
            {
                --projectCount ;
            }
        }

        private class RenameByScope extends TransformCopy
        {
            @Override
            public Op transform(OpProject opProject, Op subOp)
            { 
                // Need to find the right project
                // We already stripped outer modifier. 
                if ( projectCount >= projectRenameDepth )
                    // Inner ones already done.
                    subOp = VarRename.rename(subOp, opProject.getVars()) ;
                return super.transform(opProject, subOp) ;
            }
        }
    }
}

/*
 * (c) Copyright 2010 Epimorphics Ltd.
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