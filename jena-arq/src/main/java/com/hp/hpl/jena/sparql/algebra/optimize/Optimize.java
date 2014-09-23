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

import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.sparql.ARQConstants ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.OpWalker ;
import com.hp.hpl.jena.sparql.algebra.Transform ;
import com.hp.hpl.jena.sparql.algebra.TransformCopy;
import com.hp.hpl.jena.sparql.algebra.Transformer ;
import com.hp.hpl.jena.sparql.algebra.op.OpLabel ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.sparql.util.Symbol ;


public class Optimize implements Rewrite
{
    static private Logger log = LoggerFactory.getLogger(Optimize.class) ;

    // A small (one slot) registry to allow plugging in an alternative optimizer
    public interface RewriterFactory { Rewrite create(Context context) ; }
    
    // ----    
    public static RewriterFactory noOptimizationFactory = new RewriterFactory()
    {
        @Override
        public Rewrite create(Context context)
        {
            return new Rewrite() {

                @Override
                public Op rewrite(Op op)
                {
                    return op ;
                }} ;
        }} ;
        
    public static RewriterFactory stdOptimizationFactory = new RewriterFactory()
    {
        @Override
        public Rewrite create(Context context)
        {
            return new Optimize(context) ;
        }
    } ;
    
    // Set this to a different factory implementation to have a different general optimizer.  
    private static RewriterFactory factory = stdOptimizationFactory ;
    
    // ----        
        
    public static Op optimize(Op op, ExecutionContext execCxt)
    {
        return optimize(op, execCxt.getContext()) ;
    }

    // The execution-independent optimizations
    public static Op optimize(Op op, Context context)
    {
        Rewrite opt = decideOptimizer(context) ;
        return opt.rewrite(op) ;
    }

    /** Set the global optimizer factory to one that does nothing */
    public static void noOptimizer()
    {
        setFactory(noOptimizationFactory) ;
    }

    static private Rewrite decideOptimizer(Context context)
    {
        RewriterFactory f = (RewriterFactory)context.get(ARQConstants.sysOptimizerFactory) ;
        if ( f == null )
            f = factory ;
        if ( f == null )
            f = stdOptimizationFactory ;    // Only if default 'factory' gets lost.
        return f.create(context) ;
    }

    
    /** Globably set the factory for making optimizers */ 
    public static void setFactory(RewriterFactory aFactory)
    { factory = aFactory ; }

    /** Get the global factory for making optimizers */ 
    public static RewriterFactory getFactory()
    { return factory ; }
    
    // ---- The object proper for the standard optimizations
    
    private final Context context ;
    private Optimize(ExecutionContext execCxt)
    {
        this(execCxt.getContext()) ;
    }
    
    private Optimize(Context context)
    {
        this.context = context ;
    }

    /** Alternative name for compatibility only */
    public static final Symbol filterPlacementOldName = ARQConstants.allocSymbol("filterPlacement") ;
    
