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

package org.apache.jena.tdb2.solver.skipscan;

import java.util.List;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitorByTypeBase;
import org.apache.jena.sparql.algebra.OpWalker;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpDistinct;
import org.apache.jena.sparql.algebra.op.OpExt;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpGroup;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.join.ImmutableUniqueList;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.aggregate.AggAvgDistinct;
import org.apache.jena.sparql.expr.aggregate.AggCountVarDistinct;
import org.apache.jena.sparql.expr.aggregate.AggGroupConcatDistinct;
import org.apache.jena.sparql.expr.aggregate.AggMax;
import org.apache.jena.sparql.expr.aggregate.AggMedianDistinct;
import org.apache.jena.sparql.expr.aggregate.AggMin;
import org.apache.jena.sparql.expr.aggregate.AggModeDistinct;
import org.apache.jena.sparql.expr.aggregate.AggSampleDistinct;
import org.apache.jena.sparql.expr.aggregate.AggSumDistinct;
import org.apache.jena.sparql.expr.aggregate.Aggregator;
import org.apache.jena.sparql.expr.aggregate.AggregatorFactory;

public class SkipScanRewrite {

    /**
     * Returns true iff the argument matches the structure below.
     * Brackets indicate optional operation types.
     *
     * <pre>{@code
     * OpDistinct
     *   OpProject
     *     [OpFilter]
     *       [OpExtend]
     *         OpBGP|OpQuadPattern with a single triple/quad
     * }</pre>
     */
    public static boolean isSkipScanCandidate(Op inputOp) {
        boolean result = false;

        Op op = inputOp;
        if (op instanceof OpDistinct opD) {
            op = opD.getSubOp();
        }

        if (op instanceof OpProject opP) {
            op = opP.getSubOp();
        }

        if (op instanceof OpFilter opF) {
            op = opF.getSubOp();
        }

        if (op instanceof OpExtend opE) {
            op = opE.getSubOp();
        }

        BasicPattern bp = null;
        if (op instanceof OpQuadPattern opQuadPattern) {
            bp = opQuadPattern.getBasicPattern();
        } else if (op instanceof OpBGP opBgp) {
            bp = opBgp.getPattern();
        }

        if (bp != null && bp.size() == 1) {
            result = true;
        }
        return result;
    }

    /**
     * Attempt to rewrite an OpDistinct(...) as a skip scan.
     */
    public static Op tryRewriteAsSkipScan(OpDistinct op) {
        if (isSkipScanCandidate(op)) {
            Op op1 = Transformer.transform(new TransformDistinctPlacement(), op);

            TransformSkipScan xform = new TransformSkipScan();
            Op op2 = Transformer.transform(xform, op1);

            // Only return the transformation result if a skip scan was injected.
            if (xform.getNumAppliedSkipScanTransforms() > 0) {
                return op2;
            }
        }
        return null;
    }

    public static Op tryRewriteAsSkipScan(OpGroup opGroup) {
        if (opGroup.getGroupVars().getExprs().isEmpty() && opGroup.getAggregators().size() == 1) {
            // We come here iff there is only grouping by simple variables (no expressions)
            // and a single aggregator.
            Var v = suitableVarOrNull(opGroup.getAggregators().getFirst().getAggregator());
            if (v != null) {
                ImmutableUniqueList<Var> newProj = ImmutableUniqueList.<Var>newUniqueListBuilder()
                    .addAll(opGroup.getGroupVars().getVars())
                    .add(v)
                    .build();
                OpDistinct newOp = new OpDistinct(new OpProject(opGroup.getSubOp(), newProj));

                Op patternOp = SkipScanRewrite.tryRewriteAsSkipScan(newOp);
                if (patternOp != null) {
                    // Remove redundant distinct from aggregators - it is ensured by the pattern execution.
                    List<ExprAggregator> newAggs = List.of(convertToNonDistinct(opGroup.getAggregators().getFirst()));
                    patternOp = new OpGroup(patternOp, opGroup.getGroupVars(), newAggs);
                    return patternOp;
                }
            }
        }
        return null;
    }

