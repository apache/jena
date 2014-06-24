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

import static org.apache.jena.atlas.lib.CollectionUtils.disjoint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.atlas.lib.Tuple;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVars;
import com.hp.hpl.jena.sparql.algebra.TransformCopy;
import com.hp.hpl.jena.sparql.algebra.op.*;
import com.hp.hpl.jena.sparql.core.Substitute;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.core.VarExprList;
import com.hp.hpl.jena.sparql.expr.*;

/**
 * <p>
 * Optimizer for transforming implicit joins. These covers queries like the
 * following:
 * </p>
 * 
 * <pre>
 * SELECT *
 * WHERE
 * {
 *   ?s a ?type1 .
 *   OPTIONAL
 *   {
 *     ?t a ?type2 .
 *     FILTER(?s = ?t)
 *   }
 * }
 * </pre>
 * <p>
 * Clearly this is a trivial example but doing this optimization can have big
 * performance gains since it can completely eliminate cross products that we
 * would otherwise be required to evaluate. The optimization where applicable
 * results in a query of the following form:
 * </p>
 * 
 * <pre>
 * SELECT *
 * WHERE
 * {
 *   ?s a ?type1 .
 *   OPTIONAL
 *   {
 *     ?s a ?type1 .
 *     BIND(?s AS ?t)
 *   }
 * }
 * </pre>
 * <p>
 * This does not handle the simpler case of implicit joins where
 * {@code OPTIONAL} is not involved, for that see
 * {@link TransformFilterImplicitJoin}
 * </p>
 * <h3>Applicability</h3>
 * <p>
 * This optimization aims to eliminate implicit left joins of the form
 * {@code ?x = ?y} or {@code SAMETERM(?x, ?y)}, the latter can almost always be
 * safely eliminated while the former may only be eliminated in the case where
 * we can guarantee that at least one of the variables is a non-literal e.g. it
 * occurs in the subject/predicate position. In the case where this is not true
 * the optimization may not be made since we cannot assume that we can map value
 * equality to term equality by making the optimization.
 * </p>
 */
public class TransformImplicitLeftJoin extends TransformCopy {

    @Override
    public Op transform(OpLeftJoin opLeftJoin, Op left, Op right) {
        // Must have associated expressions to be eligible
        if (opLeftJoin.getExprs() == null)
            return super.transform(opLeftJoin, left, right);

        // Try and apply this optimization
        Op op = apply(opLeftJoin, left, right);
        if (op == null)
            return super.transform(opLeftJoin, left, right);
        return op;
    }

    private static Op apply(OpLeftJoin opLeftJoin, Op left, Op right) {
        // This handles arbitrarily nested && conditions
        ExprList orig = ExprList.splitConjunction(opLeftJoin.getExprs());
        
        // Extract optimizable conditions?
        Pair<List<Pair<Var, Var>>, ExprList> p = preprocessFilterImplicitJoin(left, right, orig);
        
        // Were there any optimizable conditions?
        if (p == null || p.getLeft().size() == 0)
            return null;

        List<Pair<Var, Var>> joins = p.getLeft();
        Collection<Var> varsMentioned = varsMentionedInImplictJoins(joins);
        ExprList remaining = p.getRight();

        // ---- Check if the subOp is the right shape to transform.
        Op op = right;
        if (!safeToTransform(joins, varsMentioned, op))
            return null;

        // We apply substitution only to the RHS
        // This is because applying to both sides would change the join
        // semantics of the Left Join
        Collection<Var> lhsVars = OpVars.visibleVars(left);
        Collection<Var> rhsVars = OpVars.visibleVars(right);

        // TODO A better approach here would be to build a dependency graph of
        // the implicit joins and use that to inform the order in which they
        // should be carried out or even to remove some entirely where
        // transitivity and commutativity apply

        for (Pair<Var, Var> implicitJoin : joins) {
            // Which variable do we want to substitute out?
            // We don't need to deal with the case of neither variable being on
            // the RHS
            Var lVar = implicitJoin.getLeft();
            Var rVar = implicitJoin.getRight();

            if (lhsVars.contains(lVar) && lhsVars.contains(rVar)) {
                // Both vars are on LHS

                if (rhsVars.contains(lVar) && rhsVars.contains(rVar)) {
                    // Both vars are also on RHS
                    // Order of substitution doesn't matter
                    op = processFilterWorker(op, lVar, rVar);
                } else if (rhsVars.contains(lVar)) {
                    // Substitute left variable for right variable
                    op = processFilterWorker(op, lVar, rVar);
                } else if (rhsVars.contains(rVar)) {
                    // Substitute right variable for left variable
                    op = processFilterWorker(op, rVar, lVar);
                } else {
                    // May be hit if trying to apply a sequence of
                    // substitutions
                    return null;
                }
            } else if (lhsVars.contains(lVar)) {
                // Only left variable on RHS

                if (rhsVars.contains(rVar)) {
                    // Substitute right variable for left variable
                    op = processFilterWorker(op, rVar, lVar);
                } else {
                    // May be hit if trying to apply a sequence of substitutions
                    return null;
                }
            } else if (lhsVars.contains(rVar)) {
                // Only right variable on LHS

                if (rhsVars.contains(lVar)) {
                    // Substitute left variable for right variable
                    op = processFilterWorker(op, lVar, rVar);
                } else {
                    // May be hit if trying to apply a sequence of substitutions
                    return null;
                }
            } else {
                // Neither variable is on LHS

                if (rhsVars.contains(lVar) && rhsVars.contains(rVar)) {
                    // Both variables are on RHS so can substitute one for the
                    // other
                    op = processFilterWorker(op, lVar, rVar);
                } else {
                    // May be hit if trying to apply a sequence of substitutions
                    return null;
                }
            }

            // Re-compute visible RHS vars after each substitution as it may
            // affect subsequent substitutions
            rhsVars = OpVars.visibleVars(op);
        }

        if (remaining.size() > 0) {
            return OpLeftJoin.create(left, op, remaining);
        } else {
            return OpLeftJoin.create(left, op, (ExprList) null);
        }
    }

