/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.solver;

import static com.hp.hpl.jena.tdb.TDB.logExec;

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
import com.hp.hpl.jena.sparql.engine.main.OpCompiler;
import com.hp.hpl.jena.sparql.engine.main.OpCompilerFactory;
import com.hp.hpl.jena.sparql.engine.main.QC;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.pgraph.GraphTDB;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderProc;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderTransformation;

public class OpCompilerTDB extends OpCompiler
{
    public static OpCompilerFactory altFactory = new OpCompilerFactory() {

        @Override
        public OpCompiler create(ExecutionContext execCxt)
        {
            return new OpCompilerTDB(execCxt) ;
        }} ;
    

    // ---- Stop a BGP being reordered, again. 
    static String executeNow = "DirectTDB" ;
    private static Transform labelBGP = new TransformCopy()
    {
        @Override
        public Op transform(OpBGP opBGP)
        {
            return OpLabel.create(executeNow, opBGP) ;
        }
    } ;
    
    private boolean isForTDB ;
    
    // A new compile object is created for each op compilation.
    // So the execCxt is changing as we go through the query-compile-execute process  
    public OpCompilerTDB(ExecutionContext execCxt)
    {
        super(execCxt) ;
        isForTDB = (execCxt.getActiveGraph() instanceof GraphTDB) ;
    }

    @Override
    public QueryIterator compile(OpBGP opBGP, QueryIterator input)
    {
        if ( ! isForTDB )
            return super.compile(opBGP, input) ;
        GraphTDB graph = (GraphTDB)execCxt.getActiveGraph() ;
        return optimizeExecute(graph, input, opBGP.getPattern(), null, execCxt) ;
    }
    
    @Override
    public QueryIterator compile(OpLabel opLabel, QueryIterator input)
    {
        if ( ! isForTDB )
            return super.compile(opLabel, input) ;
        
        if ( executeNow.equals(opLabel.getObject()) )
        {
            GraphTDB graph = (GraphTDB)execCxt.getActiveGraph() ;
            OpBGP opBGP = (OpBGP)opLabel.getSubOp() ; 
            return SolverLib.execute(graph, opBGP.getPattern(), input, execCxt) ;
        }

        return super.compile(opLabel, input) ;
    }

    @Override
    public QueryIterator compile(OpFilter opFilter, QueryIterator input)
    {
        if ( ! isForTDB )
            return super.compile(opFilter, input) ;
        
        if ( ! OpBGP.isBGP(opFilter.getSubOp()) )
            return super.compile(opFilter, input) ;

        OpBGP opBGP = (OpBGP)opFilter.getSubOp() ;
        GraphTDB graph = (GraphTDB)execCxt.getActiveGraph() ;
        
        return optimizeExecute(graph, input, opBGP.getPattern(), opFilter.getExprs(), execCxt) ;
    }

    // SolverLib??
    public static QueryIterator optimizeExecute(GraphTDB graph, QueryIterator input, BasicPattern pattern, ExprList exprs, ExecutionContext execCxt)
    {
        if ( ! input.hasNext() )
            return input ;
        
        // Must pass this into next stage. 
        QueryIterPeek peek = QueryIterPeek.create(input, execCxt) ;
        input = null ; // Now invalid.
        BasicPattern pattern2 = Substitute.substitute(pattern, peek.peek() ) ;

        // Calc the reorder from this as a prototypical patten
        // to be executed after substitution. 
        ReorderTransformation transform = graph.getReorderTransform() ;
        if ( transform != null )
        {
            // Calculate the reordering based on the substituted pattern.
            ReorderProc proc = transform.reorderIndexes(pattern2) ;
            // Then reorder original patten
            pattern = proc.reorder(pattern) ; 
        }
        
        Op op = null ;
        if ( exprs != null )
            op = TransformFilterPlacement.transform(exprs, pattern) ;
        else
            op = new OpBGP(pattern) ;
        
        if ( execCxt.getContext().isTrue(TDB.symLogExec) && logExec.isInfoEnabled() )
        {
            // Really want a one line version
            String x = op.toString();
            x = x.replaceAll("\n", "") ;
            x = x.replaceAll("\r", "") ;
            x = "Execute:: "+x ;
            logExec.info(x) ;
        }
        
        ExecutionContext ec2 = new ExecutionContext(execCxt) ;
        // No - change to one that catches BGP execution and (plainly) executes it. 
        ec2.setExecutor(OpCompiler.stdFactory) ;
        
        // Solve without going through this factory again.
        // There would be issues of nested (graph ...)
        // but this is only a (filter (bgp...)) at most
        return QC.compile(op, peek, ec2) ;
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