    /**
     * If the aggregator is suitable for skip-scan execution then return its single
     * value variable, else null.
     *
     * <p>An aggregator is suitable iff its result is invariant under removing duplicate
     * values of a single value expression that is a plain variable. This covers every
     * DISTINCT single-var aggregator (COUNT, SUM, AVG, MEDIAN, MODE, SAMPLE, GROUP_CONCAT)
     * plus the non-distinct MIN, MAX and SAMPLE aggregators (for which DISTINCT is
     * irrelevant).</p>
     */
    private static Var suitableVarOrNull(Aggregator agg) {
        if (!isSkipScanSuitable(agg)) {
            return null;
        }
        return varOrNull(agg);
    }

    private static boolean isSkipScanSuitable(Aggregator agg) {
        return agg instanceof AggCountVarDistinct
            || agg instanceof AggSumDistinct
            || agg instanceof AggAvgDistinct
            || agg instanceof AggMedianDistinct
            || agg instanceof AggModeDistinct
            || agg instanceof AggSampleDistinct
            || agg instanceof AggGroupConcatDistinct
            // Non-distinct MIN/MAX are distinct-invariant.
            || agg instanceof AggMin
            || agg instanceof AggMax;
            // For sample retain the original distribution - so exclude.
            // || agg instanceof AggSample
    }

    private static ExprAggregator convertToNonDistinct(ExprAggregator eAgg) {
        Aggregator newAgg = convertToNonDistinct(eAgg.getAggregator());
        return new ExprAggregator(eAgg.getVar(), newAgg);
    }

    /**
     * Convert a (possibly DISTINCT) skip-scan-suitable aggregator into its non-distinct
     * equivalent. Because the skip-scan input yields each value at most once per group,
     * the non-distinct aggregator computes the same result. MIN/MAX/SAMPLE (already
     * distinct-invariant) are returned unchanged.
     */
    private static Aggregator convertToNonDistinct(Aggregator agg) {
        Expr expr = agg.getExprList().get(0);
        if (agg instanceof AggCountVarDistinct) {
            return AggregatorFactory.createCountExpr(false, expr);
        } else if (agg instanceof AggSumDistinct) {
            return AggregatorFactory.createSum(false, expr);
        } else if (agg instanceof AggAvgDistinct) {
            return AggregatorFactory.createAvg(false, expr);
        } else if (agg instanceof AggMedianDistinct) {
            return AggregatorFactory.createMedian(false, expr);
        } else if (agg instanceof AggModeDistinct) {
            return AggregatorFactory.createMode(false, expr);
        } else if (agg instanceof AggSampleDistinct) {
            return AggregatorFactory.createSample(false, expr);
        } else if (agg instanceof AggGroupConcatDistinct acd) {
            return AggregatorFactory.createGroupConcat(false, expr, acd.getSeparator(), null);
        }
        // AggMin, AggMax, AggSample: already non-distinct and distinct-invariant.
        return agg;
    }

    /** Extract the first argument of the agg's exprList as a Var, or null. */
    private static Var varOrNull(Aggregator agg) {
        ExprList el = agg.getExprList();
        if (el == null || el.size() != 1) {
            return null;
        }
        Expr e = el.get(0);
        return e.isVariable() ? e.asVar() : null;
    }

    private static class AbortEarlyException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        @Override public synchronized Throwable fillInStackTrace() { return this; }
    }

    public static boolean usesSkipScan(Op op) {
        boolean[] result = {false};
        try {
            OpWalker.walk(op, new OpVisitorByTypeBase() {
                @Override
                public void visit(OpExt opExt) {
                    if (opExt instanceof OpExtSkipScan) {
                        result[0] = true;
                        throw new AbortEarlyException();
                    }
                }
            });
        } catch (AbortEarlyException e) {}
        return result[0];
    }
}

