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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.atlas.lib.Pair;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVars;
import com.hp.hpl.jena.sparql.algebra.Table;
import com.hp.hpl.jena.sparql.algebra.TableFactory;
import com.hp.hpl.jena.sparql.algebra.TransformCopy;
import com.hp.hpl.jena.sparql.algebra.op.*;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.core.VarExprList;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import com.hp.hpl.jena.sparql.expr.*;
import com.hp.hpl.jena.sparql.util.Context;

/**
 * A transform that aims to optimize queries where there is an inequality
 * constraint on a variable in an attempt to speed up evaluation e.g
 * 
 * <pre>
 * PREFIX rdf: &lt;http://www.w3.org/1999/02/22-rdf-syntax-ns#&gt;
 * SELECT *
 * WHERE
 * {
 *   ?s rdf:type &lt;http://type&gt; ;
 *      ?p ?o .
 *   FILTER(?p != rdf:type)
 * }
 * </pre>
 * 
 * Would transform to the following:
 * 
 * <pre>
 * PREFIX rdf: &lt;http://www.w3.org/1999/02/22-rdf-syntax-ns#&gt;
 * SELECT *
 * WHERE
 * {
 *   ?s rdf:type &lt;http://type&gt; ;
 *      ?p ?o .
 *   MINUS { VALUES ?p { rdf:type } }
 * }
 * </pre>
 * 
 * <h3>Status</h3>
 * <p>
 * Performance testing has shown that often this gives minimal performance
 * benefit so this optimization is not enabled by default. It may be explicitly
 * enabled by setting the {@link ARQ#optFilterInequality} symbol in your
 * {@link Context} or the ARQ global context ({@link ARQ#getContext()} to
 * {@code true}
 * </p>
 * 
 * <h3>Applicability</h3>
 * <p>
 * This optimizer is conservative in that it only makes the optimization where
 * the inequality constraint is against a non-literal as otherwise substituting
 * the value changes the query semantics because it switches from value equality
 * to the more restrictive term equality. The optimization is safe for
 * non-literals because for those value and term equality are equivalent (in
 * fact value equality is defined to be term equality).
 * </p>
 * <p>
 * There are also various nested algebra structures that can make the
 * optimization unsafe and so it does not take place if any of those situations
 * is detected.
 * </p>
 */
public class TransformFilterInequality extends TransformCopy {
    /**
     * Creates a new transform
     */
    public TransformFilterInequality() {
    }

    @Override
    public Op transform(OpFilter opFilter, Op subOp) {
        Op op = apply(opFilter.getExprs(), subOp);
        if (op == null)
            return super.transform(opFilter, subOp);
        return op;
    }

    private static Op apply(ExprList exprs, Op subOp) {
        // ---- Find and extract any inequality filters.
        Pair<List<Pair<Var, NodeValue>>, ExprList> p = preprocessFilterInequality(exprs);
        if (p == null || p.getLeft().size() == 0)
            return null;

        List<Pair<Var, NodeValue>> inequalities = p.getLeft();
        Collection<Var> varsMentioned = varsMentionedInInequalityFilters(inequalities);
        ExprList remaining = p.getRight();

        // If any of the conditions overlap the optimization is unsafe
        // (the query is also likely incorrect but that isn't our problem)

        // TODO There is actually a special case here, if the inequality
        // constraints are conflicting then we can special case to table empty.

        // ---- Check if the subOp is the right shape to transform.
        Op op = subOp;

        // Special case : deduce that the filter will always "eval unbound"
        // hence eliminate all rows. Return the empty table.
        if (testSpecialCaseUnused(subOp, inequalities, remaining))
            return OpTable.empty();

        // Special case: the deep left op of a OpConditional/OpLeftJoin is unit
        // table.
        // This is
        // { OPTIONAL{P1} OPTIONAL{P2} ... FILTER(?x = :x) }
        if (testSpecialCase1(subOp, inequalities, remaining)) {
            // Find backbone of ops
            List<Op> ops = extractOptionals(subOp);
            ops = processSpecialCase1(ops, inequalities);
            // Put back together
            op = rebuild((Op2) subOp, ops);
            // Put all filters - either we optimized, or we left alone.
            // Either way, the complete set of filter expressions.
            op = OpFilter.filter(exprs, op);
            return op;
        }

        // ---- Transform

        if (!safeToTransform(varsMentioned, op))
            return null;

        op = processFilterWorker(op, inequalities);

        // ---- Place any filter expressions around the processed sub op.
        if (remaining.size() > 0)
            op = OpFilter.filter(remaining, op);
        return op;
    }

    // --- find and extract
    private static Pair<List<Pair<Var, NodeValue>>, ExprList> preprocessFilterInequality(ExprList exprs) {
        List<Pair<Var, NodeValue>> exprsFilterInequality = new ArrayList<>();
        ExprList exprsOther = new ExprList();
        for (Expr e : exprs.getList()) {
            Pair<Var, NodeValue> p = preprocess(e);
            if (p != null)
                exprsFilterInequality.add(p);
            else
                exprsOther.add(e);
        }
        if (exprsFilterInequality.size() == 0)
            return null;
        return Pair.create(exprsFilterInequality, exprsOther);
    }

