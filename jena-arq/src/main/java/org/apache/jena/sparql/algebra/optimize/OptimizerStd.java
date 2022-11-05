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

import org.apache.jena.query.ARQ ;
import org.apache.jena.sparql.ARQConstants ;
import org.apache.jena.sparql.SystemARQ ;
import org.apache.jena.sparql.algebra.* ;
import org.apache.jena.sparql.algebra.op.OpLabel ;
import org.apache.jena.sparql.util.Context ;
import org.apache.jena.sparql.util.Symbol ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/** The standard optimization sequence. */
public class OptimizerStd implements Rewrite
{
    static private Logger log = LoggerFactory.getLogger(Optimize.class) ;
    private final Context context ;

    public OptimizerStd(Context context) {
        this.context = context ;
    }

    /** Alternative name for compatibility only */
    public static final Symbol filterPlacementOldName = SystemARQ.allocSymbol("filterPlacement") ;

    @Override
    public Op rewrite(Op op) {
        // Record optimizer
        if ( context.get(ARQConstants.sysOptimizer) == null )
            context.set(ARQConstants.sysOptimizer, this) ;

        // Old name, new name fixup.
        if ( context.isDefined(filterPlacementOldName) ) {
            if ( context.isUndef(ARQ.optFilterPlacement) )
                context.set(ARQ.optFilterPlacement, context.get(filterPlacementOldName)) ;
        }

        if ( false ) {
            // Removal of "group of one" join (AKA SPARQL "simplification")
            // is done during algebra generation in AlgebraGenerator
            op = apply("Simplify", new TransformSimplify(), op) ;
            op = apply("Delabel", new TransformRemoveLabels(), op) ;
        }

        // ** TransformScopeRename
        // This is a requirement for the linearization execution that the default
        // ARQ query engine uses where possible.
        // This transformation must be done (e.g. by QueryEngineBase) if no other optimization is done.
        op = TransformScopeRename.transform(op) ;

        // Prepare expressions.
        OpWalker.walk(op, new OpVisitorExprPrepare(context)) ;

        // Convert paths to triple patterns if possible.
        if ( context.isTrueOrUndef(ARQ.optPathFlatten) ) {
            op = apply("Path flattening", new TransformPathFlattern(), op) ;
            // and merge adjacent BGPs (part 1)
            if ( context.isTrueOrUndef(ARQ.optMergeBGPs) )
                op = apply("Merge BGPs", new TransformMergeBGPs(), op) ;
        }

        // Having done the required transforms, specifically TransformScopeRename,
        // do each optimization via an overrideable method. Subclassing can modify the
        // transform performed.

        // Expression constant folding
        if ( context.isTrueOrUndef(ARQ.optExprConstantFolding) )
            op = transformExprConstantFolding(op) ;

        if ( context.isTrueOrUndef(ARQ.propertyFunctions) )
            op = transformPropertyFunctions(op) ;

        // Expand (A&&B) to two filter (A), (B) so that they can be placed independently.
        if ( context.isTrueOrUndef(ARQ.optFilterConjunction) )
            op = transformFilterConjunction(op) ;

        // Expand IN and NOT IN which then allows other optimizations to be applied.
        if ( context.isTrueOrUndef(ARQ.optFilterExpandOneOf) )
            op = transformFilterExpandOneOf(op) ;

        // Eliminate/Inline assignments where possible
        // Do this before we do some of the filter transformation work as inlining assignments
        // may give us more flexibility in optimizing the resulting filters
        if ( context.isTrue(ARQ.optInlineAssignments) )
            op = transformInlineAssignments(op) ;

        // Apply some general purpose filter transformations
        if ( context.isTrueOrUndef(ARQ.optFilterImplicitJoin) )
            op = transformFilterImplicitJoin(op) ;

        if ( context.isTrueOrUndef(ARQ.optImplicitLeftJoin) )
            op = transformFilterImplicitLeftJoin(op) ;

        if ( context.isTrueOrUndef(ARQ.optFilterDisjunction) )
            op = transformFilterDisjunction(op) ;

        // Some ORDER BY-LIMIT N queries can be done more efficiently by only recording
        // the top N items, so a full sort is not needed.
        if ( context.isTrueOrUndef(ARQ.optTopNSorting) )
            op = transformTopNSorting(op) ;

        // ORDER BY+DISTINCT optimizations
        // We apply the one that changes evaluation order first since when it does apply it will give much
        // better performance than just transforming DISTINCT to REDUCED

        if ( context.isTrueOrUndef(ARQ.optOrderByDistinctApplication) )
            op = transformOrderByDistinctApplication(op) ;

        // Transform some DISTINCT to REDUCED, slightly more liberal transform that ORDER BY+DISTINCT application
        // Reduces memory consumption.
        if ( context.isTrueOrUndef(ARQ.optDistinctToReduced) )
            op = transformDistinctToReduced(op) ;

        // Find joins/leftJoin that can be done by index joins (generally preferred as fixed memory overhead).
        if ( context.isTrueOrUndef(ARQ.optIndexJoinStrategy) )
            op = transformJoinStrategy(op) ;

        // Do a basic reordering so that triples with more defined terms go first.
        // 2022-11: This does not take into account values flowed into the BGP
        // (OpSequence, Lateral joins, EXISTS) so the default is not enabled.
        // Use "ARQ.getContext.set(ARQ.optReorderBGP, true)" to enable.
        //if ( context.isTrueOrUndef(ARQ.optReorderBGP) )
        if ( context.isTrue(ARQ.optReorderBGP) )
            op = transformReorder(op) ;

        // Place filters close to where their input variables are defined.
        // This prunes the output of that step as early as possible.
        //
        // This is done after BGP reordering because inserting the filters breaks up BGPs,
        // and would make transformReorder complicated, and also because a two-term triple pattern
        // is (probably) more specific than many filters.
        //
        // Filters in involving equality are done separately.

        // If done before TransformJoinStrategy, you can get two applications
        // of a filter in a (sequence) from each half of a (join).
        // This is harmless but it looks a bit odd.
        if ( context.isTrueOrUndef(ARQ.optFilterPlacement) )
            op = transformFilterPlacement(op) ;

        // Replace suitable FILTER(?x = TERM) with (assign) and write the TERM for ?x in the pattern.
        // Apply (possible a second time) after FILTER placement as it can create new possibilities.
        // See JENA-616.
       if ( context.isTrueOrUndef(ARQ.optFilterEquality) )
           op = transformFilterEquality(op) ;

        // Replace suitable FILTER(?x != TERM) with (minus (original) (table)) where the table contains
        // the candidate rows to be eliminated
        // Off by default due to minimal performance difference
        if ( context.isTrue(ARQ.optFilterInequality) )
            op = transformFilterInequality(op) ;

        // Promote table empty as late as possible since this will only be produced by other
        // optimizations and never directly from algebra generation
        if ( context.isTrueOrUndef(ARQ.optPromoteTableEmpty) )
            op = transformPromoteTableEmpty(op) ;

        // Merge adjacent BGPs
        if ( context.isTrueOrUndef(ARQ.optMergeBGPs) )
            op = transformMergeBGPs(op) ;

        // Merge (extend) and (assign) stacks
        if ( context.isTrueOrUndef(ARQ.optMergeExtends) )
            op = transformExtendCombine(op) ;

        // Mark
        if ( false )
            op = OpLabel.create("Transformed", op) ;
        return op ;
    }