    private static Pair<List<Pair<Var, Var>>, ExprList> preprocessFilterImplicitJoin(Op left, Op right, ExprList exprs) {
        List<Pair<Var, Var>> exprsJoins = new ArrayList<>();
        ExprList exprsOther = new ExprList();
        for (Expr e : exprs.getList()) {
            Pair<Var, Var> p = preprocess(left, right, e);
            if (p != null) {
                exprsJoins.add(p);
            } else {
                exprsOther.add(e);
            }
        }
        if (exprsJoins.size() == 0)
            return null;
        return Pair.create(exprsJoins, exprsOther);
    }

    private static Pair<Var, Var> preprocess(Op opLeft, Op opRight, Expr e) {
        if (!(e instanceof E_Equals) && !(e instanceof E_SameTerm))
            return null;

        ExprFunction2 eq = (ExprFunction2) e;
        // An equals or same term implicit join
        Expr left = eq.getArg1();
        Expr right = eq.getArg2();

        if (!left.isVariable() || !right.isVariable()) {
            return null;
        }
        if (left.equals(right)) {
            return null;
        }

        // If neither variable is visible in RHS optimization does not apply
        Collection<Var> rhsVars = OpVars.visibleVars(opRight);
        if (!rhsVars.contains(left.asVar()) && !rhsVars.contains(right.asVar()))
            return null;

        if (e instanceof E_Equals) {
            // Is a safe equals for this optimization?
            Tuple<Set<Var>> varsByPosition = OpVars.mentionedVarsByPosition(opLeft, opRight);

            if (!isSafeEquals(varsByPosition, left.asVar(), right.asVar()))
                return null;
        }

        return Pair.create(left.asVar(), right.asVar());
    }

    private static boolean isSafeEquals(Tuple<Set<Var>> varsByPosition, Var left, Var right) {
        // For equality based joins ensure at least one variable must be
        // used in graph/subject/predicate position thus guaranteeing it to
        // not be a literal so replacing with term equality by ways of
        // substitution will be safe

        // Should get 5 sets
        if (varsByPosition.size() != 5)
            return false;

        // If anything is used in the object/unknown position then we
        // potentially have an issue unless it is also used in a safe
        // position
        Set<Var> safeVars = new HashSet<>();
        safeVars.addAll(varsByPosition.get(0));
        safeVars.addAll(varsByPosition.get(1));
        safeVars.addAll(varsByPosition.get(2));
        Set<Var> unsafeVars = new HashSet<>();
        unsafeVars.addAll(varsByPosition.get(3));
        unsafeVars.addAll(varsByPosition.get(4));
        boolean lhsSafe = true, rhsSafe = true;
        if (unsafeVars.size() > 0) {
            if (unsafeVars.contains(left)) {
                // LHS Variable occurred in unsafe position
                if (!safeVars.contains(left)) {
                    // LHS Variable is unsafe
                    lhsSafe = false;
                }
            }
            if (unsafeVars.contains(right)) {
                // RHS Variable occurred in unsafe position
                if (!safeVars.contains(right)) {
                    // RHS Variable is unsafe
                    rhsSafe = false;
                }
            }
        }

        // At least one variable must be safe or this equality expression is
        // not an implicit join that can be safely optimized
        return lhsSafe || rhsSafe;
    }

