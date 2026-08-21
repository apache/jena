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

package org.apache.jena.sparql.algebra.optimize;

import java.util.ArrayList ;
import java.util.List ;

import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.algebra.TransformCopy ;
import org.apache.jena.sparql.algebra.op.OpDisjunction ;
import org.apache.jena.sparql.algebra.op.OpFilter ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.expr.E_Equals ;
import org.apache.jena.sparql.expr.E_LogicalOr ;
import org.apache.jena.sparql.expr.E_SameTerm ;
import org.apache.jena.sparql.expr.Expr ;
import org.apache.jena.sparql.expr.ExprEvalException ;
import org.apache.jena.sparql.expr.ExprFunction2 ;
import org.apache.jena.sparql.expr.ExprList ;
import org.apache.jena.sparql.expr.NodeValue ;

/**
 * Filter disjunction. This covers the case of
 * <pre>
 *  (filter (|| expr1 expr2) pattern)</pre>
 * where either or both of {@code expr1} and {@code expr2} are equalities that help
 * ground the pattern. This includes {@code ?x IN (....)} so this optimization can a
 * significant improvement.
 * <p>
 * The rewrite evaluates the pattern once per disjunct, so it is only sound when at
 * most one disjunct can be true of any one solution; otherwise a solution satisfying
 * several disjuncts is returned once per satisfied disjunct where the filter returns
 * it once. Repeated disjuncts are dropped first, and the rewrite is then applied only
 * when every disjunct tests the same variable against a constant ({@code =} or
 * {@code sameTerm}) and the constants are pairwise known to be different values. Any
 * other disjunction is left as a filter.
 */

public class TransformFilterDisjunction extends TransformCopy {
    public TransformFilterDisjunction() {}

    @Override
    public Op transform(OpFilter opFilter, final Op subOp) {
        ExprList exprList = opFilter.getExprs();

        // First pass - any disjunctions at all?
        boolean processDisjunction = false;
        for ( Expr expr : exprList ) {
            if ( isDisjunction(expr) ) {
                processDisjunction = true;
                break;
            }
        }

        // Still may be a disjunction in a form we don't optimize.
        if ( !processDisjunction )
            return super.transform(opFilter, subOp);

        ExprList exprList2 = new ExprList();
        Op newOp = subOp;

        for ( Expr expr : exprList ) {
            if ( !isDisjunction(expr) ) {
                // not for this transform.
                exprList2.add(expr);
                continue;
            }

            Op op2 = expandDisjunction(expr, newOp);
            if ( op2 == null ) {
                // A disjunction this transform can not rewrite soundly.
                // Leave it as a filter expression.
                exprList2.add(expr);
                continue;
            }
            newOp = op2;
        }

        if ( newOp == subOp )
            // No disjunction was expanded.
            return super.transform(opFilter, subOp);

        if ( exprList2.isEmpty() )
            return newOp;

        // Put the non-disjunctions outside the disjunction and the pattern rewrite.
        Op opOther = OpFilter.filterBy(exprList2, newOp);
        if ( opOther instanceof OpFilter ) {
            return opOther;
        }

        // opOther is not a filter any more - should not happen but to isolate from
        // future changes ...
        Log.warn(this, "FilterDisjunction assumption failure: not a filter after processing disjunction/other mix");
        return super.transform(opFilter, subOp);
    }

    private boolean isDisjunction(Expr expr) {
        return (expr instanceof E_LogicalOr);
    }

    /**
     * Expand a disjunction into a union of the pattern grounded per disjunct, or null
     * when that is not possible or not sound.
     */
    public static Op expandDisjunction(Expr expr, Op subOp) {
        List<Expr> exprList = explodeDisjunction(new ArrayList<Expr>(), expr);

        // (A || A) is A: drop repeated disjuncts rather than build identical branches.
        // Generated queries really do contain the same disjunct twice - LDBC SPB writes
        // FILTER(?pf = :c || ?pf = :c) - and a single disjunct then grounds the pattern.
        List<Expr> distinct = new ArrayList<>(exprList.size());
        for ( Expr e : exprList ) {
            if ( !distinct.contains(e) )
                distinct.add(e);
        }
        exprList = distinct;

        if ( !isSafeDisjunction(exprList) )
            return null;

        // All disjunctions - some can be done efficiently via assignments,
        // some can not (value tests).
        List<Expr> exprList2 = null;
        Op op = null;
        for ( Expr e : exprList ) {
            Op op2 = TransformFilterEquality.processFilter(e, subOp);
            if ( op2 == null ) {
                // Not done.
                if ( exprList2 == null )
                    exprList2 = new ArrayList<>();
                exprList2.add(e);
            }

            op = OpDisjunction.create(op, op2);
        }

        if ( exprList2 != null && !exprList2.isEmpty() ) {
            // These are left as disjunctions.
            Expr eOther = null;
            for ( Expr e : exprList2 ) {
                if ( eOther == null )
                    eOther = e;
                else
                    eOther = new E_LogicalOr(eOther, e);
            }
            Op opOther = OpFilter.filter(eOther, subOp);
            op = OpDisjunction.create(op, opOther);
        }

        return op;
    }