    protected Op transformExprConstantFolding(Op op) {
        return Transformer.transform(new TransformCopy(), new ExprTransformConstantFold(), op);
    }

    protected Op transformPropertyFunctions(Op op) {
        return apply("Property Functions", new TransformPropertyFunction(context), op) ;
    }

    protected Op transformFilterConjunction(Op op) {
        return apply("filter conjunctions to ExprLists", new TransformFilterConjunction(), op) ;
    }
    protected Op transformFilterExpandOneOf(Op op) {
        return apply("Break up IN and NOT IN", new TransformExpandOneOf(), op) ;
    }

    protected Op transformInlineAssignments(Op op) {
        return TransformEliminateAssignments.eliminate(op, context.isTrue(ARQ.optInlineAssignmentsAggressive));
    }

    protected Op transformFilterImplicitJoin(Op op) {
        return apply("Filter Implicit Join", new TransformFilterImplicitJoin(), op);
    }

    protected Op transformFilterImplicitLeftJoin(Op op) {
        return apply("Implicit Left Join", new TransformImplicitLeftJoin(), op);
    }

    protected Op transformFilterDisjunction(Op op) {
        return apply("Filter Disjunction", new TransformFilterDisjunction(), op) ;
    }

    protected Op transformTopNSorting(Op op) {
        return apply("TopN Sorting", new TransformTopN(), op) ;
    }

