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

import java.util.* ;

import org.apache.jena.JenaRuntime ;
import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.graph.Node ;
import org.apache.jena.query.ARQ ;
import org.apache.jena.rdf.model.impl.Util ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.algebra.OpVars ;
import org.apache.jena.sparql.algebra.TransformCopy ;
import org.apache.jena.sparql.algebra.op.* ;
import org.apache.jena.sparql.core.Substitute ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.core.VarExprList ;
import org.apache.jena.sparql.expr.* ;

/**
 * A transform that aims to optimize queries where there is an equality
 * constraint on a variable to speed up evaluation e.g
 * 
 * <pre>
 * SELECT *
 * WHERE
 * {
 *   ?s ?p ?o .
 *   FILTER(?s = &lt;http://subject&gt;)
 * }
 * </pre>
 * 
 * Would transform to the following:
 * 
 * <pre>
 * SELECT *
 * WHERE
 * {
 *   &lt;http://subject&gt; ?p ?o .
 *   BIND(&lt;http://subject&gt; AS ?s)
 * }
 * </pre>
 * 
 * <h3>Applicability</h3>
 * <p>
 * This optimizer is conservative in that it only makes the optimization where
 * the equality constraint is against a non-literal as otherwise substituting
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
public class TransformFilterEquality extends TransformCopy {
    // The approach taken for { OPTIONAL{} OPTIONAL{} } is more general ...
    // and better? Still need to be careful of double-nested OPTIONALS as
    // intermediates of a different value can block overall results so
    // don't mask immediately.
    
    public TransformFilterEquality() { }

    @Override
    public Op transform(OpFilter opFilter, Op subOp) {
        Op op = apply(opFilter.getExprs(), subOp);
        if (op == null)
            return super.transform(opFilter, subOp);
        return op;
    }

    private static Op apply(ExprList exprs, Op subOp) {
        // ---- Find and extract any equality filters.
        Pair<List<Pair<Var, NodeValue>>, ExprList> p = preprocessFilterEquality(exprs);
        if (p == null || p.getLeft().size() == 0)
            return null;

        List<Pair<Var, NodeValue>> equalities = p.getLeft();
        Collection<Var> varsMentioned = varsMentionedInEqualityFilters(equalities);
        ExprList remaining = p.getRight();

        // If any of the conditions overlap the optimization is unsafe
        // (the query is also likely incorrect but that isn't our problem)

        // TODO There is actually a special case here, if the equality
        // constraints are conflicting then we can special case to table empty.
        // At the very least we should check for the case where an equality
        // condition is duplicated
        if (varsMentioned.size() < equalities.size())
            return null;

        // ---- Check if the subOp is the right shape to transform.

        // Special case : deduce that the filter will always "eval unbound"
        // hence eliminate all rows. Return the empty table.
        if (testSpecialCaseUnused(subOp, equalities, remaining))
            // JENA-1184
            // If this is run after join-strategy, then scope is not a simple matter
            // of looking at the subOp.  But running before join-strategy
            // causes other code to not optimize (presumablty because it was developed
            // to run after join-strategy probably by conincidence)
            // @Test TestTransformFilters.equality04
            //return OpTable.empty();
            // JENA-1184 woraround. Return unchanged.
            return null ;

        // Special case: the deep left op of a OpConditional/OpLeftJoin is the unit table.
        // This is the case of:
        // { OPTIONAL{P1} OPTIONAL{P2} ... FILTER(?x = :x) }
        if (testSpecialCase1(subOp, equalities, remaining)) {
            // Find backbone of ops
            List<Op> ops = extractOptionals(subOp);
            ops = processSpecialCase1(ops, equalities);
            // Put back together
            Op op = rebuild((Op2) subOp, ops);
            // Put all filters - either we optimized, or we left alone.
            // Either way, the complete set of filter expressions.
            op = OpFilter.filterBy(exprs, op);
            return op;
        }

        // ---- Transform
        Op op = subOp;

        if (!safeToTransform(varsMentioned, op))
            return null;
        for (Pair<Var, NodeValue> equalityTest : equalities)
            op = processFilterWorker(op, equalityTest.getLeft(), equalityTest.getRight());

        // ---- Place any filter expressions around the processed sub op.
        if (remaining.size() > 0)
            op = OpFilter.filterBy(remaining, op);
        return op;
    }

    // --- find and extract
    private static Pair<List<Pair<Var, NodeValue>>, ExprList> preprocessFilterEquality(ExprList exprs) {
        List<Pair<Var, NodeValue>> exprsFilterEquality = new ArrayList<>();
        ExprList exprsOther = new ExprList();
        for (Expr e : exprs.getList()) {
            Pair<Var, NodeValue> p = preprocess(e);
            if (p != null)
                exprsFilterEquality.add(p);
            else
                exprsOther.add(e);
        }
        if (exprsFilterEquality.size() == 0)
            return null;
        return Pair.create(exprsFilterEquality, exprsOther);
    }

    private static Pair<Var, NodeValue> preprocess(Expr e) {
        if (!(e instanceof E_Equals) && !(e instanceof E_SameTerm))
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
        
        if ( constant.isIRI() || constant.isBlank() )
            return Pair.create(var, constant);

        // Literals.  Without knowing more, .equals is not the same as
        // SPARQL "=" (or .sameValueAs).  
        // In RDF 1.1, it is true of xsd:strings.

        if (e instanceof E_SameTerm) {
            if ( ! JenaRuntime.isRDF11 ) {
                // Corner case: sameTerm is false for string/plain literal,
                // but true in the in-memory graph for graph matching.
                // All becomes a non-issue in RDF 1.1
                if (!ARQ.isStrictMode() && constant.isString())
                    return null;
            }
            return Pair.create(var, constant);
        }

        // At this point, (e instanceof E_Equals)
        
        // 'constant' can be a folded expression - no node yet - so use asNode. 
        Node n = constant.asNode() ; 
        if ( JenaRuntime.isRDF11 ) {
            // RDF 1.1 : simple literals are xsd:strings.  
            if ( Util.isSimpleString(n) )
                return Pair.create(var, constant);
        } 
        
        // Otherwise, lexical forms are not 1-1 with values so not safe.
        // e.g. +001 and 1 are both integer value but different terms. 
        
        return null ;
    }

    private static Collection<Var> varsMentionedInEqualityFilters(List<Pair<Var, NodeValue>> equalities) {
        Set<Var> vars = new HashSet<>();
        for (Pair<Var, NodeValue> p : equalities)
            vars.add(p.getLeft());
        return vars;
    }

    private static boolean safeToTransform(Collection<Var> varsEquality, Op op) {
        // Structure as a visitor?
        if (op instanceof OpBGP || op instanceof OpQuadPattern)
            return true;

        if (op instanceof OpPath )
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
        return Collections.disjoint(varsExprList.getVars(), varsEquality);
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

    private static List<Op> processSpecialCase1(List<Op> ops, List<Pair<Var, NodeValue>> equalities) {
        List<Op> ops2 = new ArrayList<>();
        Collection<Var> vars = varsMentionedInEqualityFilters(equalities);

        for (Op op : ops) {
            Op op2 = op;
            if (safeToTransform(vars, op)) {
                for (Pair<Var, NodeValue> p : equalities)
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

    private static boolean isUnitTable(Op op) {
        if (op instanceof OpTable) {
            if (((OpTable) op).isJoinIdentity())
                return true;
        }
        return false;
    }

    // ---- Transformation

    private static Op processFilterWorker(Op op, Var var, NodeValue constant) {
        return subst(op, var, constant);
    }

    private static Op subst(Op subOp, Var var, NodeValue nv) {
        Op op = Substitute.substitute(subOp, var, nv.asNode());
        return OpAssign.assign(op, var, nv);
    }

    // Helper for TransformFilterDisjunction.

    /** Apply the FilterEquality transform or return null if no change */

    static Op processFilter(Expr e, Op subOp) {
        return apply(new ExprList(e), subOp);
    }
}
