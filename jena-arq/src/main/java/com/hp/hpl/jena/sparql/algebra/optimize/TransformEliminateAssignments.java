package com.hp.hpl.jena.sparql.algebra.optimize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.jena.atlas.lib.CollectionUtils;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVisitor;
import com.hp.hpl.jena.sparql.algebra.OpVisitorBase;
import com.hp.hpl.jena.sparql.algebra.Transform;
import com.hp.hpl.jena.sparql.algebra.TransformCopy;
import com.hp.hpl.jena.sparql.algebra.Transformer;
import com.hp.hpl.jena.sparql.algebra.op.OpAssign;
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
 * 
 * @author rvesse
 * 
 */
public class TransformEliminateAssignments extends TransformCopy {

    public static Op eliminate(Op op) {
        AssignmentTracker tracker = new AssignmentTracker();
        VariableUsagePusher pusher = new VariableUsagePusher(tracker);
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

    @Override
    public Op transform(OpExt opExt) {
        return opExt.apply(this, this.before, this.after);
    }

    @Override
    public Op transform(OpFilter opFilter, Op subOp) {
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
    public Op transform(OpAssign opAssign, Op subOp) {
        this.tracker.putAssignments(opAssign.getVarExprList());
        // Note that for assign we don't eliminate instances where its value is
        // never used because assign has different semantics to extend that
        // means in such a case it acts more like a filter
        return super.transform(opAssign, subOp);
    }

    @Override
    public Op transform(OpExtend opExtend, Op subOp) {
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
            if (this.tracker.getUsageCount(var) > 1)
                modified.add(var, assignments.getExpr(var));
        }

        if (modified.size() == assignments.size())
            return null;
        return modified;
    }

    @Override
    public Op transform(OpOrder opOrder, Op subOp) {
        // TODO Auto-generated method stub
        return super.transform(opOrder, subOp);
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

    }

    private static class AssignmentPopper extends OpVisitorBase {

        private AssignmentTracker tracker;

        public AssignmentPopper(AssignmentTracker tracker) {
            this.tracker = tracker;
        }

        @Override
        public void visit(OpProject opProject) {
            // Any assignments that are not projected should be discarded at
            // this
            // point
            Iterator<Var> vars = tracker.getAssignments().keySet().iterator();
            while (vars.hasNext()) {
                Var var = vars.next();
                if (!opProject.getVars().contains(var))
                    vars.remove();
            }
            tracker.pop();
        }

    }
}
