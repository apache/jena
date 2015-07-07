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

package org.apache.jena.sparql.algebra.optimize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.jena.atlas.lib.CollectionUtils;

import org.apache.jena.query.SortCondition;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitor;
import org.apache.jena.sparql.algebra.OpVisitorBase;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpExt;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpGroup;
import org.apache.jena.sparql.algebra.op.OpOrder;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpTopN;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.ExprLib;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprTransform;
import org.apache.jena.sparql.expr.ExprTransformSubstitute;
import org.apache.jena.sparql.expr.ExprTransformer;
import org.apache.jena.sparql.expr.ExprVars;
import org.apache.jena.sparql.expr.NodeValue;

/**
 * A transform that tries to in-line/eliminate assignments
 * <p>
 * There are two classes of assignments that we can try and in-line/eliminate:
 * </p>
 * <ol>
 * <li>Assignments where the assigned variable is used only once in a subsequent
 * assignment can be in-lined</li>
 * <li>Assignments where the assigned value is never used elsewhere can be
 * eliminated</li>
 * </ol>
 * <p>
 * Both of these changes can only happen inside of projections as otherwise we
 * have to assume that the user may need the resulting variable and thus we
 * leave the assignment alone. Assignments to be in-lined must also be
 * deterministic i.e. moving their placement in the query and thus the possible
 * solutions they might operate must not change their outputs. Whether an
 * expression is deterministic is defined by {@link ExprLib#isStable(Expr)}.
 * </p>
 * <p>
 * Assignments may be in-lined in the following places:
 * </p>
 * <ul>
 * <li>Filter Expressions</li>
 * <li>Bind and Select Expressions</li>
 * <li>Order By Expressions if aggressive in-lining is enabled or the assigned
 * expression is a constant</li>
 * </ul>
 * <p>
 * In the case of order by we only in-line assignments when aggressive mode is
 * set as the realities of order by are that expressions may be recomputed
 * multiple times and so in-lining may actually hurt performance in those cases
 * unless the expression to be in-lined is itself a constant.
 * </p>
 */
public class TransformEliminateAssignments extends TransformCopy {

    public static Op eliminate(Op op) {
        return eliminate(op, false);
    }

    public static Op eliminate(Op op, boolean aggressive) {
        AssignmentTracker tracker = new AssignmentTracker();
        AssignmentPusher pusher = new AssignmentPusher(tracker);
        AssignmentPopper popper = new AssignmentPopper(tracker);
        Transform transform = new TransformEliminateAssignments(tracker, pusher, popper, aggressive);

        return Transformer.transformSkipService(transform, op, pusher, popper);
    }

    private final OpVisitor before, after;
    private final AssignmentTracker tracker;
    private final boolean aggressive;

    private TransformEliminateAssignments(AssignmentTracker tracker, OpVisitor before, OpVisitor after) {
        this(tracker, before, after, false);
    }

    private TransformEliminateAssignments(AssignmentTracker tracker, OpVisitor before, OpVisitor after,
            boolean aggressive) {
        this.tracker = tracker;
        this.before = before;
        this.after = after;
        this.aggressive = aggressive;
    }

    protected boolean canInline(Expr e) {
        if (e == null)
            return false;
        return ExprLib.isStable(e);
    }

    protected boolean shouldInline(Expr e) {
        if (e == null)
            return false;

        // Inline everything when being aggressive
        if (this.aggressive)
            return true;

        // If not being aggressive only inline if the expression is a constant
        return e.isConstant() || e instanceof NodeValue;
    }

    protected boolean isApplicable() {
        // Can only be applied if we are inside a projection as otherwise the
        // assigned variables need to remain visible
        if (!this.tracker.insideProjection())
            return false;
        // If there are no eligible assignments then don't bother doing any work
        if (this.tracker.assignments.size() == 0)
            return false;

        // Otherwise may be applicable
        return true;
    }

    @Override
    public Op transform(OpExt opExt) {
        return opExt.apply(this, this.before, this.after);
    }