    @Override
    public Op rewrite(Op op)
    {
        // Record optimizer
        if ( context.get(ARQConstants.sysOptimizer) == null )
            context.set(ARQConstants.sysOptimizer, this) ;
        
        // Old name, new name fixup.
        if ( context.isDefined(filterPlacementOldName) ) 
        {
            if ( context.isUndef(ARQ.optFilterPlacement) )
                context.set(ARQ.optFilterPlacement, context.get(filterPlacementOldName)) ;
        }
        
        if ( false )
        {
            // Removal of "group of one" join (AKA SPARQL "simplification") 
            // is done during algebra generation in AlgebraGenerator
            op = apply("Simplify", new TransformSimplify(), op) ;
            op = apply("Delabel", new TransformRemoveLabels(), op) ;
        }

        // ** TransformScopeRename::
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

        // Expression constant folding
        if ( context.isTrueOrUndef(ARQ.optExprConstantFolding) )
            op = Transformer.transform(new TransformCopy(), new ExprTransformConstantFold(), op);
        
        // Need to allow subsystems to play with this list.
        
        if ( context.isTrueOrUndef(ARQ.propertyFunctions) )
            op = apply("Property Functions", new TransformPropertyFunction(context), op) ;

        if ( context.isTrueOrUndef(ARQ.optFilterConjunction) )
            op = apply("filter conjunctions to ExprLists", new TransformFilterConjunction(), op) ;

        if ( context.isTrueOrUndef(ARQ.optFilterExpandOneOf) )
            op = apply("Break up IN and NOT IN", new TransformExpandOneOf(), op) ;

        // Apply some general purpose filter transformations
                
        if ( context.isTrueOrUndef(ARQ.optFilterImplicitJoin) )
            op = apply("Filter Implicit Join", new TransformFilterImplicitJoin(), op);
        
        if ( context.isTrueOrUndef(ARQ.optImplicitLeftJoin) )
            op = apply("Implicit Left Join", new TransformImplicitLeftJoin(), op);
                
        if ( context.isTrueOrUndef(ARQ.optFilterDisjunction) )
            op = apply("Filter Disjunction", new TransformFilterDisjunction(), op) ;
        
        // Some ORDER BY-LIMIT N queries can be done more efficiently by only recording
        // the top N items, so a full sort is not needed.
        if ( context.isTrueOrUndef(ARQ.optTopNSorting) )
            op = apply("TopN Sorting", new TransformTopN(), op) ;
        
        // ORDER BY+DISTINCT optimizations
        // We apply the one that changes evaluation order first since when it does apply it will give much
        // better performance than just transforming DISTINCT to REDUCED
        
        if ( context.isTrueOrUndef(ARQ.optOrderByDistinctApplication) )
            op = apply("Apply DISTINCT prior to ORDER BY where possible", new TransformOrderByDistinctApplication(), op);

        // Transform some DISTINCT to REDUCED, slightly more liberal transform that ORDER BY+DISTINCT application
        // Reduces memory consumption.
        if ( context.isTrueOrUndef(ARQ.optDistinctToReduced) )
            op = apply("Distinct replaced with reduced", new TransformDistinctToReduced(), op) ;
        
        // Find joins/leftJoin that can be done by index joins (generally preferred as fixed memory overhead).
        if ( context.isTrueOrUndef(ARQ.optIndexJoinStrategy) )
            op = apply("Index Join strategy", new TransformJoinStrategy(), op) ;
        
        // Place filters close to where their dependency variables are defined.
        // This prunes the output of that step as early as possible.
        // If done before TransformJoinStrategy, you can get two applications
        // of a filter in a (sequence) from each half of a (join).  This is harmless,
        // because filters are generally cheap, but it looks a bit bad.
        if ( context.isTrueOrUndef(ARQ.optFilterPlacement) ) {
            if ( context.isTrue(ARQ.optFilterPlacementConservative))
                op = apply("Filter Placement (conservative)", new TransformFilterPlacementConservative(), op) ;
            else { 
                // Whether to push into BGPs 
                boolean b = context.isTrueOrUndef(ARQ.optFilterPlacementBGP) ;
                op = apply("Filter Placement", new TransformFilterPlacement(b), op) ;
            }
        }
        
        // Replace suitable FILTER(?x = TERM) with (assign) and write the TERm for ?x in the pattern.    
        // Apply (possible a second time) after FILTER placement as it can create new possibilities.
        // See JENA-616.
        if ( context.isTrueOrUndef(ARQ.optFilterEquality) )
            op = apply("Filter Equality", new TransformFilterEquality(), op) ;
                
        // Replace suitable FILTER(?x != TERM) with (minus (original) (table)) where the table contains
        // the candidate rows to be eliminated
        // Off by default due to minimal performance difference
        if ( context.isTrue(ARQ.optFilterInequality) )
            op = apply("Filter Inequality", new TransformFilterInequality(), op);
        
        // Promote table empty as late as possible since this will only be produced by other 
        // optimizations and never directly from algebra generation
        if ( context.isTrueOrUndef(ARQ.optPromoteTableEmpty) )
            op = apply("Table Empty Promotion", new TransformPromoteTableEmpty(), op) ;

        // Merge adjacent BGPs
        if ( context.isTrueOrUndef(ARQ.optMergeBGPs) )
            op = apply("Merge BGPs", new TransformMergeBGPs(), op) ;
        
        // Merge (extend) and (assign) stacks
        if ( context.isTrueOrUndef(ARQ.optMergeExtends) )
            op = apply("Merge BGPs", new TransformExtendCombine(), op) ;
        
        // Mark
        if ( false )
            op = OpLabel.create("Transformed", op) ;
        return op ;
    }
    
    public static Op apply(Transform transform, Op op)
    {
        Op op2 = Transformer.transformSkipService(transform, op) ;
        if ( op2 != op )
            return op2 ; 
        return op ;
    }
    
    public static Op apply(String label, Transform transform, Op op)
    {
        // Use this to apply inside NOT EXISTS and EXISTS 
        // Transform transform2 = new TransformApplyInsideExprFunctionOp(transform) ;
        // Remember there is an outer substitue to the NOT EXISTS operation. 

        //Transform transform2 = new TransformApplyInsideExprFunctionOp(transform) ;
        
        Op op2 = Transformer.transformSkipService(transform, op) ;
        
        final boolean debug = false ;
        
        if ( debug )
        {
            if ( label != null && log.isInfoEnabled() )
                    log.info("Transform: "+label) ;
            if ( op == op2 ) 
            {
                if ( log.isInfoEnabled() ) 
                    log.info("No change (==)") ;
                return op2 ;
            }

            if ( op.equals(op2) ) 
            {
                if ( log.isInfoEnabled() )
                    log.info("No change (equals)") ;
                return op2 ;
            }
            if ( log.isInfoEnabled() )
            {
                log.info("\n"+op.toString()) ;
                log.info("\n"+op2.toString()) ;
            }
        }
        if ( op2 != op )
            return op2 ; 
        return op ;
    }
}