    private static Collection<Var> varsMentionedInImplictJoins(List<Pair<Var, Var>> joins) {
        Set<Var> vars = new HashSet<>();
        for (Pair<Var, Var> p : joins) {
            vars.add(p.getLeft());
            vars.add(p.getRight());
        }
        return vars;
    }

    private static boolean safeToTransform(List<Pair<Var, Var>> joins, Collection<Var> varsEquality, Op op) {
        // Structure as a visitor?
        if (op instanceof OpBGP || op instanceof OpQuadPattern)
            return true;

        if (op instanceof OpFilter) {
            OpFilter opf = (OpFilter) op;
            return safeToTransform(joins, varsEquality, opf.getSubOp());
        }

        // This will be applied also in sub-calls of the Transform but queries
        // are very rarely so deep that it matters.
        if (op instanceof OpSequence) {
            OpN opN = (OpN) op;
            for (Op subOp : opN.getElements()) {
                if (!safeToTransform(joins, varsEquality, subOp))
                    return false;
            }
            return true;
        }

        if (op instanceof OpJoin) {
            Op2 op2 = (Op2) op;
            return safeToTransform(joins, varsEquality, op2.getLeft()) && safeToTransform(joins, varsEquality, op2.getRight());
        }
        
        if (op instanceof OpUnion) {
        	// True only if for any pairs that affect the pattern both variables occur
        	Set<Var> fixedVars = OpVars.fixedVars(op);
        	for (Pair<Var, Var> pair : joins) {
        		if (fixedVars.contains(pair.getLeft()) && !fixedVars.contains(pair.getRight())) return false;
        		if (!fixedVars.contains(pair.getLeft()) && fixedVars.contains(pair.getRight())) return false;
        	}
        	return true;
        }

        // Not safe unless filter variables are mentioned on the LHS.
        if (op instanceof OpConditional || op instanceof OpLeftJoin) {
            Op2 opleftjoin = (Op2) op;

            if (!safeToTransform(joins, varsEquality, opleftjoin.getLeft())
                    || !safeToTransform(joins, varsEquality, opleftjoin.getRight()))
                return false;

            // Not only must the left and right be safe to transform,
            // but the equality variable must be known to be always set.

            // If the varsLeft are disjoint from assigned vars,
            // we may be able to push assign down right
            // (this generalises the unit table case specialcase1)
            // Needs more investigation.

            Op opLeft = opleftjoin.getLeft();
            Set<Var> varsLeft = OpVars.visibleVars(opLeft);
            if (varsLeft.containsAll(varsEquality))
                return true;
            return false;
        }

        if (op instanceof OpGraph) {
            OpGraph opg = (OpGraph) op;
            return safeToTransform(joins, varsEquality, opg.getSubOp());
        }

        // Subquery - assume scope rewriting has already been applied.
        if (op instanceof OpModifier) {
            // ORDER BY?
            OpModifier opMod = (OpModifier) op;
            if (opMod instanceof OpProject) {
                OpProject opProject = (OpProject) op;
                // Writing "SELECT ?var" for "?var" -> a value would need
                // AS-ification.
                for (Var v : opProject.getVars()) {
                    if (varsEquality.contains(v))
                        return false;
                }
            }
            return safeToTransform(joins, varsEquality, opMod.getSubOp());
        }

        if (op instanceof OpGroup) {
            OpGroup opGroup = (OpGroup) op;
            VarExprList varExprList = opGroup.getGroupVars();
            return safeToTransform(varsEquality, varExprList) && safeToTransform(joins, varsEquality, opGroup.getSubOp());
        }

        if (op instanceof OpTable) {
            OpTable opTable = (OpTable) op;
            if (opTable.isJoinIdentity())
                return true;
        }

        // Op1 - OpGroup
        // Op1 - OpOrder
        // Op1 - OpAssign, OpExtend
        // Op1 - OpFilter - done.
        // Op1 - OpLabel - easy
        // Op1 - OpService - no.

        return false;
    }

    private static boolean safeToTransform(Collection<Var> varsEquality, VarExprList varsExprList) {
        // If the named variable is used, unsafe to rewrite.
        return disjoint(varsExprList.getVars(), varsEquality);
    }

    // ---- Transformation

    private static Op processFilterWorker(Op op, Var find, Var replace) {
        return subst(op, find, replace);
    }

    private static Op subst(Op subOp, Var find, Var replace) {
        Op op = Substitute.substitute(subOp, find, replace);
        return OpAssign.assign(op, find, new ExprVar(replace));
    }
}
