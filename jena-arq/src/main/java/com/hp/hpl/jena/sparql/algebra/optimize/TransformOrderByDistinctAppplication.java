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

import com.hp.hpl.jena.query.SortCondition;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.TransformCopy;
import com.hp.hpl.jena.sparql.algebra.op.OpDistinct;
import com.hp.hpl.jena.sparql.algebra.op.OpOrder;
import com.hp.hpl.jena.sparql.algebra.op.OpProject;

/**
 * Prototype transformer motivated by JENA-441
 * <p>
 * This is a limited optimization that applies in the case where you have a query that meets the following conditions: 
 * </p>
 * <ul>
 * <li>Uses both ORDER BY and DISTINCT on the same level of the query</li>
 * <li>The list of order conditions is only simple variables</li>
 * <li>There is a fixed list of simple variables to project that correspond precisely to the order conditions
 * </ul>
 * <p>
 * Essentially this takes algebras of the following form:
 * </p>
 * <code>
 * (distinct 
 *   (project (?var) 
 *     (order (?var) 
 *       ... )))
 * </code>
 * <p>
 * And produces algebra of the following form:
 * </p>
 * <code>
 * (order (?var)
 *   (distinct 
 *     (project (?var) 
 *       ... )))
 * </code>
 */
public class TransformOrderByDistinctAppplication extends TransformCopy {

    @Override
    public Op transform(OpDistinct opDistinct, Op subOp) {
        if (subOp instanceof OpProject)
        {
            OpProject project = (OpProject)subOp;
            //At the project stage everything is a simple variable
            //Inner operation must be an ORDER BY
            if (project.getSubOp() instanceof OpOrder)
            {
                OpOrder order = (OpOrder)project.getSubOp();
                
                //Everything we wish to order by must a simple variable and correspond exactly to the project list
                //TODO: I think this can be generalized to allow any order conditions that use variables mentioned in the project list
                boolean ok = true;
                for (SortCondition condition : order.getConditions()) {
                    if (!condition.getExpression().isVariable()) ok = false;
                }
                
                //Everything checks out so we can make the change
                if (ok) {
                    OpProject newProject = new OpProject(order.getSubOp(), project.getVars());
                    OpDistinct newDistinct = new OpDistinct(newProject);
                    return new OpOrder(newDistinct, order.getConditions());
                }
            }
        }
        
        //If we reach here then this transform is not applicable
        return super.transform(opDistinct, subOp);
    }

}
