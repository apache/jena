/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.solver;

import static com.hp.hpl.jena.tdb.TDB.logExec;
import lib.StrUtils;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.Transform;
import com.hp.hpl.jena.sparql.algebra.TransformCopy;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpLabel;
import com.hp.hpl.jena.sparql.algebra.opt.TransformFilterPlacement;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Substitute;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPeek;
import com.hp.hpl.jena.sparql.engine.main.OpExecutor;
import com.hp.hpl.jena.sparql.engine.main.OpExecutorFactory;
import com.hp.hpl.jena.sparql.engine.main.QC;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.tdb.pgraph.GraphTDB;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderProc;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderTransformation;
import com.hp.hpl.jena.tdb.sys.SystemTDB;

/** TDB executor for algebra expressions.  It is the standard ARQ executor
 *  except for basic graph patterns and filtered basic graph patterns (currently).  
 * 
 * See also: StageGeneratorDirectTDB, a non-reordering 
 * 
 * @author Andy Seaborne
 */
public class OpExecutorTDB extends OpExecutor
{
    public static OpExecutorFactory altFactory = new OpExecutorFactory()
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
    
    private boolean isForTDB ;
    
    // A new compile object is created for each op compilation.
    // So the execCxt is changing as we go through the query-compile-execute process  
    public OpExecutorTDB(ExecutionContext execCxt)
    {
        super(execCxt) ;
        isForTDB = (execCxt.getActiveGraph() instanceof GraphTDB) ;
    }

    @Override
    public QueryIterator execute(OpBGP opBGP, QueryIterator input)
    {
        if ( ! isForTDB )
            return super.execute(opBGP, input) ;
        GraphTDB graph = (GraphTDB)execCxt.getActiveGraph() ;
        return optimizeExecute(graph, input, opBGP.getPattern(), null, execCxt) ;
    }
    
    @Override
    public QueryIterator execute(OpLabel opLabel, QueryIterator input)
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
    public QueryIterator execute(OpFilter opFilter, QueryIterator input)
    {
        if ( ! isForTDB )
            return super.execute(opFilter, input) ;
        
        if ( ! OpBGP.isBGP(opFilter.getSubOp()) )
            return super.execute(opFilter, input) ;

        OpBGP opBGP = (OpBGP)opFilter.getSubOp() ;
        GraphTDB graph = (GraphTDB)execCxt.getActiveGraph() ;
        
        return optimizeExecute(graph, input, opBGP.getPattern(), opFilter.getExprs(), execCxt) ;
    }

    // SolverLib??
    public static QueryIterator optimizeExecute(GraphTDB graph, QueryIterator input, BasicPattern pattern, 
                                                ExprList exprs, ExecutionContext execCxt)
    {
        if ( ! input.hasNext() )
            return input ;
        
        // Must pass this into next stage. 
        QueryIterPeek peek = QueryIterPeek.create(input, execCxt) ;
        input = null ; // Now invalid.
        BasicPattern pattern2 = Substitute.substitute(pattern, peek.peek() ) ;

        // -- Reorder
        ReorderTransformation transform = graph.getReorderTransform() ;
        if ( transform != null )
        {
            // Calculate the reordering based on the substituted pattern.
            ReorderProc proc = transform.reorderIndexes(pattern2) ;
            // Then reorder original patten
            pattern = proc.reorder(pattern) ; 
        }
        
        // -- Filter placement
        Op op = null ;
        if ( exprs != null )
            op = TransformFilterPlacement.transform(exprs, pattern) ;
        else
            op = new OpBGP(pattern) ;
        
        // -- Explain
        if ( execCxt.getContext().isTrue(SystemTDB.symLogExec) && logExec.isInfoEnabled() )
        {
            String x = op.toString();
            x = StrUtils.chop(x) ;
            
            while ( x.endsWith("\n") )
                x = StrUtils.chop(x) ;
            while ( x.endsWith("\r") )
                x = StrUtils.chop(x) ;
            x = "Execute:: \n"+x ;
            logExec.info(x) ;
        }
        
        // -- Execute
        // Switch to a non-reordring executor
        ExecutionContext ec2 = new ExecutionContext(execCxt) ;
        ec2.setExecutor(plainFactory) ;

        // Solve without going through this executor again.
        // There would be issues of nested (graph ...)
        // but this is only a (filter (bgp...)) at most
        return QC.execute(op, peek, ec2) ;
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
    
    // This can be the standard one if the stage generator is StageGeneratorDirectTDB   
    /** An op executor that simply executes a BGP without any reordering */ 
    private static class OpExecutorPlainTDB extends OpExecutor
    {
        public OpExecutorPlainTDB(ExecutionContext execCxt)
        {
            super(execCxt) ;
        }
        
        @Override
        public QueryIterator execute(OpBGP opBGP, QueryIterator input)
        {
            if ( ! (execCxt.getActiveGraph() instanceof GraphTDB) )
                return super.execute(opBGP, input) ;
            // Log execution.
            GraphTDB graph = (GraphTDB)execCxt.getActiveGraph() ;
            return SolverLib.execute(graph, opBGP.getPattern(), input, execCxt) ;
        }
    }
}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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