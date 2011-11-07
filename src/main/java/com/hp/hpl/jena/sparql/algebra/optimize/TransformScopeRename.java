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

package com.hp.hpl.jena.sparql.algebra.optimize;

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.OpVisitorByTypeBase ;
import com.hp.hpl.jena.sparql.algebra.TransformCopy ;
import com.hp.hpl.jena.sparql.algebra.Transformer ;
import com.hp.hpl.jena.sparql.algebra.op.OpModifier ;
import com.hp.hpl.jena.sparql.algebra.op.OpProject ;
import com.hp.hpl.jena.sparql.engine.Rename ;

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
                    subOp = Rename.renameVars(subOp, opProject.getVars()) ;
                return super.transform(opProject, subOp) ;
            }
        }
    }
}
