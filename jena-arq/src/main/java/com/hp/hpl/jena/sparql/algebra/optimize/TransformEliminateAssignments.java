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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.jena.atlas.lib.CollectionUtils;

import com.hp.hpl.jena.query.SortCondition;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVisitor;
import com.hp.hpl.jena.sparql.algebra.OpVisitorBase;
import com.hp.hpl.jena.sparql.algebra.Transform;
import com.hp.hpl.jena.sparql.algebra.TransformCopy;
import com.hp.hpl.jena.sparql.algebra.Transformer;
import com.hp.hpl.jena.sparql.algebra.op.OpExt;
import com.hp.hpl.jena.sparql.algebra.op.OpExtend;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpGroup;
import com.hp.hpl.jena.sparql.algebra.op.OpOrder;
import com.hp.hpl.jena.sparql.algebra.op.OpProject;
import com.hp.hpl.jena.sparql.algebra.op.OpTopN;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.core.VarExprList;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.ExprTransformSubstitute;
import com.hp.hpl.jena.sparql.expr.ExprTransformer;
import com.hp.hpl.jena.sparql.expr.ExprVars;

/**
 * A transform that tries to remove unecessary assignments
 * <p>
 * There are two classes of assignments that we can try and remove:
 * </p>
 * <ol>
 * <li>Assignments where the assigned variable is used only once in a subsequent
 * assignment</li>
 * <li>Assignments where the assigned value is never used elsewhere</li>
 * </ol>
 * <p>
 * Both of these changes can only happen inside of projections as otherwise we
 * have to assume that the user may need the resulting variable and thus we
 * leave the assignment alone.
 * </p>
 * 
 */
public class TransformEliminateAssignments extends TransformCopy {

    public static Op eliminate(Op op) {
        AssignmentTracker tracker = new AssignmentTracker();
        AssignmentPusher pusher = new AssignmentPusher(tracker);
        AssignmentPopper popper = new AssignmentPopper(tracker);
        Transform transform = new TransformEliminateAssignments(tracker, pusher, popper);

        return Transformer.transform(transform, op, pusher, popper);
    }

    private OpVisitor before, after;
    private AssignmentTracker tracker;

    private TransformEliminateAssignments(AssignmentTracker tracker, OpVisitor before, OpVisitor after) {
        this.tracker = tracker;
        this.before = before;
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
            if (this.tracker.getUsageCount(var) == 2 && this.tracker.getAssignments().containsKey(var)) {
                // Can go back and eliminate that assignment
                subOp = Transformer.transform(
                        new TransformRemoveAssignment(var, this.tracker.getAssignments().get(var)), subOp);
                // Replace the variable usage with the expression
                exprs = ExprTransformer.transform(
                        new ExprTransformSubstitute(var, this.tracker.getAssignments().get(var)), exprs);
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

    @Override
    public Op transform(OpExtend opExtend, Op subOp) {
        // No point tracking assignments if not in a projection as we can't
        // possibly eliminate them without a projection to hide the fact that
        // the assigned value is unnecessary or only used once
        if (!this.tracker.insideProjection())
            return super.transform(opExtend, subOp);

        // Track the assignments for future reference
        this.tracker.putAssignments(opExtend.getVarExprList());

        // See if there are any assignments we can eliminate entirely i.e. those
        // where the assigned value is never used
        VarExprList assignments = processUnused(opExtend.getVarExprList());
        if (assignments == null)
            return super.transform(opExtend, subOp);

        // Can eliminate some assignments entirely
        if (assignments.size() > 0) {
            return OpExtend.extend(subOp, assignments);
        } else {
            return subOp;
        }
    }

    private VarExprList processUnused(VarExprList assignments) {
        if (CollectionUtils.disjoint(assignments.getVars(), this.tracker.getAssignments().keySet()))
            return null;

        VarExprList modified = new VarExprList();
        for (Var var : assignments.getVars()) {
            // If an assignment is used more than once then it must be preserved
            // for now
            if (this.tracker.getUsageCount(var) > 1)
                modified.add(var, assignments.getExpr(var));
        }

        // If all assignments are used more than once then there are no changes
        // and we return null
        if (modified.size() == assignments.size())
            return null;

        return modified;
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
            // second is when it is used now in this filter
            if (this.tracker.getUsageCount(var) == 2 && this.tracker.getAssignments().containsKey(var)) {
                // Can go back and eliminate that assignment
                subOp = Transformer.transform(
                        new TransformRemoveAssignment(var, this.tracker.getAssignments().get(var)), subOp);
                // Replace the variable usage with the expression within the sort conditions
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
            e = ExprTransformer.transform(new ExprTransformSubstitute(var, this.tracker.getAssignments().get(var)), e);
            outputConditions.add(new SortCondition(e, cond.getDirection()));
        }
       
        return outputConditions;
    }

    @Override
    public Op transform(OpTopN opTop, Op subOp) {
        // TODO Auto-generated method stub
        return super.transform(opTop, subOp);
    }

    @Override
    public Op transform(OpGroup opGroup, Op subOp) {
        // TODO Auto-generated method stub
        return super.transform(opGroup, subOp);
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
