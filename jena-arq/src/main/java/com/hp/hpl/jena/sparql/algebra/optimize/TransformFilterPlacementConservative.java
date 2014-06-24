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

/* Contains code submitted in email to jena-user@incubator.apache.org 
 * so software grant and relicensed under the Apache Software License. 
 *    transformFilterConditional
 */

package com.hp.hpl.jena.sparql.algebra.optimize;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVars;
import com.hp.hpl.jena.sparql.algebra.TransformCopy;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpConditional;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern;
import com.hp.hpl.jena.sparql.algebra.op.OpSequence;
import com.hp.hpl.jena.sparql.algebra.op.OpTable;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.util.VarUtils;

/**
 * Rewrite an algebra expression to put filters as close to their bound
 * variables in a BGP. Works on (filter (BGP ...) )
 * <p>
 * This is a conservative and relatively limited optimization and has been
 * superseded in the default optimizer by the {@link TransformFilterPlacement}
 * as of the 2.11.x releases.  This original version of TransformFilterPlacement
 * only operates on filters over BGPs, quad blocks, sequences and conditions
 * (a form of LeftJoin with no scope issues) of the same.
 * However in some cases it may be desirable to have
 * the more limited and conservative behaviour so this is preserved in the code
 * for those who want to use this.
 * </p>
 * <p>
 * The context flag {@link ARQ#optFilterPlacementConservative} may be set to
 * have the default optimizer use this in place of the newer and more aggressive
 * {@link TransformFilterPlacement}
 * </p>
 */

public class TransformFilterPlacementConservative extends TransformCopy {
    
    public TransformFilterPlacementConservative( ) {}

    @Override
    public Op transform(OpFilter opFilter, Op x) {
        // Destructive use of exprs - copy it.
        ExprList exprs = ExprList.copy(opFilter.getExprs());
        Set<Var> varsScope = new HashSet<>();

        Op op = transform(exprs, varsScope, x);
        if (op == x)
            // Didn't do anything.
            return super.transform(opFilter, x);

        // Remaining exprs
        op = buildFilter(exprs, op);
        return op;
    }

    private static Op transform(ExprList exprs, Set<Var> varsScope, Op x) {
        // OpAssign/OpExtend could be done if the assignment and exprs are
        // independent.
        // Dispatch by visitor??
        if (x instanceof OpBGP)
            return transformFilterBGP(exprs, varsScope, (OpBGP) x);

        if (x instanceof OpSequence)
            return transformFilterSequence(exprs, varsScope, (OpSequence) x);

        if (x instanceof OpQuadPattern)
            return transformFilterQuadPattern(exprs, varsScope, (OpQuadPattern) x);

        if (x instanceof OpSequence)
            return transformFilterSequence(exprs, varsScope, (OpSequence) x);

        if (x instanceof OpConditional)
            return transformFilterConditional(exprs, varsScope, (OpConditional) x);

        // Not special - advance the variable scope tracking.
        OpVars.visibleVars(x, varsScope);
        return x;
    }

    // == The transformFilter* modify the exprs and patternVarsScope arguments

    private static Op transformFilterBGP(ExprList exprs, Set<Var> patternVarsScope, OpBGP x) {
        return transformFilterBGP(exprs, patternVarsScope, x.getPattern());
    }

    // Mutates exprs
    private static Op transformFilterBGP(ExprList exprs, Set<Var> patternVarsScope, BasicPattern pattern) {
        // Any filters that depend on no variables.
        Op op = insertAnyFilter(exprs, patternVarsScope, null);

        for (Triple triple : pattern) {
            OpBGP opBGP = getBGP(op);
            if (opBGP == null) {
                // Last thing was not a BGP (so it likely to be a filter)
                // Need to pass the results from that into the next triple.
                // Which is a join and sequence is a special case of join
                // which always evaluates by passing results of the early
                // part into the next element of the sequence.

                opBGP = new OpBGP();
                op = OpSequence.create(op, opBGP);
            }

            opBGP.getPattern().add(triple);
            // Update variables in scope.
            VarUtils.addVarsFromTriple(patternVarsScope, triple);

            // Attempt to place any filters
            op = insertAnyFilter(exprs, patternVarsScope, op);
        }
        // Leave any remaining filter expressions - don't wrap up any as
        // something else may take them.
        return op;
    }