    protected Op transformOrderByDistinctApplication(Op op) {
        return apply("Apply DISTINCT prior to ORDER BY where possible", new TransformOrderByDistinctApplication(), op);
    }

    protected Op transformDistinctToReduced(Op op) {
        return apply("Distinct replaced with reduced", new TransformDistinctToReduced(), op) ;
    }

    protected Op transformJoinStrategy(Op op) {
        return apply("Index Join strategy", new TransformJoinStrategy(), op) ;
    }

    protected Op transformFilterPlacement(Op op) {
        if ( context.isTrue(ARQ.optFilterPlacementConservative))
            op = apply("Filter Placement (conservative)", new TransformFilterPlacementConservative(), op) ;
        else {
            // Whether to push into BGPs
            boolean b = context.isTrueOrUndef(ARQ.optFilterPlacementBGP) ;
            op = apply("Filter Placement", new TransformFilterPlacement(b), op) ;
        }
        return op ;
    }

    protected Op transformFilterEquality(Op op) {
        return apply("Filter Equality", new TransformFilterEquality(), op) ;
    }

    protected Op transformFilterInequality(Op op) {
        return apply("Filter Inequality", new TransformFilterInequality(), op);
    }

    protected Op transformPromoteTableEmpty(Op op) {
        return apply("Table Empty Promotion", new TransformPromoteTableEmpty(), op) ;
    }

    protected Op transformMergeBGPs(Op op) {
        return apply("Merge BGPs", new TransformMergeBGPs(), op) ;
    }

    protected Op transformReorder(Op op) {
        return  apply("ReorderMerge BGPs", new TransformReorder(), op) ;
    }

    protected Op transformExtendCombine(Op op) {
        return apply("Combine BIND/LET", new TransformExtendCombine(), op) ;
    }

    public static Op apply(Transform transform, Op op) {
        Op op2 = Transformer.transformSkipService(transform, op) ;
        if ( op2 != op )
            return op2 ;
        return op ;
    }

    private static final boolean debug = false ;
    private static final boolean printNoAction = false;

    public static Op apply(String label, Transform transform, Op op) {
        Op op2 = Transformer.transformSkipService(transform, op) ;


        if ( debug ) {
            if ( printNoAction && label != null )
                log.info("Transform: " + label) ;
            if ( op == op2 ) {
                if ( printNoAction )
                    log.info("No change (==)") ;
                return op2 ;
            }
            if ( op.equals(op2) ) {
                if ( printNoAction )
                    log.info("No change (equals)") ;
                return op2 ;
            }

            if ( ! printNoAction && label != null );
                log.info("Transform: " + label) ;
            log.info("\n" + op.toString()) ;
            log.info("\n" + op2.toString()) ;
        }
        return op2 ;
    }
}
