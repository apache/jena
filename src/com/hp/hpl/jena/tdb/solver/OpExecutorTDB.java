/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.solver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import atlas.lib.InternalErrorException;
import atlas.logging.Log;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.ARQInternalErrorException;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern;
import com.hp.hpl.jena.sparql.algebra.opt.TransformFilterPlacement;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.Substitute;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPeek;
import com.hp.hpl.jena.sparql.engine.main.OpExecutor;
import com.hp.hpl.jena.sparql.engine.main.OpExecutorFactory;
import com.hp.hpl.jena.sparql.engine.main.QC;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderProc;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderTransformation;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB;
import com.hp.hpl.jena.tdb.store.GraphTDB;

/** TDB executor for algebra expressions.  It is the standard ARQ executor
 *  except for basic graph patterns and filtered basic graph patterns (currently).  
 * 
 * See also: StageGeneratorDirectTDB, a non-reordering 
 * 
 * @author Andy Seaborne
 */
public class OpExecutorTDB extends OpExecutor
{
    private static final Logger log = LoggerFactory.getLogger(OpExecutorTDB.class) ;
    
    public final static OpExecutorFactory OpExecFactoryTDB = new OpExecutorFactory()
    {
        @Override
        public OpExecutor create(ExecutionContext execCxt)
        { return new OpExecutorTDB(execCxt) ; }
    } ;
    
    private final boolean isForTDB ;
    private final boolean isUnionDefaultGraph ;
    
    // A new compile object is created for each op compilation.
    // So the execCxt is changing as we go through the query-compile-execute process  
    public OpExecutorTDB(ExecutionContext execCxt)
    {
        super(execCxt) ;
        // NB. The datset may be a TDB one, or a general one.
        // Merged union graph magic is only for a TDB daatset.
        
        isForTDB = (execCxt.getActiveGraph() instanceof GraphTDB) ;
        
        isUnionDefaultGraph = 
            execCxt.getContext().isTrue(TDB.symUnionDefaultGraph) && 
            execCxt.getDataset() instanceof DatasetGraphTDB ;
    }

    @Override
    protected QueryIterator execute(OpBGP opBGP, QueryIterator input)
    {
        if ( ! isForTDB )
            return super.execute(opBGP, input) ;
        
        GraphTDB graph = (GraphTDB)execCxt.getActiveGraph() ;
        
        if ( isUnionDefaultGraph )
            return execute(new OpQuadPattern(Quad.unionGraph, opBGP.getPattern()), input) ;
        
        
        if ( ! isDefaultGraphStorage(false,  graph.getGraphNode()))
        {
            // Global UnionGraph does not apply.
            // Execute on a API-obtained graph/model from a named graph in the dataset.
            // Need the real dataset.
            DatasetGraphTDB ds = graph.getDataset() ;
            // false - not implicitly trhe union of named models.
            return optimizeExecuteQuads(ds, input, false, graph.getGraphNode(), opBGP.getPattern(), null, execCxt) ;
        }
        
        // Execute a BGP on the real default graph
        return optimizeExecute(graph, input, opBGP.getPattern(), null, execCxt) ;
    }

    /** Execute, with optimization, the basic graph pattern */
    /*public*/ 
    private static QueryIterator optimizeExecute(GraphTDB graph, QueryIterator input,
                                                BasicPattern pattern, ExprList exprs,
                                                ExecutionContext execCxt)
    {
        return optimizeExecuteTriples(graph.getReorderTransform(), input, pattern, exprs, execCxt) ;
        
        // May have a name - explicitly named default graph
//        if ( graph.getGraphNode() == null )
//            return optimizeExecuteTriples(graph.getReorderTransform(), input, pattern, exprs, execCxt) ;
//        else
//            throw new InternalErrorException("optimizeExecute on a named graph: "+graph.getGraphNode()) ;
    }

    @Override
    protected QueryIterator execute(OpFilter opFilter, QueryIterator input)
    {
        if ( ! isForTDB )
            return super.execute(opFilter, input) ;

        if ( OpBGP.isBGP(opFilter.getSubOp()) )
        {
            OpBGP opBGP = (OpBGP)opFilter.getSubOp() ;
            GraphTDB graph = (GraphTDB)execCxt.getActiveGraph() ;
            return optimizeExecute(graph, input, opBGP.getPattern(), opFilter.getExprs(), execCxt) ;
        }
        
        // OpQuadPattern.isQuadPattern
        // For now, filter placement only works on the default graph so find that.
        if ( opFilter.getSubOp() instanceof OpQuadPattern )
        {
            DatasetGraphTDB ds = (DatasetGraphTDB)execCxt.getDataset() ;
            OpQuadPattern quadPattern = (OpQuadPattern)opFilter.getSubOp() ;
            if ( isDefaultGraphStorage(isUnionDefaultGraph, quadPattern.getGraphNode()) )
            {
                return optimizeExecute(ds.getDefaultGraphTDB(),
                                       input, quadPattern.getBasicPattern(), opFilter.getExprs(), execCxt) ;
            }
            // else drop through.
        }
        
//        // No filter placement for quads yet.
//        if ( opFilter.getSubOp() instanceof OpQuadPattern && ! isUnionDefaultGraph )
//        {
//            DatasetGraphTDB ds = (DatasetGraphTDB)execCxt.getDataset() ;
//            OpQuadPattern quadPattern = (OpQuadPattern)opFilter.getSubOp() ;
//            return optimizeExecute(ds, 
//                                   input, isUnionDefaultGraph, quadPattern.getGraphNode(),
//                                   quadPattern.getBasicPattern(), opFilter.getExprs(), execCxt) ;
//        }

        // (filter (sequence ...))
        
        // Can't do better at the moment.
        return super.execute(opFilter, input) ;
        }