    @Override
    public Op transform(OpFilter opFilter, Op subOp) {
        if (!this.isApplicable())
            return super.transform(opFilter, subOp);

        // See what vars are used in the filter
        Collection<Var> vars = new ArrayList<>();
        for (Expr expr : opFilter.getExprs().getList()) {
            ExprVars.varsMentioned(vars, expr);
        }

        // Are any of these vars single usage?
        ExprList exprs = opFilter.getExprs();
        boolean modified = false;
        for (Var var : vars) {
            // Usage count will be 2 if we can eliminate the assignment
            // First usage is when it is introduced by the assignment and the
            // second is when it is used now in this filter
            Expr e = getAssignExpr(var);
            if (this.tracker.getUsageCount(var) == 2 && hasAssignment(var) && canInline(e)) {
                // Can go back and eliminate that assignment
                subOp = eliminateAssignment(subOp, var);
                // Replace the variable usage with the expression
                exprs = ExprTransformer.transform(new ExprTransformSubstitute(var, e), exprs);
                this.tracker.getAssignments().remove(var);
                modified = true;
            }
        }

        // Create a new filter if we've substituted any expressions
        if (modified) {
            return OpFilter.filter(exprs, subOp);
        }

        return super.transform(opFilter, subOp);
    }

    private boolean hasAssignment(Var var) {
        return this.tracker.getAssignments().containsKey(var);
    }

    private Expr getAssignExpr(Var var) {
        return this.tracker.getAssignments().get(var);
    }

    @Override
    public Op transform(OpExtend opExtend, Op subOp) {
        // No point tracking assignments if not in a projection as we can't
        // possibly eliminate them without a projection to hide the fact that
        // the assigned value is unnecessary or only used once
        if (!this.tracker.insideProjection())
            return super.transform(opExtend, subOp);

        // Track the assignments for future reference
        this.tracker.putAssignments(opExtend.getVarExprList());

        // Eliminate and inline assignments
        VarExprList unusedAssignments = processUnused(opExtend.getVarExprList());
        VarExprList newAssignments = new VarExprList();
        for (Var assignVar : opExtend.getVarExprList().getVars()) {
            // If unused eliminate
            if (unusedAssignments != null && unusedAssignments.contains(assignVar))
                continue;

            Expr currExpr = opExtend.getVarExprList().getExpr(assignVar);

            // See what vars are used in the current expression
            Collection<Var> vars = new ArrayList<>();
            ExprVars.varsMentioned(vars, currExpr);

            // See if we can inline anything
            for (Var var : vars) {
                // Usage count will be 2 if we can eliminate the assignment
                // First usage is when it is introduced by the assignment and
                // the second is when it is used now used in another assignment
                Expr e = getAssignExpr(var);
                if (this.tracker.getUsageCount(var) == 2 && hasAssignment(var) && canInline(e)) {
                    // Can go back and eliminate that assignment
                    subOp = eliminateAssignment(subOp, var);
                    // Replace the variable usage with the expression within
                    // expression
                    currExpr = ExprTransformer.transform(new ExprTransformSubstitute(var, e), currExpr);
                    this.tracker.getAssignments().remove(var);

                    // Need to update any assignments we may be tracking that
                    // refer to the variable we just inlined
                    this.tracker.updateAssignments(var, e);

                    // If the assignment to be eliminated was introduced by the
                    // extend we are processing need to remove it from the
                    // VarExprList we are currently building
                    if (newAssignments.contains(var) && newAssignments.getExpr(var).equals(e)) {
                        newAssignments.getVars().remove(var);
                        newAssignments.getExprs().remove(var);
                    }
                }
            }
            newAssignments.add(assignVar, currExpr);
        }

        // May be able to eliminate the extend entirely in some cases
        if (newAssignments.size() > 0) {
            return OpExtend.create(subOp, newAssignments);
        } else {
            return subOp;
        }
    }

    private VarExprList processUnused(VarExprList assignments) {
        if (CollectionUtils.disjoint(assignments.getVars(), this.tracker.getAssignments().keySet()))
            return null;

        VarExprList singleUse = new VarExprList();
        for (Var var : assignments.getVars()) {
            if (this.tracker.getUsageCount(var) == 1)
                singleUse.add(var, assignments.getExpr(var));
        }

        // If nothing is single use
        if (singleUse.size() == 0)
            return null;

        return singleUse;
    }

