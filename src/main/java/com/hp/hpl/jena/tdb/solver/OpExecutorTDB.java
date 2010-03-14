/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.solver;

import org.openjena.atlas.iterator.Filter ;
import org.openjena.atlas.lib.Tuple ;
import org.openjena.atlas.logging.Log ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP ;
import com.hp.hpl.jena.sparql.algebra.op.OpDatasetNames ;
import com.hp.hpl.jena.sparql.algebra.op.OpDistinct ;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter ;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern ;
import com.hp.hpl.jena.sparql.algebra.op.OpReduced ;
import com.hp.hpl.jena.sparql.algebra.opt.TransformFilterPlacement ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.core.Substitute ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPeek ;
import com.hp.hpl.jena.sparql.engine.main.OpExecutor ;
import com.hp.hpl.jena.sparql.engine.main.OpExecutorFactory ;
import com.hp.hpl.jena.sparql.engine.main.QC ;
import com.hp.hpl.jena.sparql.expr.ExprList ;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderProc ;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderTransformation ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.store.GraphNamedTDB ;
import com.hp.hpl.jena.tdb.store.GraphTDB ;
import com.hp.hpl.jena.tdb.store.GraphTriplesTDB ;
import com.hp.hpl.jena.tdb.store.NodeId ;

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
        //@Override
        public OpExecutor create(ExecutionContext execCxt)
        { return new OpExecutorTDB(execCxt) ; }
    } ;
    
    private final boolean isForTDB ;
    
    // A new compile object is created for each op compilation.
    // So the execCxt is changing as we go through the query-compile-execute process  
    public OpExecutorTDB(ExecutionContext execCxt)
    {
        super(execCxt) ;
        // NB. The dataset may be a TDB one, or a general one.
        // Any merged union graph magic (for a TDB dataset was handled
        // in QueryEngineTDB).
        
        isForTDB = (execCxt.getActiveGraph() instanceof GraphTDB) ;
    }

    // Retrieving nodes isn't so bad because they will be needed anyway.
    // And if their duplicates, likely to be cached.
    // Need to work with SolverLib which wraps the NodeId bindgins with a converter. 
    
    @Override
    protected QueryIterator execute(OpDistinct opDistinct, QueryIterator input)
    {
        return super.execute(opDistinct, input) ;
    }
    
    @Override
    protected QueryIterator execute(OpReduced opReduced, QueryIterator input)
    {
        return super.execute(opReduced, input) ;
    }

    
    @Override
    protected QueryIterator execute(OpFilter opFilter, QueryIterator input)
    {
        if ( ! isForTDB )
            return super.execute(opFilter, input) ;
        
        // (filter (bgp ...))
        if ( OpBGP.isBGP(opFilter.getSubOp()) )
        {
            // Still may be a TDB graph in a non-TDB dataset (e.g. a named model)
            GraphTDB graph = (GraphTDB)execCxt.getActiveGraph() ;
            OpBGP opBGP = (OpBGP)opFilter.getSubOp() ;
            return executeBGP(graph, opBGP, input, opFilter.getExprs(), execCxt) ;
        }
        
        // (filter (quadpattern ...))
        if ( opFilter.getSubOp() instanceof OpQuadPattern )
        {
            OpQuadPattern quadPattern = (OpQuadPattern)opFilter.getSubOp() ;
            DatasetGraphTDB ds = (DatasetGraphTDB)execCxt.getDataset() ;
            return optimizeExecuteQuads(ds, input,
                                        quadPattern.getGraphNode(), quadPattern.getBasicPattern(),
                                        opFilter.getExprs(), execCxt) ;
        }
    
        // (filter (anything else))
        return super.execute(opFilter, input) ;
        }

    // ---- Triple patterns
    
    @Override
    protected QueryIterator execute(OpBGP opBGP, QueryIterator input)
    {
        if ( ! isForTDB )
            return super.execute(opBGP, input) ;
        
        GraphTDB graph = (GraphTDB)execCxt.getActiveGraph() ;
        return executeBGP(graph, opBGP, input, null, execCxt) ;
       
    }

    @Override
    protected QueryIterator execute(OpQuadPattern quadPattern, QueryIterator input)
    {
        if ( ! isForTDB )
            return super.execute(quadPattern, input) ;
            
    //        DatasetGraph dg = execCxt.getDataset() ;
    //        if ( ! ( dg instanceof DatasetGraphTDB ) )
    //            throw new InternalErrorException("Not a TDB backed dataset in quad pattern execution") ;
        
        DatasetGraphTDB ds = (DatasetGraphTDB)execCxt.getDataset() ;
        BasicPattern bgp = quadPattern.getBasicPattern() ;
        Node gn = quadPattern.getGraphNode() ;
        return optimizeExecuteQuads(ds, input, gn, bgp, null, execCxt) ;
    }

    /** Execute a BGP (and filters) on a TDB graph, which may be in default storage or it may be a named graph */ 
    private static QueryIterator executeBGP(GraphTDB graph, OpBGP opBGP, QueryIterator input, ExprList exprs, 
                                         ExecutionContext execCxt)
    {
        // Is it the real default graph (normal route or explicitly named)?
        if ( ! isDefaultGraphStorage(graph.getGraphNode()))
        {
            // Not default storage - it's a named graph in storage. 
            DatasetGraphTDB ds = graph.getDataset() ;
            return optimizeExecuteQuads(ds, input, graph.getGraphNode(), opBGP.getPattern(), exprs, execCxt) ;
        }
        
        // Execute a BGP on the real default graph
        return optimizeExecuteTriples(graph, input, opBGP.getPattern(), exprs, execCxt) ;
    }

    /** Execute, with optimization, a basic graph pattern on the default graph storage */
    private static QueryIterator optimizeExecuteTriples(GraphTDB graph, QueryIterator input,
                                                        BasicPattern pattern, ExprList exprs,
                                                        ExecutionContext execCxt)
    {
        if ( ! input.hasNext() )
            return input ;
    
        // -- Input
        // Must pass this iterator into the next stage.
        ReorderTransformation transform = graph.getReorderTransform() ;
        if ( transform != null )
        {
            QueryIterPeek peek = QueryIterPeek.create(input, execCxt) ;
            input = peek ; // Must pass on
            pattern = reorder(pattern, peek, transform) ;
        }
        // -- Filter placement
            
        Op op = null ;
        if ( exprs != null )
            op = TransformFilterPlacement.transform(exprs, pattern) ;
        else
            op = new OpBGP(pattern) ;
        
        return plainExecute(op, input, execCxt) ;
    }

    /** Execute, with optimization, a quad pattern */
    private static QueryIterator optimizeExecuteQuads(DatasetGraphTDB ds, 
                                                      QueryIterator input, 
                                                      Node gn, BasicPattern bgp,
                                                      ExprList exprs, ExecutionContext execCxt)
    {
        if ( ! input.hasNext() )
            return input ;

        // ---- Graph names with special meaning. 

        gn = decideGraphNode(gn, execCxt) ;
        if ( gn == null )
            return optimizeExecuteTriples(ds.getEffectiveDefaultGraph(), input, bgp, exprs, execCxt) ;
        
        // ---- Execute quads+filters
        ReorderTransformation transform = ds.getTransform() ;

        if ( transform != null )
        {
            QueryIterPeek peek = QueryIterPeek.create(input, execCxt) ;
            input = peek ; // Original input now invalid.
            bgp = reorder(bgp, peek, transform) ;
        }

        // -- Filter placement
        Op op = null ;
        if ( exprs != null )
            op = TransformFilterPlacement.transform(exprs, gn, bgp) ;
        else
            op = new OpQuadPattern(gn, bgp) ;

        return plainExecute(op, input, execCxt) ;
    }

    /** Execute without modification of the op - does <b>not</b> apply special graph name translations */ 
    private static QueryIterator plainExecute(Op op, QueryIterator input, ExecutionContext execCxt)
    {
        // -- Execute
        // Switch to a non-reordring executor
        // The Op may be a sequence due to TransformFilterPlacement
        // so we need to do a full execution step, not go straight to the SolverLib.
        
        ExecutionContext ec2 = new ExecutionContext(execCxt) ;
        ec2.setExecutor(plainFactory) ;

        // Solve without going through this executor again.
        // There would be issues of nested patterns but this is only a
        // (filter (bgp...)) or (filter (quadpattern ...)) or sequences of these.
        // so there are no nested patterns to reorder.
        return QC.execute(op, input, ec2) ;
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
    
    // Null ==> Triples table
    /** Handle special graph node names.  Retunrs null for default graph in storage. */
    public static Node decideGraphNode(Node gn, ExecutionContext execCxt)
    {
     // ---- Graph names with special meaning. 
    
        // Graph names with special meaning:
        //   Quad.defaultGraphIRI -- the IRI used in GRAPH <> to mean the default graph.
        //   Quad.defaultGraphNodeGenerated -- the internal marker node used for the quad form of queries.
        //   Quad.unionGraph -- the IRI used in GRAPH <> to mean the union of named graphs
    
        if ( isDefaultGraphStorage(gn) ) 
        {
            // Storage concrete, default graph. 
            // Either outside GRAPH (no implicit union)
            // or using the "name" of the default graph
            return null ;
        }

        // Not default storage graph.
        // ---- Union (RDF Merge) of named graphs
        boolean doingUnion = false ;
    
//        if ( isUnionDefaultGraph && Quad.isQuadDefaultGraphNode(gn) ) 
//            // Implicit: default graph is union of named graphs.  (rewritten - does not occur). 
//            doingUnion = true ;
    
        if ( gn.equals(Quad.unionGraph) )
            // Explicit name of the union of named graphs
            doingUnion = true ;
    
        if ( doingUnion )
            // Set the "any" graph node.
            gn = Node.ANY ;
        return gn ;
    }

    // Is this a query against the real default graph in the storage (in a 3-tuple table). 
    private static boolean isDefaultGraphStorage(Node gn)
    {
        if ( gn == null )
            return true ;
        
        // Is it the implicit name for default graph.
        if ( Quad.isDefaultGraph(gn) )
            // Not accessing the union of named graphs as the default graph
            // and pattern is directed to the default graph.
            return true ;
    
        return false ;
    }
    
    @Override
    protected QueryIterator execute(OpDatasetNames dsNames, QueryIterator input)
    { 
        DatasetGraphTDB ds = (DatasetGraphTDB)execCxt.getDataset() ;
        Filter<Tuple<NodeId>> filter = QC2.getFilter(execCxt.getContext()) ;
        return SolverLib.graphNames(ds, dsNames.getGraphNode(), input, filter, execCxt) ;
    }
    
    // ---- OpExecute factories and plain executor.
    
    private static OpExecutorFactory plainFactory = new OpExecutorPlainFactoryTDB() ;
    private static class OpExecutorPlainFactoryTDB implements OpExecutorFactory
    {
        //@Override
        public OpExecutor create(ExecutionContext execCxt)
        {
            return new OpExecutorPlainTDB(execCxt) ;
        }
    }

    /** An op executor that simply executes a BGP or QuadPattern without any reordering */ 
    private static class OpExecutorPlainTDB extends OpExecutor
    {
        Filter<Tuple<NodeId>> filter = null ;
        
        public OpExecutorPlainTDB(ExecutionContext execCxt)
        {
            super(execCxt) ;
            filter = QC2.getFilter(execCxt.getContext()) ;
        }
        
        @Override
        public QueryIterator execute(OpBGP opBGP, QueryIterator input)
        {
            Graph g = execCxt.getActiveGraph() ;
            
            if ( g instanceof GraphTDB )
            {
                BasicPattern bgp = opBGP.getPattern() ;
                if ( Explain.explaining(Explain.InfoLevel.ALL, execCxt.getContext()) )
                    Explain.explain("Execute", bgp, execCxt.getContext()) ;
                // Triple-backed (but may be named as explicit default graph).
                return SolverLib.execute((GraphTDB)g, bgp, input, filter, execCxt) ;
            }
            Log.warn(this, "Non-GraphTDB passed to OpExecutorPlainTDB") ;
            return super.execute(opBGP, input) ;
        }
        
        @Override
        public QueryIterator execute(OpQuadPattern opQuadPattern, QueryIterator input)
        {
            Node gn = opQuadPattern.getGraphNode() ;
            gn = decideGraphNode(gn, execCxt) ;
            
            if ( execCxt.getDataset() instanceof DatasetGraphTDB )
            {
                DatasetGraphTDB ds = (DatasetGraphTDB)execCxt.getDataset() ;
                
                if ( Explain.explaining(Explain.InfoLevel.ALL, execCxt.getContext()) )
                    Explain.explain("Execute", opQuadPattern.getPattern(), execCxt.getContext()) ;

                BasicPattern bgp = opQuadPattern.getBasicPattern() ;
                return SolverLib.execute(ds, gn, bgp, input, filter, execCxt) ;
            }
            // Maybe a TDB named graph inside a non-TDB dataset.
            Graph g = execCxt.getActiveGraph() ;
            if ( g instanceof GraphTDB )
            {
                if ( g instanceof GraphTriplesTDB )
                {
                    // Triples graph from TDB (which is the defaul tgraph of the dataset),
                    // used a named graph in a composite dataset.
                    BasicPattern bgp = opQuadPattern.getBasicPattern() ;
                    if ( Explain.explaining(Explain.InfoLevel.ALL, execCxt.getContext()) )
                        Explain.explain("Execute", bgp, execCxt.getContext()) ;
                    return SolverLib.execute((GraphTDB)g, bgp, input, filter, execCxt) ;
                }
                
                if ( g instanceof GraphNamedTDB )
                {
                    // Legacy - when ARQ upgrades, this will be deprecated and need updating.
                    
                    if ( Explain.explaining(Explain.InfoLevel.ALL, execCxt.getContext()) )
                        Explain.explain("Execute", opQuadPattern.getPattern(), execCxt.getContext()) ;

                    // Quad-backed graph
                    return SolverLib.execute(((GraphTDB)g).getDataset(), opQuadPattern.getGraphNode(), opQuadPattern.getBasicPattern(), 
                                             input, filter, execCxt) ;
                }
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