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
 *   ?t a ?type2 .
 *   FILTER(?s = ?t)
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
 *   ?s a ?type1 .
 *   BIND(?s AS ?t)
 * }
 * </pre>
 * <p>
 * The optimizer does not cover the implicit left join case, for that see
 * {@link TransformImplicitLeftJoin}
 * </p>
 * <h3>Applicability</h3>
 * <p>
 * This optimization aims to eliminate implicit joins of the form
 * {@code ?x = ?y} or {@code SAMETERM(?x, ?y)}, the latter can almost always be
 * safely eliminated while the former may only be eliminated in the case where
 * we can guarantee that at least one of the variables is a non-literal e.g. it
 * occurs in the subject/predicate position. In the case where this is not true
 * the optimization may not be made since we cannot assume that we can map value
 * equality to term equality by making the optimization.
 * </p>
 * <h3>Known Limitations/To Do</h3>
 * <ul>
 * <li>Application to implicit joins may block the sequence transform which
 * means the potential benefits of the optimization are negated</li>
 * </ul>
 */
public class TransformFilterImplicitJoin extends TransformCopy {

    @Override
    public Op transform(OpFilter opFilter, Op subOp) {
        Op op = apply(opFilter.getExprs(), subOp);
        if (op == null)
            return super.transform(opFilter, subOp);
        return op;
    }

    private static Op apply(ExprList exprs, Op subOp) {
        // ---- Find and extract any implicit join filters.
        Pair<List<Pair<Var, Var>>, ExprList> p = preprocessFilterImplicitJoin(subOp, exprs);
        if (p == null || p.getLeft().size() == 0)
            return null;

        List<Pair<Var, Var>> joins = p.getLeft();
        Collection<Var> varsMentioned = varsMentionedInImplictJoins(joins);
        ExprList remaining = p.getRight();

        // Not possible to optimize if multiple overlapping implicit joins
        // We can test this simply by checking that the number of vars in
        // varsMentioned is double the number of detected implicit joins

        // TODO In principal this may be safe provided we carefully apply the
        // substitutions in the correct order, this is left as a future
        // enhancement to this optimizer
        if (varsMentioned.size() != joins.size() * 2)
            return null;

        // ---- Check if the subOp is the right shape to transform.
        Op op = subOp;

        // Special case : deduce that the filter will always "eval unbound"
        // hence eliminate all rows. Return the empty table.
        if (testSpecialCaseUnused(subOp, joins, remaining))
            return OpTable.empty();

        // Special case: the deep left op of a OpConditional/OpLeftJoin is unit
        // table.
        // This is { OPTIONAL{P1} OPTIONAL{P2} ... FILTER(?x = :x) }
        if (testSpecialCaseOptional(subOp, joins, remaining)) {
            // Find backbone of ops
            List<Op> ops = extractOptionals(subOp);
            ops = processSpecialCaseOptional(ops, joins);
            // Put back together
            op = rebuild((Op2) subOp, ops);
            // Put all filters - either we optimized, or we left alone.
            // Either way, the complete set of filter expressions.
            op = OpFilter.filter(exprs, op);
            return op;
        }
        
        // Special case : filter is over a union where one/both sides are always false
        if (testSpecialCaseUnion(subOp, joins)) {
        	// This will attempt to eliminate the sides that are always false
        	op = processSpecialCaseUnion(subOp, joins);
        	
        	// In the case where both sides were invalid we'll have a table empty 
        	// operator at this point and can return immediately
        	if (op instanceof OpTable) return op;
        }

        // ---- Transform

        if (!safeToTransform(joins, varsMentioned, op))
            return null;
        for (Pair<Var, Var> implicitJoin : joins) {
            // TODO Where there are multiple conditions it may be necessary to
            // apply the substitutions more intelligently
            op = processFilterWorker(op, implicitJoin.getLeft(), implicitJoin.getRight());
        }

        // ---- Place any filter expressions around the processed sub op.
        if (remaining.size() > 0)
            op = OpFilter.filter(remaining, op);
        return op;
    }