    /** Find the current OpBGP, or return null. */
    private static OpBGP getBGP(Op op) {
        if (op instanceof OpBGP)
            return (OpBGP) op;

        if (op instanceof OpSequence) {
            // Is last in OpSequence an BGP?
            OpSequence opSeq = (OpSequence) op;
            List<Op> x = opSeq.getElements();
            if (x.size() > 0) {
                Op opTop = x.get(x.size() - 1);
                if (opTop instanceof OpBGP)
                    return (OpBGP) opTop;
                // Drop through
            }
        }
        // Can't find.
        return null;
    }

    private static Op transformFilterQuadPattern(ExprList exprs, Set<Var> patternVarsScope, OpQuadPattern pattern) {
        return transformFilterQuadPattern(exprs, patternVarsScope, pattern.getGraphNode(), pattern.getBasicPattern());
    }

    private static Op transformFilterQuadPattern(ExprList exprs, Set<Var> patternVarsScope, Node graphNode, BasicPattern pattern) {
        // Any filters that depend on no variables.
        Op op = insertAnyFilter(exprs, patternVarsScope, null);
        if (Var.isVar(graphNode)) {
            // Add in the graph node of the quad block.
            // It's picked up after the first triple is processed.
            VarUtils.addVar(patternVarsScope, Var.alloc(graphNode));
        }

        for (Triple triple : pattern) {
            OpQuadPattern opQuad = getQuads(op);
            if (opQuad == null) {
                opQuad = new OpQuadPattern(graphNode, new BasicPattern());
                op = OpSequence.create(op, opQuad);
            }

            opQuad.getBasicPattern().add(triple);
            // Update variables in scope.
            VarUtils.addVarsFromTriple(patternVarsScope, triple);

            // Attempt to place any filters
            op = insertAnyFilter(exprs, patternVarsScope, op);
        }

        return op;
    }

    /** Find the current OpQuadPattern, or return null. */
    private static OpQuadPattern getQuads(Op op) {
        if (op instanceof OpQuadPattern)
            return (OpQuadPattern) op;

        if (op instanceof OpSequence) {
            // Is last in OpSequence an BGP?
            OpSequence opSeq = (OpSequence) op;
            List<Op> x = opSeq.getElements();
            if (x.size() > 0) {
                Op opTop = x.get(x.size() - 1);
                if (opTop instanceof OpQuadPattern)
                    return (OpQuadPattern) opTop;
                // Drop through
            }
        }
        // Can't find.
        return null;
    }

    private static Op transformFilterSequence(ExprList exprs, Set<Var> varScope, OpSequence opSequence) {
        List<Op> ops = opSequence.getElements();

        // Any filters that depend on no variables.
        Op op = insertAnyFilter(exprs, varScope, null);

        for ( Op seqElt : ops )
        {
            // Process the sequence element. This may insert filters (sequence
            // or BGP)
            seqElt = transform( exprs, varScope, seqElt );
            // Merge into sequence.
            op = OpSequence.create( op, seqElt );
            // Place any filters now ready.
            op = insertAnyFilter( exprs, varScope, op );
        }
        return op;
    }

    // Modularize.
    private static Op transformFilterConditional(ExprList exprs, Set<Var> varScope, OpConditional opConditional) {
        // Any filters that depend on no variables.
        Op op = insertAnyFilter(exprs, varScope, null);
        Op left = opConditional.getLeft();
        left = transform(exprs, varScope, left);
        Op right = opConditional.getRight();
        op = new OpConditional(left, right);
        op = insertAnyFilter(exprs, varScope, op);
        return op;
    }

    // ---- Utilities

    /** For any expression now in scope, wrap the op with a filter */
    private static Op insertAnyFilter(ExprList exprs, Set<Var> patternVarsScope, Op op) {
        for (Iterator<Expr> iter = exprs.iterator(); iter.hasNext();) {
            Expr expr = iter.next();
            // Cache
            Set<Var> exprVars = expr.getVarsMentioned();
            if (patternVarsScope.containsAll(exprVars)) {
                if (op == null)
                    op = OpTable.unit();
                op = OpFilter.filter(expr, op);
                iter.remove();
            }
        }
        return op;
    }

    /** Place expressions around an Op */
    private static Op buildFilter(ExprList exprs, Op op) {
        if (exprs.isEmpty())
            return op;

        for (Iterator<Expr> iter = exprs.iterator(); iter.hasNext();) {
            Expr expr = iter.next();
            if (op == null)
                op = OpTable.unit();
            op = OpFilter.filter(expr, op);
            iter.remove();
        }
        return op;
    }
}
