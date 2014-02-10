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

import java.util.List;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.SortCondition;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.TransformCopy;
import com.hp.hpl.jena.sparql.algebra.op.OpDistinct;
import com.hp.hpl.jena.sparql.algebra.op.OpOrder;
import com.hp.hpl.jena.sparql.algebra.op.OpProject;
import com.hp.hpl.jena.sparql.algebra.op.OpReduced;
import com.hp.hpl.jena.sparql.core.Var;

/**
 * <p>
 * Improved optimization for {@code ORDER BY} plus {@code DISTINCT} or
 * {@code REDUCED} combinations, see JENA-441 for original proposal and
 * discussion.
 * </p>
 * <p>
 * This optimization is enabled by default as with most ARQ optimizations and
 * may be disabled by setting the symbol
 * {@link ARQ#optOrderByDistinctApplication} to false.
 * </p>
 * <h3>Optimization Applicability</h3>
 * <p>
 * This is a limited optimization that applies in the case where you have a
 * query that meets the following conditions:
 * </p>
 * <ul>
 * <li>Uses both {@code ORDER BY} and {@code DISTINCT} or {@code REDUCED} on the
 * same level of the query</li>
 * <li>There is a fixed list of variables to project i.e. not {@code SELECT *}</li>
 * <li>{@code ORDER BY} conditions only use variables present in the project
 * list</li>
 * </ul>
 * <p>
 * Essentially this takes algebras of the following form:
 * </p>
 * 
 * <pre>
 * (distinct 
 *   (project (?var) 
 *     (order (?var) 
 *       ... )))
 * </pre>
 * <p>
 * And produces algebra of the following form:
 * </p>
 * 
 * <pre>
 * (order (?var)
 *   (distinct 
 *     (project (?var) 
 *       ... )))
 * </pre>
 * <p>
 * In the general case this in unsafe because it would change the semantics of
 * the query since {@code ORDER BY} can access variables that are not projected.
 * However if the conditions outlined are met then this optimization is safe,
 * the algebras will be semantically equivalent and the resulting form likely
 * significantly more performant, of course YMMV depending on how much data you
 * are querying.
 * </p>
 */
public class TransformOrderByDistinctApplication extends TransformCopy {

    @Override
    public Op transform(OpDistinct opDistinct, Op subOp) {
        if (subOp instanceof OpProject) {
            OpProject project = (OpProject) subOp;
            // At the project stage everything is a simple variable
            // Inner operation must be an ORDER BY
            if (project.getSubOp() instanceof OpOrder) {
                List<Var> projectVars = project.getVars();
                OpOrder order = (OpOrder) project.getSubOp();

                // Everything we wish to order by must only use variables that
                // appear in the project list
                boolean ok = true;
                for (SortCondition condition : order.getConditions()) {
                    if (!isValidSortCondition(condition, projectVars)) {
                        ok = false;
                        break;
                    }
                }

                // Everything checks out so we can make the change
                if (ok) {
                    OpProject newProject = new OpProject(order.getSubOp(), project.getVars());
                    OpDistinct newDistinct = new OpDistinct(newProject);
                    return new OpOrder(newDistinct, order.getConditions());
                }
            }
        }

        // If we reach here then this transform is not applicable
        return super.transform(opDistinct, subOp);
    }

    @Override
    public Op transform(OpReduced opReduced, Op subOp) {
        if (subOp instanceof OpProject) {
            OpProject project = (OpProject) subOp;
            // At the project stage everything is a simple variable
            // Inner operation must be an ORDER BY
            if (project.getSubOp() instanceof OpOrder) {
                List<Var> projectVars = project.getVars();
                OpOrder order = (OpOrder) project.getSubOp();

                // Everything we wish to order by must only use variables that
                // appear in the project list
                boolean ok = true;
                for (SortCondition condition : order.getConditions()) {
                    if (!isValidSortCondition(condition, projectVars)) {
                        ok = false;
                        break;
                    }
                }

                // Everything checks out so we can make the change
                if (ok) {
                    OpProject newProject = new OpProject(order.getSubOp(), project.getVars());
                    Op newReduced = OpReduced.create(newProject);
                    return new OpOrder(newReduced, order.getConditions());
                }
            }
        }

        // If we reach here then this transform is not applicable
        return super.transform(opReduced, subOp);
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
    private boolean isValidSortCondition(SortCondition cond, List<Var> projectVars) {
        if (cond.getExpression().isVariable()) {
            return projectVars.contains(cond.getExpression().asVar());
        } else {
            for (Var v : cond.getExpression().getVarsMentioned()) {
                if (!projectVars.contains(v))
                    return false;
            }
            return true;
        }
    }
}
