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

package org.apache.jena.tdb.solver;

import java.util.function.Predicate;

import org.apache.jena.atlas.lib.tuple.Tuple ;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.ARQInternalErrorException ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.algebra.op.* ;
import org.apache.jena.sparql.algebra.optimize.TransformFilterPlacement ;
import org.apache.jena.sparql.core.BasicPattern ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.core.Substitute ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.iterator.QueryIterPeek ;
import org.apache.jena.sparql.engine.main.OpExecutor ;
import org.apache.jena.sparql.engine.main.OpExecutorFactory ;
import org.apache.jena.sparql.engine.main.QC ;
import org.apache.jena.sparql.engine.main.iterator.QueryIterGraph ;
import org.apache.jena.sparql.engine.optimizer.reorder.ReorderProc ;
import org.apache.jena.sparql.engine.optimizer.reorder.ReorderTransformation ;
import org.apache.jena.sparql.expr.ExprList ;
import org.apache.jena.sparql.mgt.Explain ;
import org.apache.jena.tdb.store.DatasetGraphTDB ;
import org.apache.jena.tdb.store.GraphTDB ;
import org.apache.jena.tdb.store.NodeId;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/** TDB executor for algebra expressions.  It is the standard ARQ executor
 *  except for basic graph patterns and filtered basic graph patterns (currently).
 *
 * See also: StageGeneratorDirectTDB, a non-reordering
 */
public class OpExecutorTDB1 extends OpExecutor
{
    private static final Logger log = LoggerFactory.getLogger(OpExecutorTDB1.class) ;

    public final static OpExecutorFactory OpExecFactoryTDB = new OpExecutorFactory()
    {
        @Override
        public OpExecutor create(ExecutionContext execCxt)
        { return new OpExecutorTDB1(execCxt) ; }
    } ;

    private final boolean isForTDB ;

    // A new compile object is created for each op compilation.
    // So the execCxt is changing as we go through the query-compile-execute process
    public OpExecutorTDB1(ExecutionContext execCxt)
    {
        super(execCxt) ;
        // NB. The dataset may be a TDB one, or a general one.
        // Any merged union graph magic (for a TDB dataset was handled
        // in QueryEngineTDB).

        isForTDB = (execCxt.getActiveGraph() instanceof GraphTDB) ;
    }

    @Override
    protected QueryIterator exec(Op op, QueryIterator input) {
        if ( level < 0 )
            // Print only at top level (and we're called before level++)
            Explain.explain("TDB1", op, super.execCxt.getContext()) ;
        return super.exec(op, input) ;
    }

    @Override
    protected QueryIterator execute(OpFilter opFilter, QueryIterator input)
    {
        if ( ! isForTDB )
            return super.execute(opFilter, input) ;

        // If the filter does not apply to the input??
        // Where does ARQ catch this?

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

    @Override
    protected QueryIterator execute(OpGraph opGraph, QueryIterator input)
    {
        // Path evaluation or dataset sets which do not go straight to the DatasetGraphTDB
        return new QueryIterGraph(input, opGraph, execCxt) ;
    }

    /** Execute a BGP (and filters) on a TDB graph, which may be in default storage or it may be a named graph */
    private static QueryIterator executeBGP(GraphTDB graph, OpBGP opBGP, QueryIterator input, ExprList exprs,
                                            ExecutionContext execCxt)
    {
        DatasetGraphTDB dsgtdb = graph.getDatasetGraphTDB() ;
        // Is it the real default graph (normal route or explicitly named)?
        if ( ! isDefaultGraphStorage(graph.getGraphName()))
        {
            // Not default storage - it's a named graph in storage.
            return optimizeExecuteQuads(dsgtdb, input, graph.getGraphName(), opBGP.getPattern(), exprs, execCxt) ;
        }

        // Execute a BGP on the real default graph
        return optimizeExecuteTriples(dsgtdb, input, opBGP.getPattern(), exprs, execCxt) ;
    }

    /** Execute, with optimization, a basic graph pattern on the default graph storage */
    private static QueryIterator optimizeExecuteTriples(DatasetGraphTDB dsgtdb, QueryIterator input,
                                                        BasicPattern pattern, ExprList exprs,
                                                        ExecutionContext execCxt)
    {
        if ( ! input.hasNext() )
            return input ;

        // -- Input
        // Must pass this iterator into the next stage.
        if ( pattern.size() >= 2 )
        {
            // Must be 2 or triples to reorder.
            ReorderTransformation transform = dsgtdb.getReorderTransform() ;
            if ( transform != null )
            {
                QueryIterPeek peek = QueryIterPeek.create(input, execCxt) ;
                input = peek ; // Must pass on
                pattern = reorder(pattern, peek, transform) ;
            }
        }
        if ( exprs == null ) {
            Explain.explain("Execute", pattern, execCxt.getContext());
            Predicate<Tuple<NodeId>> filter = QC2.getFilter(execCxt.getContext());
            return PatternMatchTDB1.execute(dsgtdb, Quad.defaultGraphNodeGenerated, pattern, input, filter, execCxt);
        }
        // -- Filter placement

        Op op = TransformFilterPlacement.transform(exprs, pattern);
        return plainExecute(op, input, execCxt);
    }

    /** Execute, with optimization, a quad pattern */
    private static QueryIterator optimizeExecuteQuads(DatasetGraphTDB dsgtdb,
                                                      QueryIterator input,
                                                      Node gn, BasicPattern bgp,
                                                      ExprList exprs, ExecutionContext execCxt)
    {
        if ( ! input.hasNext() )
            return input ;

        // ---- Graph names with special meaning.

        gn = decideGraphNode(gn, execCxt) ;
        if ( gn == null )
            return optimizeExecuteTriples(dsgtdb, input, bgp, exprs, execCxt) ;

        // ---- Execute quads+filters
        if ( bgp.size() >= 2 )
        {
            ReorderTransformation transform = dsgtdb.getReorderTransform() ;

            if ( transform != null )
            {
                QueryIterPeek peek = QueryIterPeek.create(input, execCxt) ;
                input = peek ; // Original input now invalid.
                bgp = reorder(bgp, peek, transform) ;
            }
        }

        if ( exprs == null ) {
            // Triple-backed (but may be named as explicit default graph).
            Explain.explain("Execute", bgp, execCxt.getContext());
            Predicate<Tuple<NodeId>> filter = QC2.getFilter(execCxt.getContext());
            return PatternMatchTDB1.execute(dsgtdb, gn, bgp, input, filter, execCxt);
        }

        // -- Filter placement
        Op op = TransformFilterPlacement.transform(exprs, gn, bgp);
        return plainExecute(op, input, execCxt);
    }
    /** Execute without modification of the op - does <b>not</b> apply special graph name translations */
    private static QueryIterator plainExecute(Op op, QueryIterator input, ExecutionContext execCxt)
    {
        // -- Execute
        // Switch to a non-reordering executor
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

    /** Handle special graph node names.
     * Returns null for default graph in storage (use the triple table).
     * Returns Node.ANY for the union graph
     */
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

        if ( Quad.isUnionGraph(gn) )
            return Node.ANY ;
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
        Predicate<Tuple<NodeId>> filter = QC2.getFilter(execCxt.getContext()) ;
        Node gn = dsNames.getGraphNode() ;
        if ( Var.isVar(gn) )
            return SolverLibTDB.graphNames(ds, dsNames.getGraphNode(), input, filter, execCxt) ;
        else
            return SolverLibTDB.testForGraphName(ds, dsNames.getGraphNode(), input, filter, execCxt) ;
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
        Predicate<Tuple<NodeId>> filter = null ;

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
                Explain.explain("Execute", bgp, execCxt.getContext()) ;
                // Triple-backed (but may be named as explicit default graph).
                //return SolverLib.execute((GraphTDB)g, bgp, input, filter, execCxt) ;
                GraphTDB gtdb = (GraphTDB)g ;
                Node gn = decideGraphNode(gtdb.getGraphName(), execCxt) ;
                return PatternMatchTDB1.execute(gtdb.getDatasetGraphTDB(), gn, bgp, input, filter, execCxt) ;
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
                Explain.explain("Execute", opQuadPattern.getPattern(), execCxt.getContext()) ;
                BasicPattern bgp = opQuadPattern.getBasicPattern() ;
                return PatternMatchTDB1.execute(ds, gn, bgp, input, filter, execCxt) ;
            }
            // Maybe a TDB named graph inside a non-TDB dataset.
            Graph g = execCxt.getActiveGraph() ;
            if ( g instanceof GraphTDB )
            {
                // Triples graph from TDB (which is the default graph of the dataset),
                // used a named graph in a composite dataset.
                BasicPattern bgp = opQuadPattern.getBasicPattern() ;
                Explain.explain("Execute", bgp, execCxt.getContext()) ;
                // Don't pass in G -- gn may be different.
                return PatternMatchTDB1.execute(((GraphTDB)g).getDatasetGraphTDB(), gn, bgp, input, filter, execCxt) ;
            }
            Log.warn(this, "Non-DatasetGraphTDB passed to OpExecutorPlainTDB") ;
            return super.execute(opQuadPattern, input) ;
        }

    }
}