    // --- find and extract
    private static Pair<List<Pair<Var, Var>>, ExprList> preprocessFilterImplicitJoin(Op subOp, ExprList exprs) {
        List<Pair<Var, Var>> exprsJoins = new ArrayList<>();
        ExprList exprsOther = new ExprList();
        for (Expr e : exprs.getList()) {
            Pair<Var, Var> p = preprocess(subOp, e);
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

    private static Pair<Var, Var> preprocess(Op subOp, Expr e) {
        if (!(e instanceof E_Equals) && !(e instanceof E_SameTerm))
            return null;

        ExprFunction2 eq = (ExprFunction2) e;
        Expr left = eq.getArg1();
        Expr right = eq.getArg2();

        if (!left.isVariable() || !right.isVariable()) {
            return null;
        }
        if (left.equals(right)) {
            return null;
        }

        if (e instanceof E_Equals) {
            // Is a safe equals for this optimization?
            Tuple<Set<Var>> varsByPosition = OpVars.mentionedVarsByPosition(subOp);

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

    // -- A special case

    private static boolean testSpecialCaseUnused(Op op, List<Pair<Var, Var>> joins, ExprList remaining) {
        // If the op does not contain one of the vars at all, then the implicit
        // join will be "eval unbound" i.e. false.
        // We can return empty table.
        Set<Var> patternVars = OpVars.visibleVars(op);
        for (Pair<Var, Var> p : joins) {
            if (!patternVars.contains(p.getLeft()) || !patternVars.contains(p.getRight()))
                return true;
        }
        return false;
    }

    // If a sequence of OPTIONALS, and nothing prior to the first, we end up
    // with a unit table on the left side of a next of LeftJoin/conditionals.

    private static boolean testSpecialCaseOptional(Op op, List<Pair<Var, Var>> joins, ExprList remaining) {
        while (op instanceof OpConditional || op instanceof OpLeftJoin) {
            Op2 opleftjoin2 = (Op2) op;
            op = opleftjoin2.getLeft();
        }
        return isTableUnit(op);
    }
    
    private static boolean testSpecialCaseUnion(Op op, List<Pair<Var, Var>> joins) {
    	if (op instanceof OpUnion) {
    		OpUnion union = (OpUnion) op;
    		Set<Var> leftVars = OpVars.visibleVars(union.getLeft());
    		Set<Var> rightVars = OpVars.visibleVars(union.getRight());
    		
    		// Is a special case if there is any implicit join where only one of the variables mentioned in an
    		// implicit join is present on one side of the union
    		for (Pair<Var, Var> p : joins) {
    			if (!leftVars.contains(p.getLeft()) || !leftVars.contains(p.getRight())) return true;
    			if (!rightVars.contains(p.getLeft()) || !rightVars.contains(p.getRight())) return true;
    		}
    	}
    	return false;
    }

    private static List<Op> extractOptionals(Op op) {
        List<Op> chain = new ArrayList<>();
        while (op instanceof OpConditional || op instanceof OpLeftJoin) {
            Op2 opleftjoin2 = (Op2) op;
            chain.add(opleftjoin2.getRight());
            op = opleftjoin2.getLeft();
        }
        return chain;
    }

    private static List<Op> processSpecialCaseOptional(List<Op> ops, List<Pair<Var, Var>> joins) {
        List<Op> ops2 = new ArrayList<>();
        Collection<Var> vars = varsMentionedInImplictJoins(joins);

        for (Op op : ops) {
            Op op2 = op;
            if (safeToTransform(joins, vars, op)) {
                for (Pair<Var, Var> p : joins)
                    op2 = processFilterWorker(op, p.getLeft(), p.getRight());
            }
            ops2.add(op2);
        }
        return ops2;
    }

    private static Op rebuild(Op2 subOp, List<Op> ops) {
        Op chain = OpTable.unit();
        for (Op op : ops) {
            chain = subOp.copy(chain, op);
        }
        return chain;
    }

    private static boolean isTableUnit(Op op) {
        if (op instanceof OpTable) {
            if (((OpTable) op).isJoinIdentity())
                return true;
        }
        return false;
    }
    
    private static Op processSpecialCaseUnion(Op op, List<Pair<Var, Var>> joins) {
    	if (op instanceof OpUnion) {
    		OpUnion union = (OpUnion) op;
    		
    		Set<Var> leftVars = OpVars.visibleVars(union.getLeft());
    		Set<Var> rightVars = OpVars.visibleVars(union.getRight());
    		
    		// Is a special case if there is any implicit join where only one of the variables mentioned in an
    		// implicit join is present on one side of the union
    		boolean leftEmpty = false, rightEmpty = false;
    		for (Pair<Var, Var> p : joins) {
    			if (leftEmpty || !leftVars.contains(p.getLeft()) || !leftVars.contains(p.getRight())) leftEmpty = true;
    			if (rightEmpty || !rightVars.contains(p.getLeft()) || !rightVars.contains(p.getRight())) rightEmpty = true;
    		}
    		
    		// If both sides of the union guarantee to produce errors then just replace the whole thing with table empty
    		if (leftEmpty && rightEmpty) return OpTable.empty();
    		if (leftEmpty) return union.getRight();
    		if (rightEmpty) return union.getLeft();
    	}
    	// Leave untouched
    	return op;
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
