package com.hp.hpl.jena.sparql.algebra.optimize;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.TransformCopy;
import com.hp.hpl.jena.sparql.algebra.op.OpAssign;
import com.hp.hpl.jena.sparql.algebra.op.OpExtend;
import com.hp.hpl.jena.sparql.algebra.op.OpExtendAssign;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.core.VarExprList;
import com.hp.hpl.jena.sparql.expr.Expr;

/**
 * A transform capable of removing assignments from the algebra tree
 * 
 */
public class TransformRemoveAssignment extends TransformCopy {

    private Var var;
    private Expr expr;
    private boolean topmostOnly = true;

    public TransformRemoveAssignment(Var var, Expr expr, boolean topmostOnly) {
        this.var = var;
        this.expr = expr;
        this.topmostOnly = topmostOnly;
    }

    public TransformRemoveAssignment(Var var, Expr expr) {
        this(var, expr, true);
    }

    @Override
    public Op transform(OpAssign opAssign, Op subOp) {
        VarExprList assignments = processAssignments(opAssign);
        if (assignments == null)
            return super.transform(opAssign, subOp);

        // Rewrite appropriately
        if (this.topmostOnly) {
            // If topmost only ignore any transformations lower down the tree
            // hence call getSubOp() rather than using the provided subOp
            if (assignments.size() > 0) {
                return OpAssign.assign(opAssign.getSubOp(), assignments);
            } else {
                return opAssign.getSubOp();
            }
        } else {
            // Otherwise preserve any transformations from lower down the tree
            if (assignments.size() > 0) {
                return OpAssign.assign(subOp, assignments);
            } else {
                return subOp;
            }
        }
    }

    private VarExprList processAssignments(OpExtendAssign opAssign) {
        VarExprList orig = opAssign.getVarExprList();
        if (!orig.contains(this.var))
            return null;
        if (!orig.getExpr(this.var).equals(this.expr))
            return null;

        VarExprList modified = new VarExprList();
        for (Var v : orig.getVars()) {
            if (!v.equals(this.var)) {
                modified.add(v, orig.getExpr(v));
            }
        }
        return modified;
    }

    @Override
    public Op transform(OpExtend opExtend, Op subOp) {
        VarExprList assignments = processAssignments(opExtend);
        if (assignments == null)
            return super.transform(opExtend, subOp);

        // Rewrite appropriately
        if (this.topmostOnly) {
            // If topmost only ignore any transformations lower down the tree
            // hence call getSubOp() rather than using the provided subOp
            if (assignments.size() > 0) {
                return OpExtend.extend(opExtend.getSubOp(), assignments);
            } else {
                return opExtend.getSubOp();
            }
        } else {
            // Otherwise preserve any transformations from lower down the tree
            if (assignments.size() > 0) {
                return OpExtend.extend(subOp, assignments);
            } else {
                return subOp;
            }
        }
    }

}