    /**
     * Is at most one disjunct true of any one solution? Each branch of the expansion
     * re-evaluates the pattern, so a solution that satisfies {@code k} disjuncts comes
     * back {@code k} times where the filter returns it once. For example
     * <pre>
     *  FILTER(?x = :c || ?x != :d)</pre>
     * must not be expanded: a solution with {@code ?x = :c} satisfies both disjuncts.
     * <p>
     * The safe case is: every disjunct tests one and the same variable against a
     * constant, and the constants are pairwise known to be different values, so the
     * tests are mutually exclusive. Constants whose comparison is indeterminate (an
     * unknown datatype, a timezone-less date) are treated as possibly equal.
     */
    private static boolean isSafeDisjunction(List<Expr> exprList) {
        Var var = null;
        List<NodeValue> constants = new ArrayList<>(exprList.size());
        for ( Expr e : exprList ) {
            NodeValue constant = constantTestedAgainst(e, var);
            if ( constant == null )
                return false;
            if ( var == null )
                var = singleVariable(e);
            constants.add(constant);
        }

        for ( int i = 0 ; i < constants.size() ; i++ ) {
            for ( int j = i + 1 ; j < constants.size() ; j++ ) {
                if ( !provablyDistinctValues(constants.get(i), constants.get(j)) )
                    return false;
            }
        }
        return true;
    }

    /**
     * The constant of a {@code variable = constant} or {@code sameTerm(variable, constant)}
     * disjunct (either argument order), where the variable is {@code var} - or any
     * variable when {@code var} is null. Null when the disjunct has another shape.
     */
    private static NodeValue constantTestedAgainst(Expr e, Var var) {
        if ( !(e instanceof E_Equals) && !(e instanceof E_SameTerm) )
            return null;
        ExprFunction2 test = (ExprFunction2)e;
        Expr left = test.getArg1();
        Expr right = test.getArg2();
        Expr varExpr = null;
        Expr constExpr = null;
        if ( left.isVariable() && right.isConstant() ) {
            varExpr = left;
            constExpr = right;
        } else if ( right.isVariable() && left.isConstant() ) {
            varExpr = right;
            constExpr = left;
        } else
            return null;
        if ( var != null && !var.equals(varExpr.asVar()) )
            return null;
        return constExpr.getConstant();
    }

    /** The variable of a disjunct {@link #constantTestedAgainst} accepted. */
    private static Var singleVariable(Expr e) {
        ExprFunction2 test = (ExprFunction2)e;
        return test.getArg1().isVariable() ? test.getArg1().asVar() : test.getArg2().asVar();
    }

    private static boolean provablyDistinctValues(NodeValue nv1, NodeValue nv2) {
        try {
            return NodeValue.notSameValueAs(nv1, nv2);
        } catch (ExprEvalException ex) {
            // Indeterminate comparison: can not prove the disjuncts mutually exclusive.
            return false;
        }
    }

    /** Explode an expr into a list of disjunctions */
    private static List<Expr> explodeDisjunction(List<Expr> exprList, Expr expr) {
        if ( !(expr instanceof E_LogicalOr) ) {
            exprList.add(expr);
            return exprList;
        }

        E_LogicalOr exprOr = (E_LogicalOr)expr;
        Expr e1 = exprOr.getArg1();
        Expr e2 = exprOr.getArg2();
        explodeDisjunction(exprList, e1);
        explodeDisjunction(exprList, e2);
        return exprList;
    }
}