    @Override
    public Op transform(OpOrder opOrder, Op subOp) {
        if (!this.isApplicable())
            return super.transform(opOrder, subOp);

        // See what vars are used in the sort conditions
        Collection<Var> vars = new ArrayList<>();
        for (SortCondition cond : opOrder.getConditions()) {
            ExprVars.varsMentioned(vars, cond.getExpression());
        }

        // Are any of these vars single usage?
        List<SortCondition> conditions = null;
        for (Var var : vars) {
            // Usage count will be 2 if we can eliminate the assignment
            // First usage is when it is introduced by the assignment and the
            // second is when it is used now in this order expression
            Expr e = getAssignExpr(var);
            if (this.tracker.getUsageCount(var) == 2 && hasAssignment(var) && canInline(e) && shouldInline(e)) {
                // Can go back and eliminate that assignment
                subOp = eliminateAssignment(subOp, var);
                // Replace the variable usage with the expression within the
                // sort conditions
                conditions = processConditions(opOrder.getConditions(), conditions, var);
                this.tracker.getAssignments().remove(var);
            }
        }

        // Create a new order if we've substituted any expressions
        if (conditions != null) {
            return new OpOrder(subOp, conditions);
        }

        return super.transform(opOrder, subOp);
    }

    private List<SortCondition> processConditions(List<SortCondition> baseConditions,
            List<SortCondition> processedConditions, Var var) {
        List<SortCondition> inputConditions = processedConditions != null ? processedConditions : baseConditions;
        List<SortCondition> outputConditions = new ArrayList<>();

        for (SortCondition cond : inputConditions) {
            Expr e = cond.getExpression();
            e = ExprTransformer.transform(new ExprTransformSubstitute(var, getAssignExpr(var)), e);
            outputConditions.add(new SortCondition(e, cond.getDirection()));
        }

        return outputConditions;
    }

    @Override
    public Op transform(OpTopN opTop, Op subOp) {
        if (!this.isApplicable())
            return super.transform(opTop, subOp);

        // See what vars are used in the sort conditions
        Collection<Var> vars = new ArrayList<>();
        for (SortCondition cond : opTop.getConditions()) {
            ExprVars.varsMentioned(vars, cond.getExpression());
        }

        // Are any of these vars single usage?
        List<SortCondition> conditions = null;
        for (Var var : vars) {
            // Usage count will be 2 if we can eliminate the assignment
            // First usage is when it is introduced by the assignment and the
            // second is when it is used now in this filter
            Expr e = getAssignExpr(var);
            if (this.tracker.getUsageCount(var) == 2 && hasAssignment(var) && canInline(e) && shouldInline(e)) {
                // Can go back and eliminate that assignment
                subOp = eliminateAssignment(subOp, var);
                // Replace the variable usage with the expression within the
                // sort conditions
                conditions = processConditions(opTop.getConditions(), conditions, var);
                this.tracker.getAssignments().remove(var);
            }
        }

        // Create a new order if we've substituted any expressions
        if (conditions != null) {
            return new OpTopN(subOp, opTop.getLimit(), conditions);
        }

        return super.transform(opTop, subOp);
    }

    @Override
    public Op transform(OpGroup opGroup, Op subOp) {
        return super.transform(opGroup, subOp);

        // TODO Unclear if this will work properly or not because group can
        // introduce new assignments as well as evaluate expressions

        //@formatter:off
//        if (!this.isApplicable())
//            return super.transform(opGroup, subOp);
//
//        // See what vars are used in the filter
//        Collection<Var> vars = new ArrayList<>();
//        VarExprList exprs = new VarExprList(opGroup.getGroupVars());
//        List<ExprAggregator> aggs = new ArrayList<ExprAggregator>(opGroup.getAggregators());
//        for (Expr expr : exprs.getExprs().values()) {
//            ExprVars.varsMentioned(vars, expr);
//        }
//
//        // Are any of these vars single usage?
//        boolean modified = false;
//        for (Var var : vars) {
//            // Usage count will be 2 if we can eliminate the assignment
//            // First usage is when it is introduced by the assignment and the
//            // second is when it is used now in this group by
//            Expr e = getAssignExpr(var);
//            if (this.tracker.getUsageCount(var) == 2 && hasAssignment(var) && canInline(e)) {
//                // Can go back and eliminate that assignment
//                subOp = eliminateAssignment(subOp, var);
//                // Replace the variable usage with the expression in both the
//                // expressions and the aggregators
//                ExprTransform transform = new ExprTransformSubstitute(var, e);
//                exprs = processVarExprList(exprs, transform);
//                aggs = processAggregators(aggs, transform);
//                this.tracker.getAssignments().remove(var);
//                modified = true;
//            }
//        }
//
//        // Create a new group by if we've substituted any expressions
//        if (modified) {
//            return new OpGroup(subOp, exprs, aggs);
//        }
//
//        return super.transform(opGroup, subOp);
        //@formatter:on
    }

