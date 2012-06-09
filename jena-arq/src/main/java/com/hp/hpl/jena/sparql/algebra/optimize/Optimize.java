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

    
    /** Globably set the fcaory for making optimizers */ 
    public static void setFactory(RewriterFactory aFactory)
    { factory = aFactory ; }

    /** Get the global factory for making optimizers */ 
    public static RewriterFactory getFactory(RewriterFactory aFactory)
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
    
    @SuppressWarnings("all")
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
            // Simplify is always applied by the AlgebraGenerator
            op = apply("Simplify", new TransformSimplify(), op) ;
            op = apply("Delabel", new TransformRemoveLabels(), op) ;
        }

        // ** TransformScopeRename::
        // This is a requirement for the linearization execution that the default
        // ARQ query engine uses where possible.  
        // This transformation must be done (e.g. by QueryEngineBase) if no other optimziation is done. 
        op = TransformScopeRename.transform(op) ;
        
        // Remove "group of one" join
        // Done in AlgebraGenerator
        // e..g CONSTRUCT {} WHERE { SELECT ... } 
        //op = TransformTopLevelSelect.simplify(op) ;
        
        // Prepare expressions.
        OpWalker.walk(op, new OpVisitorExprPrepare(context)) ;
        
        // Need to allow subsystems to play with this list.
        
        if ( context.isTrueOrUndef(ARQ.propertyFunctions) )
            op = apply("Property Functions", new TransformPropertyFunction(context), op) ;

        if ( context.isTrueOrUndef(ARQ.optFilterConjunction) )
            op = apply("filter conjunctions to ExprLists", new TransformFilterConjunction(), op) ;

        if ( context.isTrueOrUndef(ARQ.optFilterExpandOneOf) )
            op = apply("Break up IN and NOT IN", new TransformExpandOneOf(), op) ;

        // TODO Improve filter placement to go through assigns that have no effect.
        // Either, do filter placement and other sequence generating transformations.
        // or improve to place in a sequence (latter is better?)
        
        if ( context.isTrueOrUndef(ARQ.optFilterEquality) )
        {
            boolean termStrings = context.isDefined(ARQ.optTermStrings) ;
            op = apply("Filter Equality", new TransformFilterEquality(!termStrings), op) ;
        }
        
        if ( context.isTrueOrUndef(ARQ.optFilterDisjunction) )
            op = apply("Filter Disjunction", new TransformFilterDisjunction(), op) ;
        
        if ( context.isTrueOrUndef(ARQ.optFilterPlacement) )
            // This can be done too early (breaks up BGPs).
            op = apply("Filter Placement", new TransformFilterPlacement(), op) ;
        
        if ( context.isTrueOrUndef(ARQ.optTopNSorting) )
        	op = apply("TopN Sorting", new TransformTopN(), op) ;

        if ( context.isTrueOrUndef(ARQ.optDistinctToReduced) )
            op = apply("Distinct replaced with reduced", new TransformDistinctToReduced(), op) ;
        
        // Convert paths to triple patterns. 
        // Also done in the AlgebraGenerator so this transform step catches programattically built op expressions 
        op = apply("Path flattening", new TransformPathFlattern(), op) ;
        
        // Find joins/leftJoin that can be done by index joins (generally preferred as fixed memory overhead).
        op = apply("Join strategy", new TransformJoinStrategy(), op) ;
        
        op = apply("Merge BGPs", new TransformMergeBGPs(), op) ;
        
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