    @Override
    protected QueryIterator execute(OpQuadPattern quadPattern, QueryIterator input)
    {
        if ( ! isForTDB )
            return super.execute(quadPattern, input) ;
        
//        // Dataset a TDB one?
//        // Presumably the quad transform has been applied.
//        if ( ! ( execCxt.getDataset() instanceof DatasetGraphTDB ) )
//            return super.execute(quadPattern, input) ;
        
        DatasetGraph dg = execCxt.getDataset() ;
        if ( ! ( dg instanceof DatasetGraphTDB ) )
            throw new InternalErrorException("Not a TDB backed dataset in quad pattern execution") ;
        
        DatasetGraphTDB ds = (DatasetGraphTDB)dg ;
        BasicPattern bgp = quadPattern.getBasicPattern() ;
        Node gn = quadPattern.getGraphNode() ;
        return optimizeExecuteQuads(ds, input, isUnionDefaultGraph, gn, bgp, null, execCxt) ;
    }
    
    /** Execute, with optimization, a quad pattern */
    
    private static QueryIterator optimizeExecuteQuads(DatasetGraphTDB ds, 
                                                      QueryIterator input, 
                                                      boolean isUnionDefaultGraph, 
                                                      Node gn, BasicPattern bgp,
                                                      ExprList exprs, ExecutionContext execCxt)
    {
        if ( ! input.hasNext() )
            return input ;
        
        // Graph names with special meaning:
        //   Quad.defaultGraphIRI -- the IRI used in GRAPH <> to mean the default graph.
        //   Quad.defaultGraphNodeGenerated -- the internal marker node used for the quad form of queries.
        //   Quad.unionGraph -- the IRI used in GRAPH <> to mean the union of named graphs
        // Also: isUnionDefaultGraph if implicit union of named graphs.
        
        if ( isDefaultGraphStorage(isUnionDefaultGraph, gn) ) 
        {
            // Storage concrete, default graph. Either outside GRAPH (no implicit union)
            // or using the "name" of the default graph
            return optimizeExecute(ds.getDefaultGraphTDB(), input, bgp, exprs, execCxt) ;
        }
        
        // ---- Union (RDF Merge) of named graphs
        boolean doingUnion = false ;
        
        if ( isUnionDefaultGraph && Quad.isQuadDefaultGraphNode(gn) ) 
            // Implicit: default graph is union of named graphs. 
            doingUnion = true ;
        
        if ( gn.equals(Quad.unionGraph) )
            // Explicit name of the union of named graphs
            doingUnion = true ;
        
        if ( doingUnion )
            // Set the "any" graph node.
            gn = Node.ANY ;
        
        // ----
        // TEMP: Filters were not considered.
        ReorderTransformation transform = ds.getTransform() ;
        
        if ( transform != null )
        {
            QueryIterPeek peek = QueryIterPeek.create(input, execCxt) ;
            input = peek ; // Original input now invalid.
            BasicPattern bgp2 = reorder(bgp, peek, transform) ;
            Explain.explain(bgp2, execCxt.getContext()) ;
            bgp = bgp2 ;
        }
        
        // -- Filter placement
        // Does not operate on an op at this point.
        // Needs rework.  Needs intergation with the bgp execution path.
        if ( exprs != null )
            log.warn("Expression for filters passed to quad optimize/execute") ;
        
//        Op op = null ;
//        if ( exprs != null )
//            op = TransformFilterPlacement.transform(exprs, bgp) ;
//        else
//            op = new OpBGP(bgp) ;
// May not be a BGP - now what?
        
        QueryIterator qIter = SolverLib.execute(ds, gn, bgp, input, execCxt) ;
        return qIter ;
    }
    
