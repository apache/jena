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

import java.util.Collection ;
import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.query.SortCondition;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVars ;
import com.hp.hpl.jena.sparql.algebra.TransformCopy;
import com.hp.hpl.jena.sparql.algebra.op.OpDistinct;
import com.hp.hpl.jena.sparql.algebra.op.OpOrder;
import com.hp.hpl.jena.sparql.algebra.op.OpProject;
import com.hp.hpl.jena.sparql.algebra.op.OpReduced;
import com.hp.hpl.jena.sparql.core.Var;

/**
 * <p>
 * Transforms generic {@code DISTINCT} plus {@code ORDER BY} combinations to
 * {@code REDUCED} plus {@code ORDER BY} which typically gives better
 * performance and memory consumption because engines have to keep less data
 * in-memory to evaluate it.
 * </p>
 * <p>
 * As with most optimizations this is only applied when it is safe to do so. The
 * criteria for being safe to do so are as follows:
 * </p>
 * <ul>
 * <li>Uses both {@code ORDER BY} and {@code DISTINCT} on the same level of the
 * query</li>
 * <li>There is a fixed list of variables to project i.e. not {@code SELECT *}</li>
 * <li>{@code ORDER BY} conditions cover all the projected variables prior to
 * the use of any other variables</li>
 * </ul>
 * <h3>Related Optimizations</h3>
 * <p>
 * See also {@link TransformOrderByDistinctApplication} which is a better
 * optimization for these kinds of queries but only applies to a more limited range
 * of queries. Where possible that optimization is applied in preference to this
 * one.
 * </p>
 * <p>
 * {@link TransformTopN} covers the case of {@code DISTINCT} plus
 * {@code ORDER BY} where there is also a {@code LIMIT}. Where possible that
 * optimization is applied in preference to either this or
 * {@link TransformOrderByDistinctApplication}.
 * </p>
 * 
 */
public class TransformDistinctToReduced extends TransformCopy {

    public TransformDistinctToReduced() {}
    
    // Best is this is after TransformTopN but they are order independent
    // TopN of "reduced or distinct of order" is handled.
    //@Override
    public Op transform1(OpDistinct opDistinct, Op subOp) {
        if (subOp instanceof OpProject) {
            OpProject opProject = (OpProject) subOp;
            if (opProject.getSubOp() instanceof OpOrder) {
                OpOrder opOrder = (OpOrder) opProject.getSubOp();
                Set<Var> projectVars = new HashSet<>(opProject.getVars()) ;
                if (isSafe(projectVars, opOrder)) {
                    return OpReduced.create(subOp);
                }
            }
        }
        return super.transform(opDistinct, subOp);
    }
    
    
    @Override
    public Op transform(OpDistinct opDistinct, Op subOp) {
        
        OpOrder opOrder = null ;
        Set<Var> projectVars = null ;
        /*   SELECT DISTINCT * {} ORDER BY
         * giving an alegbra expression of the form:  
         *   (distinct
         *     (order 
         */
        if (subOp instanceof OpOrder) {
            opOrder = (OpOrder) subOp;
            projectVars = OpVars.visibleVars(subOp) ;
        } else if (subOp instanceof OpProject) {
            OpProject opProject = (OpProject) subOp;
            if (opProject.getSubOp() instanceof OpOrder) {
                projectVars = new HashSet<>(opProject.getVars()) ;
                opOrder = (OpOrder) opProject.getSubOp();
            }
        } 

        if ( projectVars == null )
            return super.transform(opDistinct, subOp) ;
            
        if (isSafe(projectVars, opOrder))
            return OpReduced.create(subOp);
        
        return super.transform(opDistinct, subOp);
    }

    protected boolean isSafe(Set<Var> projectVars, OpOrder opOrder) {
        Set<Var> seenVars = new HashSet<>();

        // For the optimization to be safe all project variables must appear in
        // the ordering prior to any unprojected variables
        // Ordering by expressions is fine provided they use only projected
        // variables
        boolean ok = true;
        for (SortCondition cond : opOrder.getConditions()) {
            if (!isValidSortCondition(cond, projectVars, seenVars)) {
                ok = false;
                break;
            }
            // XXX
            // As soon as we've seen all variables we know this is safe and any
            // further sort conditions are irrelevant
            if (seenVars.size() == projectVars.size())
                return true ;
        }
        // The projects vars must all have been seen.
        return (seenVars.size() == projectVars.size()) ;
    }

    /**
     * Determines whether a sort condition is valid in terms of this optimizer
     * 
     * @param cond
     *            Sort Condition
     * @param projectVars
     *            Project Variables
     * @return True if valid, false otherwise
     */
    private boolean isValidSortCondition(SortCondition cond, Collection<Var> projectVars, Set<Var> seenVars) {
        if (cond.getExpression().isVariable()) {
            if (projectVars.contains(cond.getExpression().asVar())) {
                seenVars.add(cond.getExpression().asVar());
                return true;
            }
            return false;
        } else {
            for (Var v : cond.getExpression().getVarsMentioned()) {
                if (!projectVars.contains(v))
                    return false;
                seenVars.add(v);
            }
            return true;
        }
    }
}