    private static Pair<Var, NodeValue> preprocess(Expr e) {
        if (!(e instanceof E_NotEquals))
            return null;

        ExprFunction2 eq = (ExprFunction2) e;
        Expr left = eq.getArg1();
        Expr right = eq.getArg2();

        Var var = null;
        NodeValue constant = null;

        if (left.isVariable() && right.isConstant()) {
            var = left.asVar();
            constant = right.getConstant();
        } else if (right.isVariable() && left.isConstant()) {
            var = right.asVar();
            constant = left.getConstant();
        }

        if (var == null || constant == null)
            return null;

        // Final check for "!=" where a FILTER != can do value matching when the
        // graph does not.
        // Value based?
        if (!ARQ.isStrictMode() && constant.isLiteral())
            return null;

        return Pair.create(var, constant);
    }

    private static Collection<Var> varsMentionedInInequalityFilters(List<Pair<Var, NodeValue>> inequalities) {
        Set<Var> vars = new HashSet<>();
        for (Pair<Var, NodeValue> p : inequalities)
            vars.add(p.getLeft());
        return vars;
    }

    private static boolean safeToTransform(Collection<Var> varsEquality, Op op) {
        // TODO This may actually be overly conservative, since we aren't going
        // to perform substitution we can potentially remove much of this method

        // Structure as a visitor?
        if (op instanceof OpBGP || op instanceof OpQuadPattern)
            return true;

        if (op instanceof OpFilter) {
            OpFilter opf = (OpFilter) op;
            // Expressions are always safe transform by substitution.
            return safeToTransform(varsEquality, opf.getSubOp());
        }

        // This will be applied also in sub-calls of the Transform but queries
        // are very rarely so deep that it matters.
        if (op instanceof OpSequence) {
            OpN opN = (OpN) op;
            for (Op subOp : opN.getElements()) {
                if (!safeToTransform(varsEquality, subOp))
                    return false;
            }
            return true;
        }

        if (op instanceof OpJoin || op instanceof OpUnion) {
            Op2 op2 = (Op2) op;
            return safeToTransform(varsEquality, op2.getLeft()) && safeToTransform(varsEquality, op2.getRight());
        }

        // Not safe unless filter variables are mentioned on the LHS.
        if (op instanceof OpConditional || op instanceof OpLeftJoin) {
            Op2 opleftjoin = (Op2) op;

            if (!safeToTransform(varsEquality, opleftjoin.getLeft()) || !safeToTransform(varsEquality, opleftjoin.getRight()))
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
            return safeToTransform(varsEquality, opg.getSubOp());
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
            return safeToTransform(varsEquality, opMod.getSubOp());
        }

        if (op instanceof OpGroup) {
            OpGroup opGroup = (OpGroup) op;
            VarExprList varExprList = opGroup.getGroupVars();
            return safeToTransform(varsEquality, varExprList) && safeToTransform(varsEquality, opGroup.getSubOp());
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

    private static boolean testSpecialCaseUnused(Op op, List<Pair<Var, NodeValue>> equalities, ExprList remaining) {
        // If the op does not contain the var at all, for some equality
        // then the filter expression will be "eval unbound" i.e. false.
        // We can return empty table.
        Set<Var> patternVars = OpVars.visibleVars(op);
        for (Pair<Var, NodeValue> p : equalities) {
            if (!patternVars.contains(p.getLeft()))
                return true;
        }
        return false;
    }

    // If a sequence of OPTIONALS, and nothing prior to the first, we end up
    // with a unit table on the left side of a next of LeftJoin/conditionals.

    private static boolean testSpecialCase1(Op op, List<Pair<Var, NodeValue>> equalities, ExprList remaining) {
        while (op instanceof OpConditional || op instanceof OpLeftJoin) {
            Op2 opleftjoin2 = (Op2) op;
            op = opleftjoin2.getLeft();
        }
        return isUnitTable(op);
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

    private static List<Op> processSpecialCase1(List<Op> ops, List<Pair<Var, NodeValue>> inequalities) {
        List<Op> ops2 = new ArrayList<>();
        Collection<Var> vars = varsMentionedInInequalityFilters(inequalities);

        for (Op op : ops) {
            Op op2 = op;
            if (safeToTransform(vars, op)) {
                op2 = processFilterWorker(op, inequalities);
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

    private static boolean isUnitTable(Op op) {
        if (op instanceof OpTable) {
            if (((OpTable) op).isJoinIdentity())
                return true;
        }
        return false;
    }

    private static Op processFilterWorker(Op op, List<Pair<Var, NodeValue>> inequalities) {
        // Firstly find all the possible values for each variable
        Map<Var, Set<NodeValue>> possibleValues = new HashMap<>();
        for (Pair<Var, NodeValue> inequalityTest : inequalities) {
            if (!possibleValues.containsKey(inequalityTest.getLeft())) {
                possibleValues.put(inequalityTest.getLeft(), new HashSet<NodeValue>());
            }
            possibleValues.get(inequalityTest.getLeft()).add(inequalityTest.getRight());
        }

        // Then combine them into all possible rows to be eliminated
        Table table = buildTable(possibleValues);

        // Then apply the MINUS
        return OpMinus.create(op, OpTable.create(table));
    }

    private static Table buildTable(Map<Var, Set<NodeValue>> possibleValues) {
        if (possibleValues.size() == 0)
            return TableFactory.createEmpty();
        Table table = TableFactory.create();

        // Although each filter condition must apply for a row to be accepted
        // they are actually independent since only one condition needs to fail
        // for the filter to reject the row. Thus for each unique variable/value
        // combination a single row must be produced
        for (Var v : possibleValues.keySet()) {
            for (NodeValue value : possibleValues.get(v)) {
                BindingMap b = BindingFactory.create();
                b.add(v, value.asNode());
                table.addBinding(b);
            }
        }
        return table;
    }
}