    private static QueryIterator optimizeExecuteTriples(ReorderTransformation transform, 
                                                        QueryIterator input, 
                                                        BasicPattern pattern, ExprList exprs,
                                                        ExecutionContext execCxt)
    {
        if ( ! input.hasNext() )
            return input ;

        // -- Input
        // Must pass this iterator into the next stage. 
        QueryIterPeek peek = QueryIterPeek.create(input, execCxt) ;
        input = null ; // and not this one which is now invalid.
        
        // -- Reorder
        pattern = reorder(pattern, peek, transform) ;
        
        // -- Filter placement
        Op op = null ;
        if ( exprs != null )
            op = TransformFilterPlacement.transform(exprs, pattern) ;
        else
            op = new OpBGP(pattern) ;
        
        // -- Explain
        Explain.explain(op, execCxt.getContext()) ;
        
        // -- Execute
        // Switch to a non-reordring executor
        // The Op may be a sequence due to TransformFilterPlacement
        // so we need to do a full execution step, not go straight to the SolverLib.
        
        ExecutionContext ec2 = new ExecutionContext(execCxt) ;
        ec2.setExecutor(plainFactory) ;

        // Solve without going through this executor again.
        // There would be issues of nested patterns but this is only a
        // (filter (bgp...)) or (filter (quadpattern ...))
        // so there are no nested patterns to reorder.
        return QC.execute(op, peek, ec2) ;
    }

    private static BasicPattern reorder(BasicPattern pattern, QueryIterPeek peek, ReorderTransformation transform)
    {
        if ( transform != null )
        {
            // This works by getting one result from the peek iterator,
            // and creating the more gounded BGP. The tranform is used to
            // determine the best order and the transformation is returned. This
            // transform is applied to the unsubstituted pattern (which will be
            // substituted as part of evaluation.
            
            if ( ! peek.hasNext() )
                throw new ARQInternalErrorException("Peek iterator is already empty") ;
 
            BasicPattern pattern2 = Substitute.substitute(pattern, peek.peek() ) ;
            // Calculate the reordering based on the substituted pattern.
            ReorderProc proc = transform.reorderIndexes(pattern2) ;
            // Then reorder original patten
            pattern = proc.reorder(pattern) ;
        }
        return pattern ;
    }
    
    // Is this a query against the real default graph in the storage (in a 3-tuple table). 
    private static boolean isDefaultGraphStorage(boolean isUnionDefaultGraph, Node gn/*, BasicPattern bgp*/)
    {
        if ( gn == null )
            return true ;
        
        // Graph names with special meaning:
        //   Quad.defaultGraphIRI -- the IRI used in GRAPH <> to mean the default graph.
        //   Quad.defaultGraphNodeGenerated -- the internal marker node used for the quad form of queries.
        //   Quad.unionGraph -- the IRI used in GRAPH <> to mean the union of named graphs
        //  

        // Not union and implicit name for default graph.
        if ( ! isUnionDefaultGraph && Quad.isQuadDefaultGraphNode(gn) )
            // Not accessing the union of named graphs as the default graph
            // and pattern is directed to the default graph.
            return true ;
    
        // Is it the explciitly named default graph?
        if ( gn.equals(Quad.defaultGraphIRI) )
            // Explicit GRAPH <urn:x-arq:DefaultGraph> {}
            return true ;
        
        return false ;
    }

    // ---- OpExecute factories and plain executor.
    
    private static OpExecutorFactory plainFactory = new OpExecutorPlainFactoryTDB() ;
    private static class OpExecutorPlainFactoryTDB implements OpExecutorFactory
    {
        @Override
        public OpExecutor create(ExecutionContext execCxt)
        {
            return new OpExecutorPlainTDB(execCxt) ;
        }
    }
    
    /** An op executor that simply executes a BGP or QuadPattern without any reordering */ 
    private static class OpExecutorPlainTDB extends OpExecutor
    {
        public OpExecutorPlainTDB(ExecutionContext execCxt)
        {
            super(execCxt) ;
        }
        
        @Override
        public QueryIterator execute(OpBGP opBGP, QueryIterator input)
        {
            Graph g = execCxt.getActiveGraph() ;
            
            if ( g instanceof GraphTDB )
                // Triple-backed (but may have a named of explicit default graph).
                return SolverLib.execute((GraphTDB)g, opBGP.getPattern(), input, execCxt) ;
            Log.warn(this, "Non-GraphTDB passed to OpExecutorPlainTDB") ;
            return super.execute(opBGP, input) ;
        }
        
        @Override
        public QueryIterator execute(OpQuadPattern opQuadPattern, QueryIterator input)
        {
            if ( execCxt.getDataset() instanceof DatasetGraphTDB )
            {
                DatasetGraphTDB ds = (DatasetGraphTDB)execCxt.getDataset() ;
                BasicPattern bgp = opQuadPattern.getBasicPattern() ;
                Node gn = opQuadPattern.getGraphNode() ;
                return SolverLib.execute(ds, gn, bgp, input, execCxt) ;
            }
            Log.warn(this, "Non-DatasetGraphTDB passed to OpExecutorPlainTDB") ;
            return super.execute(opQuadPattern, input) ;
        }

    }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */