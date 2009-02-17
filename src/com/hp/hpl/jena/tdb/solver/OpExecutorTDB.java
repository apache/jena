/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.solver;

import static com.hp.hpl.jena.tdb.TDB.logExec;

import java.util.ArrayList;
import java.util.List;

import lib.Log;
import lib.StrUtils;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVars;
import com.hp.hpl.jena.sparql.algebra.Transform;
import com.hp.hpl.jena.sparql.algebra.TransformCopy;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpLabel;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern;
import com.hp.hpl.jena.sparql.algebra.opt.TransformFilterPlacement;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.Substitute;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.core.VarAlloc;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterDistinct;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPeek;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterProject;
import com.hp.hpl.jena.sparql.engine.main.OpExecutor;
import com.hp.hpl.jena.sparql.engine.main.OpExecutorFactory;
import com.hp.hpl.jena.sparql.engine.main.QC;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.util.ALog;

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
    public final static OpExecutorFactory altFactory = new OpExecutorFactory()
    {
        @Override
        public OpExecutor create(ExecutionContext execCxt)
        { return new OpExecutorTDB(execCxt) ; }
    } ;
    

    // ---- Stop a BGP being reordered.
    // Normally, this is done by swapping to OpExecutorPlainTDB but this
    // allows specific control for experimentation.
    private static final String executeNow = "TDB:NoReorder" ;
    private static Transform labelBGP = new TransformCopy()
    {
        @Override
        public Op transform(OpBGP opBGP)
        { return OpLabel.create(executeNow, opBGP) ; }
    } ;
    
    private final boolean isForTDB ;
    private final boolean isUnionDefaultGraph ;
    
    // A new compile object is created for each op compilation.
    // So the execCxt is changing as we go through the query-compile-execute process  
    public OpExecutorTDB(ExecutionContext execCxt)
    {
        super(execCxt) ;
        isForTDB = (execCxt.getActiveGraph() instanceof GraphTDB) ;
        isUnionDefaultGraph = execCxt.getContext().isTrue(TDB.symUnionDefaultGraph) ;
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
        
        DatasetGraphTDB ds = (DatasetGraphTDB)execCxt.getDataset() ;
        BasicPattern bgp = quadPattern.getBasicPattern() ;
        Node gn = quadPattern.getGraphNode() ;
        return optimizeExecute(ds, input, isUnionDefaultGraph, gn, bgp, null, execCxt) ;
    }
    
    /** Execute, with optimization, a quad pattern */
    
    private static QueryIterator optimizeExecute(DatasetGraphTDB ds, 
                                                 QueryIterator input, 
                                                 boolean isUnionDefaultGraph, Node gn, BasicPattern bgp,
                                                 ExprList exprs, ExecutionContext execCxt)
    {
        // ---- Default graph in storage
        boolean isDefaultGraph = false ;
        
        // Graph names with special meaning:
        //   Quad.defaultGraphIRI -- the IRI used in GRAPH <> to mean the default graph.
        //   Quad.defaultGraphNode -- the marker node used by the algebra for the quad form of queries.
        //   Quad.unionGraph -- the IRI used in GRAPH <> to mean the union of named graphs
        // Also: isUnionDefaultGraph if implicit union of named graphs.
        
        if ( gn.equals(Quad.defaultGraphIRI) )
            // Explicit GRAPH <urn:x-arq:DefaultGraph> {}
            isDefaultGraph = true ;
        
        if ( ! isUnionDefaultGraph && Quad.isDefaultGraphNode(gn) )
            // Not accessing the union of named graphs as the default graph
            // and pattern is directed to the default graph.
            isDefaultGraph = true ;

        if ( isDefaultGraph )
        {
            // Storage default graph 
            // Either outside GRAPH (no implicit union) or using the "name" of the default graph
            return optimizeExecute(ds.getDefaultGraph(), input, bgp, null, execCxt) ;
        }
        
        // ---- Union (RDF Merge) of named graphs
        boolean doingUnion = false ;
        
        if ( isUnionDefaultGraph && Quad.isDefaultGraphNode(gn) ) 
            // Implicit: default graph is union of named graphs 
            doingUnion = true ;
        
        if ( gn.equals(Quad.unionGraph) )
            // Explicit name of the union of named graphs
            doingUnion = true ;
        
        if ( doingUnion )
            // Name for the union of named graphs
            gn = VarAlloc.getVarAllocator().allocVar() ;
        
        // ----
        // TEMP: Filters were not considered.
        ReorderTransformation transform = ds.getTransform() ;
        
        if ( transform != null )
        {
            QueryIterPeek peek = QueryIterPeek.create(input, execCxt) ;
            input = peek ; // Original input now invalid.
            bgp = reorder(bgp, peek, transform) ;
        }
        
        // -- Filter placement
        // Does not operate on an op at this point.
        // Needs rework.  Needs intergation with the bgp execution path.
        if ( exprs != null )
            ALog.warn(OpExecutorTDB.class, "Expression for filters passed to quad otimize/execute") ;
        
//        Op op = null ;
//        if ( exprs != null )
//            op = TransformFilterPlacement.transform(exprs, bgp) ;
//        else
//            op = new OpBGP(bgp) ;
        
        QueryIterator qIter = SolverLib.execute(ds, gn, bgp, input, execCxt) ;
        if ( doingUnion )
        {
            //??? QueryIterProjectHide
            List<Var> vars = new ArrayList<Var>() ;
            OpVars.vars(bgp, vars) ;
            qIter = new QueryIterProject(qIter, vars, execCxt) ;
            qIter = new QueryIterDistinct(qIter, execCxt) ;
        }
        return qIter ;
    }
    
    @Override
    protected QueryIterator execute(OpBGP opBGP, QueryIterator input)
    {
        if ( ! isForTDB )
            return super.execute(opBGP, input) ;
        
        if ( isUnionDefaultGraph )
            return execute(new OpQuadPattern(Quad.unionGraph, opBGP.getPattern()), input) ; 
        
        GraphTDB graph = (GraphTDB)execCxt.getActiveGraph() ;
        return optimizeExecute(graph, input, opBGP.getPattern(), null, execCxt) ;
    }
    
    @Override
    protected QueryIterator execute(OpLabel opLabel, QueryIterator input)
    {
        if ( ! isForTDB )
            return super.execute(opLabel, input) ;
        
        if ( executeNow.equals(opLabel.getObject()) )
        {
            GraphTDB graph = (GraphTDB)execCxt.getActiveGraph() ;
            OpBGP opBGP = (OpBGP)opLabel.getSubOp() ; 
            return SolverLib.execute(graph, opBGP.getPattern(), input, execCxt) ;
        }

        return super.execute(opLabel, input) ;
    }

    @Override
    protected QueryIterator execute(OpFilter opFilter, QueryIterator input)
    {
        // XXX Not active - quad transform was applied so no OpBGPs.
        if ( ! isForTDB )
            return super.execute(opFilter, input) ;
        
        if ( OpBGP.isBGP(opFilter.getSubOp()) )
        {
            OpBGP opBGP = (OpBGP)opFilter.getSubOp() ;
            GraphTDB graph = (GraphTDB)execCxt.getActiveGraph() ;
            return optimizeExecute(graph, input, opBGP.getPattern(), opFilter.getExprs(), execCxt) ;
        }
        // No filter placement.
//        if ( opFilter.getSubOp() instanceof OpQuadPattern && ! isUnionDefaultGraph )
//        {
//            DatasetGraphTDB ds = (DatasetGraphTDB)execCxt.getDataset() ;
//            OpQuadPattern quadPattern = (OpQuadPattern)opFilter.getSubOp() ;
//            return optimizeExecute(ds, 
//                                   input, isUnionDefaultGraph, quadPattern.getGraphNode(),
//                                   quadPattern.getBasicPattern(), opFilter.getExprs(), execCxt) ;
//        }

        // Can't do better at the moment.
        return super.execute(opFilter, input) ;
    }

    /** Execute, with optimization, the basic graph pattern */
    public static QueryIterator optimizeExecute(/*DatasetGraphTDB ds,*/ GraphTDB graph, QueryIterator input,
                                                /*Node graphNode,*/ BasicPattern pattern, ExprList exprs,
                                                ExecutionContext execCxt)
    {
        return optimizeExecute(graph.getReorderTransform(), input, pattern, exprs, execCxt) ;
    }
    
    private static QueryIterator optimizeExecute(ReorderTransformation transform, 
                                                 QueryIterator input,
                                                 BasicPattern pattern, 
                                                 ExprList exprs,
                                                 ExecutionContext execCxt)
    {
        if ( ! input.hasNext() )
            return input ;
        
        // Must pass this iterator into the next stage. 
        QueryIterPeek peek = QueryIterPeek.create(input, execCxt) ;
        input = null ; // and not this one which is now invalid.
        
        BasicPattern patternOriginal = pattern ;
        BasicPattern pattern2 = Substitute.substitute(pattern, peek.peek() ) ;

        // -- Reorder
        pattern = reorder(pattern, peek, transform) ;
        
        // -- Filter placement
        Op op = null ;
        if ( exprs != null )
            op = TransformFilterPlacement.transform(exprs, pattern) ;
        else
            op = new OpBGP(pattern) ;
        
        // -- Explain
        if ( execCxt.getContext().isTrue(TDB.symLogExec) && logExec.isInfoEnabled() )
        {
            String x = op.toString();
            x = StrUtils.chop(x) ;
            while ( x.endsWith("\n") || x.endsWith("\r") )
                x = StrUtils.chop(x) ;
            x = "Execute:: \n"+x ;
            logExec.info(x) ;
        }
        
        // -- Execute
        // Switch to a non-reordring executor
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
            BasicPattern pattern2 = Substitute.substitute(pattern, peek.peek() ) ;
            // Calculate the reordering based on the substituted pattern.
            ReorderProc proc = transform.reorderIndexes(pattern2) ;
            // Then reorder original patten
            pattern = proc.reorder(pattern) ; 
        }
        return pattern ;
    }
    
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