    private Op eliminateAssignment(Op subOp, Var var) {
        return Transformer.transform(new TransformRemoveAssignment(var, getAssignExpr(var)), subOp);
    }

    @SuppressWarnings("unused")
    private VarExprList processVarExprList(VarExprList exprs, ExprTransform transform) {
        VarExprList newExprs = new VarExprList();
        for (Var v : exprs.getVars()) {
            Expr e = exprs.getExpr(v);
            Expr e2 = ExprTransformer.transform(transform, e);
            newExprs.add(v, e2);
        }
        return newExprs;
    }

    @SuppressWarnings("unused")
    private List<ExprAggregator> processAggregators(List<ExprAggregator> aggs, ExprTransform transform) {
        List<ExprAggregator> newAggs = new ArrayList<ExprAggregator>();
        for (ExprAggregator agg : aggs) {
            ExprAggregator e2 = (ExprAggregator) ExprTransformer.transform(transform, agg);
            newAggs.add(e2);
        }
        return newAggs;
    }

    private static class AssignmentTracker extends VariableUsageTracker {

        private Map<Var, Expr> assignments = new HashMap<>();
        private int depth = 0;

        public Map<Var, Expr> getAssignments() {
            return this.assignments;
        }

        public void putAssignments(VarExprList assignments) {
            for (Var var : assignments.getVars()) {
                int i = getUsageCount(var);
                if (i <= 2) {
                    this.assignments.put(var, assignments.getExpr(var));
                } else {
                    this.assignments.remove(var);
                }
            }
        }

        @Override
        public void increment(String var) {
            super.increment(var);

            int i = getUsageCount(var);
            if (i > 2) {
                this.assignments.remove(var);
            }
        }

        public void updateAssignments(Var v, Expr e) {
            ExprTransformSubstitute transform = new ExprTransformSubstitute(v, e);
            for (Var assignVar : this.assignments.keySet()) {
                Expr assignExpr = this.assignments.get(assignVar);
                assignExpr = ExprTransformer.transform(transform, assignExpr);
                this.assignments.put(assignVar, assignExpr);
            }
        }

        public void incrementDepth() {
            this.depth++;
        }

        public void decrementDepth() {
            this.depth--;
            // Clear all assignments if not inside a project
            if (this.depth == 0)
                this.assignments.clear();
        }

        public boolean insideProjection() {
            return this.depth > 0;
        }
    }

    private static class AssignmentPusher extends VariableUsagePusher {

        private AssignmentTracker tracker;

        public AssignmentPusher(AssignmentTracker tracker) {
            super(tracker);
            this.tracker = tracker;
        }

        @Override
        public void visit(OpProject opProject) {
            super.visit(opProject);
            this.tracker.incrementDepth();
        }
    }

    private static class AssignmentPopper extends OpVisitorBase {

        private AssignmentTracker tracker;

        public AssignmentPopper(AssignmentTracker tracker) {
            this.tracker = tracker;
        }

        @Override
        public void visit(OpProject opProject) {
            // Any assignments that are not projected should be discarded at
            // this point
            Iterator<Var> vars = tracker.getAssignments().keySet().iterator();
            while (vars.hasNext()) {
                Var var = vars.next();
                if (!opProject.getVars().contains(var))
                    vars.remove();
            }
            tracker.pop();
            this.tracker.decrementDepth();
        }

    }
}
