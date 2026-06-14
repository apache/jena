/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.tdb2.solver.skipscan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.atlas.lib.SetUtils;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.algebra.OpVisitorBase;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.op.Op1;
import org.apache.jena.sparql.algebra.op.OpDistinct;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprLib;
import org.apache.jena.sparql.expr.ExprList;

public class TransformDistinctPlacement
    extends TransformCopy
{
    @Override
    public Op transform(OpDistinct opDistinct, Op subOp) {
        Set<Var> projectVars = new LinkedHashSet<>();
        OpVars.visibleVars(subOp, projectVars);
        Op result = OpVisitorDistinctPlacement.transform(projectVars, subOp);
        return result;
    }

    /**
     * The visitor does the actual top-down rewrite.
     * For each encountered Op, the visitor creates a new instance of itself
     * that holds the transformation result.
     */
    private static class OpVisitorDistinctPlacement
        extends OpVisitorBase
    {
        private Set<Var> projectVars;
        private Op result;

        public OpVisitorDistinctPlacement(Set<Var> projectVars) {
            super();
            this.projectVars = Objects.requireNonNull(projectVars);
        }

        public Op getResult() {
            return result;
        }

        public static Op transform(Set<Var> projectVars, Op op) {
            OpVisitorDistinctPlacement visitor = new OpVisitorDistinctPlacement(projectVars);
            op.visit(visitor);
            Op r = visitor.getResult();
            Op result = r == null ? placeDistinctProject(projectVars, op) : r;
            return result;
        }

        /** Inject (distinct (project (projectVars') op)) */
        private static Op placeDistinctProject(Set<Var> projectVars, Op op) {
            Set<Var> visibleVars = OpVars.visibleVars(op);

            Op projectionAdaptedOp = op;

            // If the visible variables differ from in-scope projectVars then place the projection.
            Set<Var> inScopeProjectVars = SetUtils.intersection(projectVars, visibleVars);
            if (!inScopeProjectVars.containsAll(visibleVars)) {
                List<Var> projectVarList = new ArrayList<>(inScopeProjectVars);
                projectionAdaptedOp = new OpProject(projectionAdaptedOp, projectVarList);
            }

            return OpDistinct.create(projectionAdaptedOp);
        }

        /** Process (distinct (projectVars) (project (vars) (.))).
         *
         *  (distinct (?x ?y ?z) .) can be pushed over a (project (?y ?a) .)
         *  (distinct (?y) .)
         *  - so we can push over any project by creating the intersection of the involved variables. */
        @Override
        public void visit(OpProject opProject) {
            Set<Var> vars = new LinkedHashSet<>(opProject.getVars()); // Retain order
            vars.retainAll(projectVars);

            // Create the intersection of the current distinct-vars and the project-vars.
            // In the corner case where this set is empty, just don't push.
            boolean canPush = !vars.isEmpty();
            if (canPush) {
                Op subOp = opProject.getSubOp();
                // Proceed with the variables of the projection
                // (possibly a sub-set of the original distinct-vars).
                result = transform(vars, subOp);
            }
        }

        @Override
        public void visit(OpExtend opExtend) {
            tryPush(opExtend, (opE, subOpVars) -> canPush(projectVars, opE));
            // Post process the result: After pushing (distinct (vars) .) over (extend .),
            // the variables visible from extend must remain the same.
            if (result != null) {
                List<Var> extendVars = opExtend.getVarExprList().getVars();
                boolean reapplyProject = !projectVars.containsAll(extendVars);
                if (reapplyProject) {
                    result = new OpProject(result, new ArrayList<>(extendVars));
                }
            }
        }

        @Override
        public void visit(OpFilter opFilter) {
            tryPush(opFilter, (opF, subOpVars) -> canPush(projectVars, opF));
        }

        /** Merge nested distinct. */
        @Override
        public void visit(OpDistinct opDistinct) {
            Op subOp = opDistinct.getSubOp();
            result = transform(projectVars, subOp);
        }

        private <T extends Op1> void tryPush(T op1, BiPredicate<T, Set<Var>> canPush) {
            if (canPush.test(op1, projectVars)) {
                Op subOp = op1.getSubOp();
                // We could already prune projectVars to the visible vars of subOp here.
                // But we postpone this to the point where we place the distinct operator
                // using placeDistinctProject.
                Op rewrittenSubOp = transform(projectVars, subOp);
                result = op1.copy(rewrittenSubOp);
            } else {
                result = placeDistinctProject(projectVars, op1);
            }
        }

        private static boolean canPush(Set<Var> projectVars, OpExtend opExtend) {
            VarExprList extend = opExtend.getVarExprList();
            boolean isStable = extend.getExprs().values().stream().allMatch(ExprLib::isStable);
            if (!isStable) {
                return false;
            }

            Op subOp = opExtend.getSubOp();
            Set<Var> subOpVars = OpVars.visibleVars(subOp);
            Set<Var> mentionedVars = varsMentioned(extend);

            if (!isSameCoverage(mentionedVars, subOpVars, projectVars)) {
                return false;
            }

            return true;
        }

        private static boolean canPush(Set<Var> projectVars, OpFilter opFilter) {
            ExprList filter = opFilter.getExprs();

            // Don't push distinct over unstable expressions, such as rand() or uuid().
            boolean isStable = filter.getList().stream().allMatch(ExprLib::isStable);
            if (!isStable) {
                return false;
            }
            // Example: Can we push DISTINCT ?s ?p over (filter (?p = ?x) (bgp (:s ?p :o))) ?

            // filter(?p = ?x) mentions {?p, ?x}
            Set<Var> filterVars = filter.getVarsMentioned();

            // Assume subOp := (bgp (:s ?p :o)) which mentions {?p}
            Op subOp = opFilter.getSubOp();
            Set<Var> subOpVars = OpVars.visibleVars(subOp);

            // Of the filter's variables {?p, ?x} only {?p} is in scope in the subOp
            // and "grounds" the filter's {?p}.
            Set<Var> inScopeFilterVars = SetUtils.intersection(filterVars, subOpVars);

            // If the distinct-transforms's projectVars contains all the filter's grounded vars then we can push.
            if (projectVars.containsAll(inScopeFilterVars)) {
                // Our DISTINCT ?s ?p covers all grounded variables of the filter expression, namely {p}
                // So we can push.
                return true;
            }
            return false;
        }

        private static Set<Var> varsMentioned(VarExprList vel) {
            Set<Var> mentionedVars = varsMentioned(vel.getExprs().values().stream());
            return mentionedVars;
        }

        private static Set<Var> varsMentioned(Stream<Expr> exprs) {
            Set<Var> mentionedVars = exprs
                    .map(Expr::getVarsMentioned)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());
            return mentionedVars;
        }

        /**
         * True iff the exact same set of mentioned variables is covered by both
         * the original projectVars and the sub op's visible variables.
         */
        private static boolean isSameCoverage(Set<Var> mentionedVars, Set<Var> subOpVars, Set<Var> projectVars) {
            Set<Var> coveredVarsBefore = SetUtils.intersection(mentionedVars, subOpVars);
            Set<Var> coveredVarsAfter = SetUtils.intersection(mentionedVars, projectVars);
            boolean result = coveredVarsBefore.equals(coveredVarsAfter);
            return result;
        }
    }
}

